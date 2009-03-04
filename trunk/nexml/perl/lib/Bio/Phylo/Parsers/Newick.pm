# $Id$
package Bio::Phylo::Parsers::Newick;
use strict;
use Bio::Phylo::IO;
use Bio::Phylo;
use Bio::Phylo::Factory;
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::IO);

no warnings 'recursion';

my $fac = Bio::Phylo::Factory->new;
my $logger = Bio::Phylo->get_logger;

*_from_handle = \&_from_both;
*_from_string = \&_from_both;

=head1 NAME

Bio::Phylo::Parsers::Newick - Parser used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module parses tree descriptions in parenthetical
format. It is called by the L<Bio::Phylo::IO> facade,
don't call it directly.

=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $newick = Bio::Phylo::Parsers::Newick->_new;
 Function: Initializes a Bio::Phylo::Parsers::Newick object.
 Returns : A Bio::Phylo::Parsers::Newick object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = $_[0];
    $logger->debug("instantiating newick parser");
    my $self  = {};
    bless( $self, $class );
    return $self;
}

=begin comment

 Type    : Wrapper
 Title   : _from_both(%options)
 Usage   : $newick->_from_both(%options);
 Function: Extracts trees from file, sends strings to _parse_string()
 Returns : Bio::Phylo::Forest
 Args    : -handle => (\*FH) or -string => (scalar).
 Comments:

=end comment

=cut

sub _from_both {
    my $self  = shift;
    my %args  = @_;

    my $string;    
    
    # we want to remove all line breaks
    if ( $args{'-string'} ) {
        $string = $args{'-string'};
        $string =~ s/\r|\n|\r\n//g;
    }    
    elsif ( $args{'-handle'} ) {
        while ( my $line = $args{'-handle'}->getline ) {
            chomp( $line );
            $string .= $line;
        }
    }
    $logger->debug("concatenated lines");                   
    
    # remove comments, split on trees
    my @trees = $self->_split( $string );
    
    # lazy loading, we only want the forest *now*
    my $forest = $fac->create_forest;
    
    # parse trees
    for my $tree ( @trees ) {
        $forest->insert( $self->_parse_string( $tree ) );
    }
    
    # adding labels to untagged nodes
    if ( $args{'-label'} ) {
        for my $tree ( @{ $forest->get_entities } ) {
            my $i = 1;
            for my $node ( @{ $tree->get_entities } ) {
                if ( not $node->get_name ) {
                    $node->set_name( 'n' . $i++ );
                }
            }
        }
    }

    # done
    if ( $args{'-project'} ) {
    	my $taxa = $forest->make_taxa;
    	$args{'-project'}->insert($taxa,$forest);
    	return $args{'-project'};
    }
    elsif ( $args{'-as_project'} ) {
    	my $taxa = $forest->make_taxa;
    	my $proj = $fac->create_project;
    	$proj->insert($taxa,$forest);
    	return $proj;
    }
    else {
    	return $forest;
    }
}

=begin comment

 Type    : Parser
 Title   : _split($string)
 Usage   : my @strings = $newick->_split($string);
 Function: Creates an array of (decommented) tree descriptions
 Returns : A Bio::Phylo::Forest::Tree object.
 Args    : $string = concatenated tree descriptions

=end comment

=cut

sub _split {
    my ( $self, $string ) = @_;
    my ( $QUOTED, $COMMENTED ) = ( 0, 0 );
    my $decommented = '';
    my @trees;
    TOKEN: for my $i ( 0 .. length( $string ) ) {
        if ( ! $QUOTED && ! $COMMENTED && substr($string,$i,1) eq "'" ) {
            $QUOTED++;
        }
        elsif ( ! $QUOTED && ! $COMMENTED && substr($string,$i,1) eq "[" ) {
            $COMMENTED++;
            next TOKEN;
        }
        elsif ( ! $QUOTED && $COMMENTED && substr($string,$i,1) eq "]" ) {
            $COMMENTED--;
            next TOKEN;
        }
        elsif ( $QUOTED && ! $COMMENTED && substr($string,$i,1) eq "'" && substr($string,$i,2) ne "''" ) {
            $QUOTED--;
        }
        $decommented .= substr($string,$i,1) unless $COMMENTED;
        if ( ! $QUOTED && ! $COMMENTED && substr($string,$i,1) eq ';' ) {
            push @trees, $decommented;
            $decommented = '';
        }
    }
    $logger->debug("removed comments, split on tree descriptions");
    return @trees;
}

