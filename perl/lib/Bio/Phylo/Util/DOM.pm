#$Id$

# _to_dom() definitions for classes having _to_xml() methods
# for producing XML::LibXML::Element instances suitable for 
# manipulation in the XML::LibXML DOM

# do logging for all events

package Bio::Phylo::Util::Dom;
use strict;
use Bio::Phylo::Util::Exceptions qw( throw );

eval { require XML::LibXML };
if ($@) {
    throw 'ExtensionError' => "Error loading the XML::LibXML extension: $@";
}

=head1 NAME

Bio::Phylo::Util::DOM - Drop-in XML DOM support for C<Bio::Phylo> via C<XML::LibXML>

=head1 SYNOPSIS

=head1 DESCRIPTION

This module adds C<to_dom> methods to C<Bio::Phylo::Util::XMLWritable> classes, which provide NeXML-valid XML::LibXML objects for document object model manipulation by means of the C<XML::LibXML> package. For any C<XMLWritable> object, use C<to_dom> in place of C<to_xml> to create DOM nodes.

The C<dom()> method is also added to the C<Bio::Phylo::Project> class. It returns a NeXML document DOM object ( C<XML::LibXML::Document> ) populated by the current contents of the C<Project> object.

=head1 AUTHOR

Mark A. Jensen  (maj -at- fortinbras -dot- us)

=head1 TODO

The C<Bio::Phylo::Annotation> class is not yet DOMized.

=cut

1;

=head1 METHODS

=cut

package Bio::Phylo::Util::XMLWritable;
use strict;
use XML::LibXML;

{

=over

=item  Bio::Phylo::Util::XMLWritable::get_dom_elt()

Analog to get_xml_tag.

 Type    : Serializer
 Title   : get_dom_elt
 Usage   : $obj->get_dom_elt
 Function: Generates a DOM element from the invocant
 Returns : an XML::LibXML::Element object
 Args    : none

=cut

    sub get_dom_elt {
	my $self = shift;
	my $elt = XML::LibXML::Element->new($self->get_tag);
	my %attrs = %{ $self->get_attributes };
	for my $key ( keys %attrs ) {
	    $elt->setAttribute( $key => $attrs{$key} );
	}

	my $dictionaries = $self->get_dictionaries;
	if ( @{ $dictionaries } ) {
	    $elt->addChild( $_->to_dom ) for @{ $dictionaries };
	}
	if ( UNIVERSAL::can($self,'get_sets') ) {
	    my $sets = $self->get_sets;
	    $elt->addChild( $_->to_dom ) for @{ $sets };
	}
	return $elt;
    }

=item  Bio::Phylo::Util::XMLWritable::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $obj->to_dom
 Function: Generates a DOM subtree from the invocant and
           its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none
 Note    : This is the generic function. It is redefined in the 
           classes below.
=cut

    sub to_dom {
	my $self = shift;
	my $elt = $self->get_dom_elt;
	if ( $self->can('get_entities') ) {
	    for my $ent ( @{ $self->get_entities } ) {
		if ( UNIVERSAL::can($ent,'to_dom') ) { 
		    $elt->addChild( $ent->to_dom );
		}
	    }
	}
	return $elt;
    }

}

1;

package Bio::Phylo::Forest::Node;
use strict;
use XML::LibXML;
use XML::LibXML::NodeList;

