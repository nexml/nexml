package Bio::Phylo::BranchClust;
use Bio::Phylo::Factory;
use Bio::Phylo::IO 'parse';
use Data::Dumper;

BEGIN {
    require Exporter;
    use vars qw(@ISA @EXPORT_OK %EXPORT_TAGS);

    # classic subroutine exporting
    @ISA         = qw(Exporter);
    @EXPORT_OK   = qw(bc);
    %EXPORT_TAGS = ( 'all' => [ @EXPORT_OK ] );
}

my ( $fac, $logger );

sub new {
    my $class = shift;
    my %args = @_ if @_;
    my $self = {
        '_forest'  => $args{'-forest'},
        '_taxa'    => $args{'-taxa'},
        '_many'    => $args{'-many'},
        '_clust'   => [],
        '_fam'     => [],
        '_tallest' => [],
    };
    bless $self, $class;
    $fac = Bio::Phylo::Factory->new if not $fac;
    $logger = $fac->create_logger if not $logger;
    $self->initialize(@ARGV) if @ARGV;
    return $self;
}

sub bc {
    my $bc = __PACKAGE__->new;
    $bc->set_many( scalar @{ $bc->get_taxa->get_entities } );
    my $tree = $bc->get_tree->clone;
    $bc->_recurse( $tree->get_tallest_tip, $tree );
    $bc->_find_paralogs; 
    for my $fam ( @{ $bc->get_families } ) {
        print join(',', @{ $fam } ), "\n";
    }
    $bc->test('families.list');
}

sub test {
    my ( $self, $famlistfile ) = @_;
    open my $fh, '<', $famlistfile or die $!;
    my @families;
    while(<$fh>){
        chomp;
        next if /^INCOMPLETE:\s+\d+\s*$/ or /^COMPLETE:\s+\d+\s*$/;
        my %members = map { $_ => 1 } split /\s+/, $_;
        push @families, \%members;
    }
    my @attempt = @{ $self->get_families };
    $logger->error if scalar @attempt != scalar @families;
    $logger->warn( "going to compare families" );
    my $success;
    FAMILY: for my $fam ( @attempt ) {
        my %family = map { $_ => 1 } @{ $fam };
        PARSED: for my $parsed ( @families ) {
            %parsed_family = %{ $parsed };
            next PARSED if scalar keys %family != scalar keys %parsed_family;
            for my $key ( keys %family ) {
                next PARSED if not exists $parsed_family{$key};
                next PARSED if $family{$key} ne $parsed_family{$key};
            }
            $logger->warn(++$success);
            next FAMILY;
        }
    }
    $logger->warn( $success == scalar @attempt ? 'success' : 'fail - incomplete?' );
}

sub initialize {
    my ( $self, $gi_file, $tree_file ) = @_;
    my %matches = $self->_parse_gi_file( $gi_file );
    $self->_parse_tree_file( $tree_file, %matches );
}

sub _parse_gi_file {
    my ( $self, $gi_file ) = @_;
    open my $fh, '<', $gi_file or die $!;
    my %matches;
    while(<$fh>) {
        chomp;
        if ( /^(.+?)\|(.+)$/ ) {
            my ( $key, $value ) = ( $1, $2 );
            my @regexes = map { qr/$_/ } grep { $_ } split /\s+/, $value;
            $matches{$key} = \@regexes;
        }
    }
    return %matches;
}

sub _parse_tree_file {
    my ( $self, $tree_file, %matches ) = @_;
    my $taxa = $fac->create_taxa;
    my $forest = parse( '-format' => 'newick', '-file' => $tree_file );
    $self->set_taxa( $taxa );
    $self->set_forest( $forest );
    $forest->set_taxa( $taxa );
    $taxa->clear;
    TIP: for my $tip ( @{ $forest->first->get_terminals } ) {
        my $name = $tip->get_name;
        for my $species ( keys %matches ) {
            for my $regex ( @{ $matches{$species} } ) {
                if ( $name =~ $regex ) {
                    my $taxon = $self->_fetch_or_create_taxon( "'$species'" );
                    $tip->set_taxon( $taxon );
                    next TIP;
                }
            }
        }
    }
}

sub add_family {
    my ( $self, @fam ) = @_;
    push @{ $self->get_families }, \@fam;
    return $self;
}

sub get_families { shift->{'_fam'} }

sub add_cluster {
    my ( $self, @clust ) = @_;
    push @{ $self->get_clusters }, \@clust;
    return $self;
}

sub get_clusters { shift->{'_clust'} }

sub set_forest {
    my ( $self, $forest ) = @_;
    $self->{'_forest'} = $forest;
    return $self;
}

sub get_forest { shift->{'_forest'} }

sub get_tree { shift->get_forest->first }

sub set_taxa {
    my ( $self, $taxa ) = @_;
    $self->{'_taxa'} = $taxa;
    return $self;
}

sub get_taxa { shift->{'_taxa'} }

sub set_many {
    my ( $self, $many ) = @_;
    $self->{'_many'} = $many;
    return $self;
}

sub get_many { shift->{'_many'} }