=begin comment

 Type    : Parser
 Title   : _parse_string($string)
 Usage   : my $tree = $newick->_parse_string($string);
 Function: Creates a populated Bio::Phylo::Forest::Tree object from a newick
           string.
 Returns : A Bio::Phylo::Forest::Tree object.
 Args    : $string = a newick tree description

=end comment

=cut

sub _parse_string {
    my ( $self, $string ) = @_;
    $logger->debug("going to parse tree string '$string'");
    my $tree = $fac->create_tree;
    my $remainder = $string;
    my $token;
    my @tokens;
    while ( ( $token, $remainder ) = $self->_next_token( $remainder ) ) {
        last if ( ! defined $token || ! defined $remainder );
        $logger->debug("fetched token '$token'");
        push @tokens, $token;
    }
    my $i;
    for ( $i = $#tokens; $i >= 0; $i-- ) {
        last if $tokens[$i] eq ';';
    }
    my $root = $fac->create_node;
    $tree->insert( $root );
    $self->_parse_node_data( $root, @tokens[ 0 .. ( $i - 1 ) ] );
    $self->_parse_clade( $tree, $root, @tokens[ 0 .. ( $i - 1 ) ] );
    return $tree;
}

sub _parse_clade {
    my ( $self, $tree, $root, @tokens ) = @_;
    $logger->debug("recursively parsing clade '@tokens'");
    my ( @clade, $depth, @remainder );
    TOKEN: for my $i ( 0 .. $#tokens ) {
        if ( $tokens[$i] eq '(' ) {
            if ( not defined $depth ) {
                $depth = 1;
                next TOKEN;
            }
            else {
                $depth++;
            }
        }
        elsif ( $tokens[$i] eq ',' && $depth == 1 ) {
            my $node = $fac->create_node;
            $root->set_child( $node );
            $tree->insert( $node );
            $self->_parse_node_data( $node, @clade );
            $self->_parse_clade( $tree, $node, @clade );
            @clade = ();
            next TOKEN;
        }
        elsif ( $tokens[$i] eq ')' ) {
            $depth--;
            if ( $depth == 0 ) {
                @remainder = @tokens[ ( $i + 1 ) .. $#tokens ];
                my $node = $fac->create_node;
                $root->set_child( $node );
                $tree->insert( $node );
                $self->_parse_node_data( $node, @clade );
                $self->_parse_clade( $tree, $node, @clade );
                last TOKEN;
            }
        }
        push @clade, $tokens[$i];
    }
}

sub _parse_node_data {
    my ( $self, $node, @clade ) = @_;
    $logger->debug("parsing name and branch length for node");
    my @tail;
    PARSE_TAIL: for ( my $i = $#clade; $i >= 0; $i-- ) {
        if ( $clade[$i] eq ')' ) {
            @tail = @clade[ ( $i + 1 ) .. $#clade ];
            last PARSE_TAIL;
        }
        elsif ( $i == 0 ) {
            @tail = @clade;
        }
    }
    # name only
    if ( scalar @tail == 1 ) {
        $node->set_name( $tail[0] );
    }
    elsif ( scalar @tail == 2 ) {
        $node->set_branch_length( $tail[-1] );
    }
    elsif ( scalar @tail == 3 ) {
        $node->set_name( $tail[0] );
        $node->set_branch_length( $tail[-1] );
    }
}

sub _next_token {
    my ( $self, $string ) = @_;
    $logger->debug("tokenizing string '$string'");
    my $QUOTED = 0;
    my $token = '';
    my $TOKEN_DELIMITER = qr/[():,;]/;
    TOKEN: for my $i ( 0 .. length( $string ) ) {
        $token .= substr($string,$i,1);
        $logger->debug("growing token: '$token'");
        if ( ! $QUOTED && $token =~ $TOKEN_DELIMITER ) {
            my $length = length( $token );
            if ( $length == 1 ) {
                $logger->debug("single char token: '$token'");
                return $token, substr($string,($i+1));
            }
            else {
                $logger->debug(sprintf("range token: %s", substr($token,0,$length-1)));
                return substr($token,0,$length-1),substr($token,$length-1,1).substr($string,($i+1));
            }
        }
        if ( ! $QUOTED && substr($string,$i,1) eq "'" ) {
            $QUOTED++;
        }
        elsif ( $QUOTED && substr($string,$i,1) eq "'" && substr($string,$i,2) ne "''" ) {
            $QUOTED--;
        }        
    }
}

=begin comment

 Type    : Internal method.
 Title   : _nodelabels($string)
 Usage   : my $labelled = $newick->_nodelabels($string);
 Function: Returns a newick string with labelled nodes
 Returns : SCALAR = a labelled newick tree description
 Args    : $string = a newick tree description
 Notes   : Node labels are now optional, determined by the -labels => 1 switch.

=end comment

=cut

sub _nodelabels {
    my ( $self, $string ) = @_;
    my ( $x, @x );
    while ( $string =~ /\)[:|,|;|\)]/o ) {
        foreach ( split( /[:|,|;|\)]/o, $string ) ) {
            if (/n([0-9]+)/) {
                push( @x, $1 );
            }
        }
        @x = sort { $a <=> $b } @x;
        $x = $x[-1];
        $string =~ s/(\))([:|,|;|\)])/$1.'n'.++$x.$2/ose;
    }
    return $string;
}