{

=item  Bio::Phylo::Forest::Node::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $node->to_dom
 Function: Generates an array of DOM elements from the invocant's
           descendants
 Returns : an array of  XML::LibXML::Element objects, or 
           an XML::LibXML::NodeList of elements
 Args    : pass 1 to retrieve elements as a NodeList

=cut

    sub to_dom {
	my $self = shift;
	my $as_node_list = shift;
	my @nodes = ( $self, @{ $self->get_descendants } );
	my @elts;
	# first write out the node elements
	for my $node ( @nodes ) {
	    if ( my $taxon = $node->get_taxon ) {
		$node->set_attributes( 'otu' => $taxon->get_xml_id );
	    }
	    if ( $node->is_root ) {
		$node->set_attributes( 'root' => 'true' );
	    }
	    push @elts, $node->get_dom_elt;
	    
	}
	
	# then the rootedge?
	if ( my $length = shift(@nodes)->get_branch_length ) {
	    my $target = $self->get_xml_id;
	    my $id = "edge" . $self->get_id;
	    my $elt = XML::LibXML::Element->new('rootedge');
	    $elt->setAttribute('target' => $target);
	    $elt->setAttribute('id' => $id);
	    $elt->setAttribute('length' => $length);
	    push @elts, $elt;
	}
	
	# then the subtended edges
	for my $node ( @nodes ) {
	    my $source = $node->get_parent->get_xml_id;
	    my $target = $node->get_xml_id;
	    my $id     = "edge" . $node->get_id;
	    my $length = $node->get_branch_length;
	    my $elt = XML::LibXML::Element->new('edge');
	    $elt->setAttribute('source' => $source);
	    $elt->setAttribute('target' => $target);
	    $elt->setAttribute('id' => $id);
	    $elt->setAttribute('length' => $length) if ( defined $length );
	    push @elts, $elt;
	}
	if ($as_node_list) {
	    my $l = XML::LibXML::NodeList->new();
	    $l->push(@elts);
	    return $l;
	}
	else {
	    return @elts; 
	}
    }
    
}

1;

package Bio::Phylo::Forest::Tree;
use strict;
use XML::LibXML;

{

=item  Bio::Phylo::Forest::Tree::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $tree->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none

=cut

    sub to_dom {
	my $self = shift;
	my $xsi_type = 'nex:IntTree';
	for my $node ( @{ $self->get_entities } ) {
	    my $length = $node->get_branch_length;
	    if ( defined $length and $length !~ /^[+-]?\d+$/ ) {
		$xsi_type = 'nex:FloatTree';
	    }
	}
	$self->set_attributes( 'xsi:type' => $xsi_type );
	my $elt = $self->get_dom_elt;
	if ( my $root = $self->get_root ) {
	    $elt->addChild( $_ ) for $root->to_dom;
	}
	return $elt;
    }
}

1;

package Bio::Phylo::Matrices::Datatype;
use strict;
use XML::LibXML;
{

=item  Bio::Phylo::Matrices::Datatype::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $type->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none

=cut

    sub to_dom {
	my $self = shift;	
	my $elt;
	my $normalized   = $_[0] || {};
	my $polymorphism = $_[1];
	if ( my $lookup  = $self->get_lookup ) {
	    $elt = $self->get_dom_elt;
	    my $id_for_state = $self->get_ids_for_states;
	    my @states = sort  { $id_for_state->{$a} <=> $id_for_state->{$b} } 
	    keys %{ $id_for_state };
	    my $max_id = 0;
	    for my $state ( @states ) {
		my $state_id = $id_for_state->{ $state };
		$id_for_state->{ $state } = 's' . $state_id;
		$max_id = $state_id;
	    }
	    for my $state ( @states ) {
		$elt->addChild( 
		    $self->_state_to_dom(
			$state, 
			$id_for_state, 
			$lookup, 
			$normalized, 
			$polymorphism 
 		    )
		    );
	    }
	    my ( $missing, $gap ) = ( $self->get_missing, $self->get_gap );
	    my $special = $self->get_ids_for_special_symbols;
	    if ( %{ $special } ) {
		my $uss;
		$uss = XML::LibXML::Element->new('uncertain_state_set');
		$uss->setAttribute( 'id' => 's'.$special->{$gap} );
		$uss->setAttribute( 'symbol' => '-' );
		$elt->addChild($uss);
		$uss = XML::LibXML::Element->new('uncertain_state_set');
		$uss->setAttribute( 'id' => 's'.$special->{$missing} );
		$uss->setAttribute( 'symbol' => '?' );
		my $mbr;
		for (@states) {
		    $mbr = XML::LibXML::Element->new('member');
		    $mbr->setAttribute( 'state' => $id_for_state->{$_} );
		    $uss->addChild($mbr);
		}
		$mbr = XML::LibXML::Element->new('member');
		$mbr->setAttribute( 'state' => 's'.$special->{$gap} );
		$uss->addChild($mbr);
		$elt->addChild($uss);
	    }		
	    
	}	
	return $elt;
    }

    sub _state_to_dom {
	my ( $self, $state, $id_for_state, $lookup, $normalized, $polymorphism ) = @_;
        my $state_id = $id_for_state->{ $state };
        my @mapping = @{ $lookup->{$state} };
        my $symbol = exists $normalized->{$state} ? $normalized->{$state} : $state;
	my $elt;
	
        # has ambiguity mappings
        if ( scalar @mapping > 1 ) {
            my $tag = $polymorphism ? 'polymorphic_state_set' : 'uncertain_state_set';

	    $elt = XML::LibXML::Element->new($tag);
	    $elt->setAttribute( 'id' => $state_id );
	    $elt->setAttribute( 'symbol' => $symbol );
            for my $map ( @mapping ) {
		my $mbr = XML::LibXML::Element->new('member');
		$mbr->setAttribute('state' => $id_for_state->{ $map } );
		$elt->addChild($mbr);
            }
	    
        }
        
        # no ambiguity
        else {
	    $elt = XML::LibXML::Element->new('state');
	    $elt->setAttribute( 'id' => $state_id );
	    $elt->setAttribute( 'symbol' => $symbol ); 
        }
	return $elt;
    }
}

