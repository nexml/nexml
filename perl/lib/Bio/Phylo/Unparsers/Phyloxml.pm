# $Id: Mrp.pm 1235 2010-03-02 16:11:07Z rvos $
package Bio::Phylo::Unparsers::Phyloxml;
use strict;
use Bio::Phylo::IO ();
use XML::Twig;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw':objecttypes looks_like_object';
use Bio::Phylo::Util::Logger ':levels';
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::IO);

my $logger = Bio::Phylo::Util::Logger->new;

my $phyloxml_ns = 'http://www.phyloxml.org/1.10/terms#';

my $phyloxml_header = <<'HEADER';
<?xml version="1.0" encoding="UTF-8"?>
<phyloxml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd"
   xmlns="http://www.phyloxml.org">
HEADER

my %has_attribute = (
	'confidence' => 'type',
	'id'         => 'provider',
);

=head1 NAME

Bio::Phylo::Unparsers::Phyloxml - Serializer used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module turns a L<Bio::Phylo::Forest> object into a PhyloXML file. It is called by the L<Bio::Phylo::IO> facade, don't call it
directly.

=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $mrp = Bio::Phylo::Unparsers::Phyloxml->_new;
 Function: Initializes a Bio::Phylo::Unparsers::Phyloxml object.
 Returns : A Bio::Phylo::Unparsers::Phyloxml object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = shift;
    my $self  = {};
    if (@_) {
        my %opts = @_;
        foreach my $key ( keys %opts ) {
            my $localkey = uc $key;
            $localkey =~ s/-//;
            unless ( ref $opts{$key} ) {
                $self->{$localkey} = uc $opts{$key};
            }
            else {
                $self->{$localkey} = $opts{$key};
            }
        }
    }
    bless $self, $class;
    return $self;
}

=begin comment

 Type    : Wrapper
 Title   : _to_string
 Usage   : my $mrp_string = $mrp->_to_string;
 Function: Stringifies a matrix object into
           an MRP nexus formatted table.
 Alias   :
 Returns : SCALAR
 Args    : Bio::Phylo::Matrices::Matrix;

=end comment

=cut

sub _to_string {
    my $self = shift;
    my $proj = $self->{'PHYLO'};
    $logger->debug("serializing object $proj");
    
    my @trees;
    if ( looks_like_object $proj, _PROJECT_ ) {
    	$logger->debug("object is a project");
    	for my $forest ( @{ $proj->get_forests } ) {
    		push @trees, @{ $forest->get_entities };
    	}
    }
    elsif ( looks_like_object $proj, _FOREST_ ) {
    	$logger->debug("object is a forest");
    	push @trees, @{ $proj->get_entities };
    }
    elsif ( looks_like_object $proj, _TREE_ ) {
    	$logger->debug("object is a tree");
    	push @trees, $proj;
    }
    my $xml = $phyloxml_header;
    $xml .= $self->_tree_to_xml( $_ ) for @trees;
    $xml .= '</phyloxml>';
    
    # pretty printing
	my $twig = XML::Twig->new( 'pretty_print' => 'indented' );
	eval { $twig->parse($xml) };
	if ( $@ ) {
		throw 'API' => "Couldn't build xml: " . $@;
	}
	else {
		return $twig->sprint;
	}    
}

sub _name_to_xml {
	my ( $self, $obj ) = @_;
	if ( my $name = $obj->get_name ) {
		return sprintf('<name>%s</name>', $name);
	}
	return '';
}

sub _tree_to_xml {
	my ( $self, $tree ) = @_;
	my $rooted = $tree->is_rooted ? 'true' : 'false';
	my $xml = sprintf('<phylogeny rooted="%s">', $rooted);
	$xml .= $self->_name_to_xml( $tree );
	$xml .= $self->_node_to_xml( $tree->get_root );
	return $xml .= '</phylogeny>';
}

sub _node_to_xml {
	my ( $self, $node ) = @_;
	my $xml = '<clade>' . $self->_name_to_xml( $node );
	
	# branch length
	my $length = $node->get_branch_length;
	if ( defined $length ) {
		$xml .= '<branch_length>' . $length . '</branch_length>';
	}
	
	# annotations
	$xml .= $self->_meta_to_xml( $_ ) for @{ $node->get_meta };
	
	# taxon links
	if ( my $taxon = $node->get_taxon ) {
		$xml .= $self->_taxon_to_xml( $taxon );
	}
	
	# traverse nodes
	$xml .= $self->_node_to_xml( $_ ) for @{ $node->get_children };
	
	return $xml .= '</clade>';
}

sub _meta_to_xml {
	my ( $self, $meta ) = @_;
	my $fq_predicate = $meta->get_predicate;
	my $xml;
	if ( $fq_predicate =~ /^(.+?):(.+)$/ ) {
		my ( $pre, $predicate ) = ( $1, $2 );
		my $namespace = $meta->get_namespaces( $pre );
		if ( $namespace eq $phyloxml_ns ) {
			my $obj = $meta->get_object;
			$xml = "<${predicate}>";
			
			# object is a single, nested annotation
			if ( UNIVERSAL::can($obj,'_type') && $obj->_type == _META_ ) {
				if ( my $att = $has_attribute{$predicate} ) {
					my $inner_predicate = $obj->get_predicate;
					my $obj = $obj->get_object;
					$inner_predicate =~ s/^.+://;				
					$xml = "<${predicate} ${att}=\"${inner_predicate}\">${obj}";
					$logger->debug($xml);
				}
				else {					
					$xml .= $self->_meta_to_xml( $obj );					
				}
			}
			
			# object is an array of annotations
			elsif ( UNIVERSAL::isa($obj,'ARRAY') ) {
				for my $inner ( @{ $obj } ) {
					$xml .= $self->_meta_to_xml( $inner );
				}
			}
			else {
				$logger->debug("meta object is $obj");
				$xml .= $obj;
			}
			$xml .= "</${predicate}>";
		}
	}
	return $xml;
}

sub _taxon_to_xml {
	my ( $self, $taxon ) = @_;
	my $xml = '<taxonomy>';
	$xml .= $self->_meta_to_xml( $_ ) for @{ $taxon->get_meta };
	return $xml .= '</taxonomy>';
}

sub _datum_to_xml {
	my ( $self, $datum ) = @_;
	return '';
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The newick unparser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to create mrp matrices.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=item L<http://www.phyloxml.org>

To learn more about the PhyloXML standard, visit L<http://www.phyloxml.org>

=back

=head1 REVISION

 $Id: Mrp.pm 1235 2010-03-02 16:11:07Z rvos $

=cut

1;