=begin comment

 Type    : Internal method.
 Title   : _parse
 Usage   : my $labelled = $newick->_nodelabels($string);
 Function: Recursive newick parser function
 Returns : (Modifies caller's tree object)
 Args    : $substr (a newick subtree), $tree (a tree object),
           $parent (root of subtree)
 Notes   :

=end comment

=cut

sub _parse {
    my ( $substr, $tree, $parent ) = @_;
    my @clades;
    my ( $level, $token ) = ( 0, '' );
    for my $i ( 0 .. length($substr) ) {
        my $c = substr( $substr, $i, 1 );
        $level++ if $c eq '(';
        $level-- if $c eq ')';
        if ( !$level && $c eq ',' || $i == length($substr) ) {
            my ( $node, $clade ) = &_token_handler($token);
            if ($clade) {
                push( @clades, [ $node, $clade ] );
            }
            else {
                push( @clades, [$node] );
            }
            $token = '';
        }
        else {
            $token .= $c;
        }
    }
    $parent->set_first_daughter( $clades[0][0] )
      ->set_last_daughter( $clades[-1][0] );
    $clades[0][0]->set_parent($parent);
    $tree->insert( $clades[0][0] );
    &_parse( $clades[0][1], $tree, $clades[0][0] ) if $clades[0][1];
    for my $i ( 1 .. $#clades ) {
        $clades[$i][0]->set_parent($parent)
          ->set_previous_sister(
            $clades[ $i - 1 ][0]->set_next_sister( $clades[$i][0] ) );
        $tree->insert( $clades[$i][0] );
        &_parse( $clades[$i][1], $tree, $clades[$i][0] ) if $clades[$i][1];
    }
}

=begin comment

 Type    : Internal subroutine.
 Title   : _token_handler
 Usage   : my ( $node, $substring ) = &_token_handler($string);
 Function: Tokenizes current substring, instantiates node objects.
 Returns : L<Bio::Phylo::Forest::Node>, SCALAR substring
 Args    : $token (a newick subtree)
 Notes   :

=end comment

=cut

sub _token_handler {
    my $token = shift;
    my ( $node, $name, $clade );
    if ( $token =~ m/^\((.*)\)([^()]*)$/o ) {
        ( $clade, $name ) = ( $1, $2 );
    }
    else {
        $name = $token;
    }
    if ( $name =~ m/^([^:()]*?)\s*:\s*(.*)$/o ) {
        $node = $fac->create_node(
            '-name'          => $1,
            '-branch_length' => $2,
        );
    }
    else {
        $node = $fac->create_node( '-name' => $name, );
    }
    return $node, $clade;
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The newick parser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to parse newick strings.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