1;

package Bio::Phylo::Matrices::Datum;
use strict;
use Bio::Phylo::Util::CONSTANT qw(:objecttypes looks_like_number looks_like_hash);
use XML::LibXML;
{

=item  Bio::Phylo::Matrices::Datum::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $datum->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none

=cut

    sub to_dom {
	my $self = shift;
	my %args = looks_like_hash @_;

	my $char_ids  = $args{'-chars'};
	my $state_ids = $args{'-states'};
	my $special   = $args{'-special'};
	if ( my $taxon = $self->get_taxon ) {
	    $self->set_attributes( 'otu' => $taxon->get_xml_id );
	}
	my @char = $self->get_char;
	my ( $missing, $gap ) = ( $self->get_missing, $self->get_gap );

	my $elt = $self->get_dom_elt;

	if ( not $args{'-compact'} ) {
	    for my $i ( 0 .. $#char ) {
		if ( $missing ne $char[$i] and $gap ne $char[$i] ) {
		    my ( $c, $s );
		    if ( $char_ids and $char_ids->[$i] ) {
			$c = $char_ids->[$i];
		    }
		    else {
			$c = $i;
		    }
		    if ( $state_ids and $state_ids->{uc $char[$i]} ) {
			$s = $state_ids->{uc $char[$i]};
		    }
		    else {
			$s = uc $char[$i];
		    }
		    my $cell_elt = XML::LibXML::Element->new('cell'); 
		    $cell_elt->setAttribute( 'char' => $c );
		    $cell_elt->setAttribute('state' => $s ); 
		    $elt->addChild($cell_elt);
		}
		elsif ( $missing eq $char[$i] or $gap eq $char[$i] ) {
		    my ( $c, $s );
		    if ( $char_ids and $char_ids->[$i] ) {
			$c = $char_ids->[$i];
		    }
		    else {
			$c = $i;
		    }
		    if ( $special and $special->{$char[$i]} ) {
			$s = $special->{$char[$i]};
		    }
		    else {
			$s = $char[$i];
		    }
		    my $cell_elt = XML::LibXML::Element->new('cell');
		    $cell_elt->setAttribute('char' => $c);
		    $cell_elt->setAttribute( 'state' => $s );
		    $elt->addChild($cell_elt);
		    
		}
	    }
	}
	else {
	    my @tmp = map { uc $_ } @char;
	    my $seq = $self->get_type_object->join(\@tmp);
	    my $seq_elt = XML::LibXML::Element->new('seq');
	    $seq_elt->addChild( XML::LibXML::Text->new($seq) );
	    $elt->addChild($seq_elt);
	}
	return $elt;
    }

}

1;

