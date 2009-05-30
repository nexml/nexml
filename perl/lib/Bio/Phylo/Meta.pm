package Bio::Phylo::Meta;
use Bio::Phylo::Listable;
use Bio::Phylo::Util::CONSTANT qw'_META_ looks_like_number';
use Bio::Phylo::Util::Exceptions 'throw';
use UNIVERSAL 'isa';
use vars qw'@ISA';
use strict;
@ISA=qw'Bio::Phylo::Listable';

#Synopsis 
#
# use Bio::Phylo::Factory;
# my $fac = Bio::Phylo::Factory->new;
#
# my $proj = $fac->create_project->add_meta(
#     $fac->create_meta(
#         '-namespaces' => { 'cdao' => 'http://evolutionaryontology.org#' },
#         '-triple'     => { 
#             'cdao:hasMeta' => $fac->create_meta(
#                 '-namespaces' => { 'cdao' => 'http://evolutionaryontology.org#' },
#                 '-triple'     => { 'cdao:hasUrl' => 'http://8ball.sdsc.edu:6666/treebase-web/PhyloWS/tree/TreeBASE:2602' }
#             )
#         }
#     )
# );

{
    my @fields = \( my( %property, %content ) );
    my $TYPE_CONSTANT      = _META_;
    my $CONTAINER_CONSTANT = $TYPE_CONSTANT;

    sub new { return shift->SUPER::new( '-tag' => 'meta', @_ ) }   
    
    my $set_content = sub {
        my ( $self, $content ) = @_;
        $content{ $self->get_id } = $content;
        my %resource = ( 'xsi:type' => 'nex:ResourceMeta' );
        my %literal  = ( 'xsi:type' => 'nex:LiteralMeta'  );
        if ( not ref $content ) {
            if ( $content =~ m|^http://| ) {
                $self->set_attributes( 'href' => $content, %resource );
                if ( my $prop = $self->get_attributes( 'property' ) ) {
                    $self->set_attributes( 'rel' => $prop );
                    $self->unset_attribute( 'property' );
                }
            }
            else {
                $self->set_attributes( 'content' => $content, %literal );            
                if ( looks_like_number $content ) {
                	my $dt = $content == int($content) && $content !~ /\./ ? 'integer' : 'float';
                	$self->set_attributes( 'datatype' => 'xsd:' . $dt );
                }
                else {
                    $self->set_attributes( 'datatype' => 'xsd:string' );
                }        
            }
        }
        else {
            if ( isa($content, 'Bio::Phylo') and $content->_type == $TYPE_CONSTANT ) {
                $self->insert($content)->set_attributes( %resource );
                if ( my $prop = $self->get_attributes( 'property' ) ) {
                    $self->set_attributes( 'rel' => $prop );
                    $self->unset_attribute( 'property' );
                }                
            }
            else {
                $self->set_attributes( 'datatype' => 'rdf:XMLLiteral', %literal );
                $self->insert(Bio::Phylo::Meta::XMLLiteral->new($content));
            }        
        }
        return $self;
    };
    
    my $set_property = sub {
        my ( $self, $property ) = @_;
        if ( $property =~ m/^([a-zA-Z0-9_]+):([a-zA-Z0-9_]+)$/ ) {
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
    
    sub set_triple {
        my ( $self, $property, $content ) = @_;
        if ( ref($property) && ref($property) eq 'HASH' ) {
            ( $property, $content ) = each %{ $property };
        }
        $set_property->( $self, $property );
        $set_content->( $self, $content );
        return $self;
    }    
    
    sub get_content { $content{ shift->get_id } }    
    
    sub get_property { $property{ shift->get_id } }
    
    sub _type { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }    
    sub _cleanup {
        my $id = shift->get_id;
        delete $_->{$id} for @fields;
    }    

}

package Bio::Phylo::Meta::XMLLiteral;
use Bio::Phylo::Util::CONSTANT qw'_META_';
use Bio::Phylo::Util::Exceptions 'throw';
use UNIVERSAL qw'isa can';
{
    my $TYPE_CONSTANT      = _META_;
    my $CONTAINER_CONSTANT = $TYPE_CONSTANT;
    sub new {
        my ( $class, $obj ) = @_;
        return bless \$obj, $class;
    }
    sub to_xml {
        my $self = shift;
        my $objs = $$self;
        my @objs = ref($objs) eq 'ARRAY' ? @{ $objs } : ( $objs );
        my $xml = '';
		for my $obj ( @objs ) {
	        # for RDF::Core::Model objects
	        if ( isa($obj, 'RDF::Core::Model') ) {
	            eval {
	                require RDF::Core::Model::Serializer;
	                my $serialized_model = '';
	                my $serializer = RDF::Core::Model::Serializer->new(
	                    'Model'  => $obj,
	                    'Output' => \$serialized_model,
	                );   
	                $xml .= $serialized_model;     			
	            };
	            if ( $@ ) {
	                throw 'API' => $@;
	            }
	        }	        
	        # for XML::XMLWriter object
	        elsif ( isa($obj, 'XML::XMLWriter') ) {
	            $xml .= $obj->get;
	        }	        
	        else {
	            # duck-typing
	            # Bio::Phylo => to_xml, XML::DOM,XML::GDOME,XML::LibXML => toString, XML::Twig => sprint
	            # XML::DOM2 => xmlify, XML::DOMBacked => as_xml,
	            # XML::Handler => dump_tree, XML::Element => as_XML
	            # XML::API => _as_string, XML::Code => code	            
	            my @methods = qw(to_xml toString sprint _as_string code xmlify as_xml dump_tree as_XML);
	            SERIALIZER: for my $method ( @methods ) {
	                if ( can($obj,$method) ) {
	                    $xml .= $obj->$method;
	                    last SERIALIZER;
	                }
	            }
	        }
		}
        return $xml;        
    }
    sub _type { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }    
    sub _cleanup {}    
}
1;