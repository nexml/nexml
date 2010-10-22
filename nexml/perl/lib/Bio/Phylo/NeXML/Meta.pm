package Bio::Phylo::NeXML::Meta;
use strict;
use Bio::Phylo::Listable ();
use Bio::Phylo::Util::CONSTANT qw(
    _DOMCREATOR_
    _META_
    looks_like_number
    looks_like_instance
    looks_like_object
);
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Factory;
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::Listable);

{
    my $fac = Bio::Phylo::Factory->new;
    my @fields = \( my( %property, %content ) );
    my $TYPE_CONSTANT      = _META_;
    my $CONTAINER_CONSTANT = $TYPE_CONSTANT;
    
=head1 NAME

Bio::Phylo::NeXML::Meta - Single predicate/object annotation, attached to an
xml-writable subject

=head1 SYNOPSIS

 use Bio::Phylo::Factory;
 my $fac = Bio::Phylo::Factory->new;
 my $url = 'http://purl.org/phylo/treebase/phylows/study/TB2:S1787';
 my $proj = $fac->create_project->add_meta(
     $fac->create_meta(
         '-namespaces' => { 'cdao' => 'http://evolutionaryontology.org#' },
         '-triple'     => { 
             'cdao:hasMeta' => $fac->create_meta(
                 '-namespaces' => { 'cdao' => 'http://evolutionaryontology.org#' },
                 '-triple'     => { 'cdao:has_External_Reference' => $url }
             )
         }
     )
 );

=head1 DESCRIPTION

To comply with the NeXML standard (L<http://www.nexml.org>), Bio::Phylo
implements metadata annotations which consist conceptually of RDF triples where
the subject is a container object that subclasses
L<Bio::Phylo::NeXML::Writable>, and the predicate and object are defined in
this class. 

The objects of the triples provided by this class can be of any simple type
(string, number) or one of L<XML::DOM>, L<XML::GDOME>, L<XML::LibXML>,
L<XML::Twig>, L<XML::DOM2>, L<XML::DOMBacked>, L<XML::Handler>, L<XML::Element>,
L<XML::API>, L<XML::Code> or L<XML::XMLWriter> or L<RDF::Core::Model>.

When serialized, the Bio::Phylo::NeXML::Meta object in NeXML is typically written out
as an element called 'meta', with RDFa compliant attributes.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

 Type    : Constructor
 Title   : new
 Usage   : my $anno = Bio::Phylo::NeXML::Meta->new;
 Function: Initializes a Bio::Phylo::NeXML::Meta object.
 Returns : A Bio::Phylo::NeXML::Meta object.
 Args    : optional constructor arguments are key/value
 		   pairs where the key corresponds with any of
 		   the methods that starts with set_ (i.e. mutators) 
 		   and the value is the permitted argument for such 
 		   a method. The method name is changed such that,
 		   in order to access the set_value($val) method
 		   in the constructor, you would pass -value => $val

=cut    

    sub new { return shift->SUPER::new( '-tag' => 'meta', @_ ) }   
    
    my $set_content = sub {
        my ( $self, $content ) = @_;
        my $predicateName = 'property';
        $content{ $self->get_id } = $content;
        my %resource = ( 'xsi:type' => 'nex:ResourceMeta' );
        my %literal  = ( 'xsi:type' => 'nex:LiteralMeta'  );
        if ( not ref $content ) {
            if ( $content =~ m|^http://| ) {
                $self->set_attributes( 'href' => $content, %resource );
                if ( my $prop = $self->get_attributes( 'property' ) ) {
                    $self->set_attributes( 'rel' => $prop );
                    $self->unset_attribute( 'property' );
                    $predicateName = 'rel';
                }
            }
            else {
                $self->set_attributes( 'content' => $content, %literal );            
                if ( looks_like_number $content ) {
                	my $dt = $content == int($content) && $content !~ /\./ ? 'integer' : 'float';
                	$self->set_attributes( 'datatype' => 'xsd:' . $dt );
                }
				elsif ( $content eq 'true' or $content eq 'false' ) {
							$self->set_attributes( 'datatype' => 'xsd:boolean' );
				}
                else {
                    $self->set_attributes( 'datatype' => 'xsd:string' );
                }        
            }
        }
        else {
            if ( looks_like_instance $content, 'Bio::Phylo' and $content->_type == $TYPE_CONSTANT ) {
                $self->insert($content)->set_attributes( %resource );
                if ( my $prop = $self->get_attributes( 'property' ) ) {
                    $self->set_attributes( 'rel' => $prop );
                    $self->unset_attribute( 'property' );
                    $predicateName = 'rel';
                }                
            }
            elsif ( looks_like_instance $content, 'DateTime' ) {
            	$self->set_attributes( 
            		'content'  => $content->iso8601(), 
            		'datatype' => 'xsd:date',
            		%literal 
            	);
            }
            else {
                $self->set_attributes( 'datatype' => 'rdf:XMLLiteral', %resource );
                $self->insert( $fac->create_xmlliteral($content) );
				$self->unset_attribute( 'content' );
            }        
        }
        $property{ shift->get_id } = $predicateName;
        return $self;
    };
    
    my $set_property = sub {
        my ( $self, $property ) = @_;
        if ( $property =~ m/^([a-zA-Z_]+):([a-zA-Z0-9_\-\.]+)$/ ) {
            my ( $prefix, $prop ) = ( $1, $2 );
            if ( $self->get_namespaces( $prefix ) ) {
                $self->set_attributes( 'property' => $property );
            }
            else {
                throw 'BadArgs' => "Prefix $prefix not bound to a namespace";
            }
        }
        else {
            throw 'BadString' => "$property is not a valid CURIE";
        }
    };   
    
=back

=head2 MUTATORS

=over

=item set_triple()

Populates the triple, assuming that the invocant is attached to a subject.

 Type    : Mutator
 Title   : set_triple
 Usage   : $meta->set_triple( $predicate, $object );
 Function: Populates the triple.
 Returns : Modified object.
 Args    : $predicate - a CURIE whose namespace prefix must 
                        have been bound previously using 
                        $meta->set_namespaces( $prefix, $uri );
           $object    - any of the valid object types: a number,
                        a string, a url, a nested annotation
                        or anything that can be adapted by
                        Bio::Phylo::NeXML::Meta::XMLLiteral 

=cut    
    
    sub set_triple {
        my ( $self, $property, $content ) = @_;
        if ( ref($property) && ref($property) eq 'HASH' ) {
            ( $property, $content ) = each %{ $property };
        }
        $set_property->( $self, $property );
        $set_content->( $self, $content );
        return $self;
    }    
    
=back

=head2 ACCESSORS

=over

=item get_object()

Returns triple object

 Type    : Accessor
 Title   : get_object
 Usage   : my $val = $anno->get_object;
 Function: Returns triple object
 Returns : A triple object
 Args    : NONE

=cut    
    
    sub get_object { $content{ shift->get_id } }    
    
=item get_predicate()

Returns triple predicate

 Type    : Accessor
 Title   : get_predicate
 Usage   : my $val = $anno->get_predicate;
 Function: Returns triple predicate
 Returns : A triple predicate
 Args    : NONE

=cut     
    
    sub get_predicate { 
    	my $self = shift;
    	my $predicateName = $property{ $self->get_id };
    	return $self->get_attributes->{$predicateName};
    }

=back

=head2 SERIALIZERS

=over

=item to_dom()

 Type    : Serializer
 Title   : to_dom
 Usage   : $obj->to_dom
 Function: Generates a DOM subtree from the invocant and
           its contained objects
 Returns : a DOM element object (default: XML::Twig flavor)
 Args    : DOM factory object
 Note    : This is the generic function. It is redefined in the 
           classes below.
=cut

    sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= Bio::Phylo::NeXML::DOM->get_dom;
		if ( looks_like_object $dom, _DOMCREATOR_ ) {
			my $elt = $self->get_dom_elt($dom);
			if ( $self->can('get_entities') ) {
				for my $ent ( @{ $self->get_entities } ) {
				if ( looks_like_implementor $ent,'to_dom' ) { 
					$elt->set_child( $ent->to_dom($dom) );
				}
				}
			}
			return $elt;                
		}
		else {
			throw 'BadArgs' => 'DOM factory object not provided';
		}	
    }

    
=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Dictionary>

Annotation objects are combined into a dictionary.

=item L<Bio::Phylo::NeXML::Writable>

This object inherits from L<Bio::Phylo::NeXML::Writable>, so methods
defined there are also applicable here.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Annotation.pm 1040 2009-05-28 04:26:49Z rvos $

=cut    
    
    sub _type { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }    
    sub _cleanup {
        my $id = shift->get_id;
        delete $_->{$id} for @fields;
    }    

}

1;