package Bio::Phylo::Matrices::Matrix;
use strict;
use XML::LibXML;
{

=item  Bio::Phylo::Matrices::Matrix::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $matrix->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : Optional:
           -compact => 1 : renders characters as sequences,
                           not individual cells

=cut

    sub to_dom {
	my $self = shift;		
	my ( %args, $ids_for_states );
	if ( @_ ) {
	    %args = @_;
	}
	my $type = $self->get_type;
	my $verbosity = $args{'-compact'} ? 'Seqs' : 'Cells';
	my $xsi_type = 'nex:' . ucfirst($type) . $verbosity;
	$self->set_attributes( 'xsi:type' => $xsi_type );
	my $elt = $self->get_dom_elt;
	my $normalized = $self->_normalize_symbols;
	
	# the format block
 	my $format_elt = XML::LibXML::Element->new('format');
	my $to = $self->get_type_object;
	$ids_for_states = $to->get_ids_for_states(1);
	
	# write state definitions

	$format_elt->addChild( $to->to_dom( $normalized, $self->get_polymorphism ) );
	
	# write column definitions
	$format_elt->addChild($_) for $self->_package_char_labels( %{ $ids_for_states } ? $to->get_xml_id : undef );

	$elt->addChild($format_elt);

	# the matrix block

	my $mx_elt = XML::LibXML::Element->new('matrix');
	my @char_ids;
	for ( 0 .. $self->get_nchar ) {
	    push @char_ids, 'c' . ($_+1);
	}
	
	# write rows
	my $special = $self->get_type_object->get_ids_for_special_symbols(1);
	for my $row ( @{ $self->get_entities } ) {
	    # $row->to_dom is calling ...::Datum::to_dom...
	    $mx_elt->addChild( 
		$row->to_dom(
		    '-states'  => $ids_for_states,
		    '-chars'   => \@char_ids,
		    '-symbols' => $normalized,
		    '-special' => $special,
		    %args,
		)
		);
	}
	$elt->addChild($mx_elt);
	return $elt;
    }

    # returns an array of elements
    sub _package_char_labels {
	my ( $self, $states_id ) = @_;
	my @elts;
	my $labels = $self->get_charlabels;
	for my $i ( 1 .. $self->get_nchar ) {
	    my $char_id = 'c' . $i;
	    my $label   = $labels->[ $i - 1 ];
	    my $elt = XML::LibXML::Element->new('char');
	    $elt->setAttribute( 'id' => $char_id );
	    $elt->setAttribute( 'label' => $label ) if $label;
	    $elt->setAttribute( 'states' => $states_id ) if $states_id;
	    push @elts, $elt;
	}	
	return @elts;
    }
}

1;

package Bio::Phylo::Project;
use strict;
use XML::LibXML;
{

=item  Bio::Phylo::Project::dom

 Type    : Serializer
 Title   : dom
 Usage   : $proj->dom
 Function: Creates an XML::LibXML::Document, containing the 
           present state of the project by default
 Returns : an XML::LibXML::Document object
 Args    : pass 1 to obtain a document node without 
           content

=cut

    sub dom {
	my $self = shift;
	my $empty = shift;
	my $dom = XML::LibXML::Document->new();
	my $root;

	unless ($empty) {
	    $root = $self->to_dom;
	    $dom->setDocumentElement($root);
	}
	
	return $dom;
    }

=item  Bio::Phylo::Project::doc

 Type    : Alias
 Title   : doc
 Usage   : $proj->doc
 Function: aliases doc()
 Returns : an XML::LibXML::Document object
 Args    : pass 1 to obtain a document node without 
           content

=cut

sub doc { shift->dom(shift()) }

=item  Bio::Phylo::Project::to_dom

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $node->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an XML::LibXML::Element object
 Args    : none

=back

=cut

    sub to_dom {
	my $self = shift;
	my $elt = $self->get_dom_elt;

	my @linked = ( @{ $self->get_forests }, @{ $self->get_matrices } );
	my %taxa = map { $_->get_id => $_ } @{ $self->get_taxa }, map { $_->make_taxa } @linked;
	for ( values %taxa, @linked ) {
	    $elt->addChild( $_->to_dom(@_) );
	}
	return $elt;
    }
}

1;

### TODO: Annotations are harder (i.e., I have to learn stuff)

package Bio::Phylo::Annotation;
use strict;
use XML::LibXML;
1;