sub _recurse {
    my ( $self, $tallest_tip, $tree ) = @_; 
    if ( my $root = $tallest_tip->set_root_below ) {
        $tree->insert( $root );        
    }
    $tree->_consolidate;
    my $start_of_clade = $tree->get_tallest_tip;
    push @{ $self->{'_tallest'} }, $start_of_clade->get_name;
    
    CLADEGROWTH : while ( $start_of_clade = $start_of_clade->get_parent ) {
        my %seen = map { $_->get_name => 1 } 
                   map { $_->get_taxon } 
                   @{ $start_of_clade->get_terminals };
        my @tips = keys %seen;
        $logger->debug( join ',', map { $_->get_name } @{ $start_of_clade->get_terminals } );
        return if scalar @tips == 1 and $tips[0] eq 'R';
        last CLADEGROWTH if scalar @tips >= $self->get_many or $seen{'R'};
    }

    $self->add_cluster( grep { $_ ne 'R' } map { $_->get_name } @{ $start_of_clade->get_terminals } );
    if ( $start_of_clade->get_parent ) {
        my $newchild = $fac->create_node( '-name' => 'R' );
        $start_of_clade->get_parent->set_child( $newchild );
        $tree->insert( $newchild );
        $newchild->set_taxon( $self->_fetch_or_create_taxon( 'R' ) );        
        $tree->prune_tips( $self->get_clusters->[-1] );
        $self->_recurse( $newchild, $tree ) if $tree->to_newick =~ /\(/;
    }
}

sub _find_paralogs {
    my $self = shift;
    CLUSTER: for my $c ( @{ $self->get_clusters } ) {
        my $tree  = $self->get_forest->first;
        my %clade = map  { $_ => 1 } @{ $c };
        my @tips  = grep { $clade{$_->get_name} } @{ $tree->get_terminals };
        my %taxa  = map  { $_->get_taxon->get_name => $_ } grep { $_->get_taxon } @tips;
        if ( scalar @tips == scalar keys %taxa ) {
            $logger->info( 'no paralogs here!' );
            $self->add_family( @{ $c } );
            next CLUSTER;
        }
        else {
            $self->_resolve_paralog( $c );
        }
    }
}

sub _get_tips_for_taxon {
    my ( $self, $taxon ) = @_;
    my $tree = $self->get_forest->first;
    my @tips_for_taxon = values %{
        { 
            map  { $_->get_name => $_ } 
            grep { $_->get_tree->get_id == $tree->get_id }
            grep { $_ } 
                @{ $taxon->get_nodes } 
        }        
    };
    return @tips_for_taxon;
}

sub _resolve_paralog {
    my ( $self, $cluster ) = @_;
    $logger->info( 'found paralogs in family ' . join ', ', @{ $cluster } );
    my $tree  = $self->get_forest->first;
    my @tips = map { $tree->get_by_name($_) } @{ $cluster };
    my ( @family );
    
    # loop over all genes in current family
    TIP: for my $tip ( @tips ) {
        next TIP if $tip->get_generic( 'seen' );
        my $taxon = $tip->get_taxon;
        my @tips_for_taxon = $self->_get_tips_for_taxon( $taxon );
        
        # no paralogs for this species
        if ( scalar @tips_for_taxon == 1 ) {
            push @family, $tip->get_name;
        }
        else {
            my %tips = map { $_->get_name => $_ } @tips;
            my @tips_for_taxon_in_cluster = grep { exists $tips{$_->get_name} } @tips_for_taxon;
            
            # no paralogs for this species
            if ( scalar @tips_for_taxon_in_cluster == 1 ) {
                push @family, $tip->get_name;
            }
            else {
                my @tallest = map  { $_->[0] }
                              sort { $a->[1] <=> $b->[1] }
                              map  { [ $_, $tip->calc_nodal_distance($_) ] }
                              map  { $tree->get_by_name($_) }
                                  @{ $self->{'_tallest'} };
                for my $tip_in_taxon ( @tips_for_taxon_in_cluster ) {
                    $tip_in_taxon->set_generic( 'seen' => 1 );
                    my $distance = $tree->is_cladogram 
                    	? $tip_in_taxon->calc_nodal_distance( $tallest[0] )
                    	: $tip_in_taxon->calc_patristic_distance( $tallest[0] );
                    $tip_in_taxon->set_generic( 'distance' => $distance );
                } 
                my @sorted = map  { $_->[0] }
                             sort { $a->[1] <=> $b->[1] }
                             map  { [ $_, $_->get_generic( 'distance' ) ] } 
                             @tips_for_taxon_in_cluster;
                push @family, $sorted[-1]->get_name;
            }
        }
    }
    my @uniq = keys %{ { map { $_ => 1 } @family } };
    $self->add_family(@uniq);
}

sub _fetch_or_create_taxon {
    my ( $self, $name ) = @_;
    my $taxa = $self->get_taxa;
    if ( my $taxon = $taxa->get_by_name( $name ) ) {
        return $taxon;
    }
    else {
        my $taxon = $fac->create_taxon( '-name' => $name );
        $taxa->insert( $taxon );
        return $taxon;
    }
}

1;