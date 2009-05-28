package Bio::Phylo::Meta;
use Bio::Phylo::Listable;
use Bio::Phylo::Util::CONSTANT qw'_META_ looks_like_number';
use Bio::Phylo::Util::Exceptions 'throw';
use UNIVERSAL 'isa';
use vars qw'@ISA';
@ISA=qw'Bio::Phylo::Listable';

#Synopsis 
#
#use Bio::Phylo::Factory;
#my $fac = Bio::Phylo::Factory->new;
#
#my $proj = $fac->create_project->add_meta(
#    $fac->create_meta(
#        '-namespaces' => { 'cdao' => 'http://evolutionaryontology.org#' },
#        '-property'   => 'cdao:hasMeta',
#        '-content'    => $fac->create_meta(
#            '-namespaces' => { 'cdao' => 'http://evolutionaryontology.org#' },
#            '-property'   => 'cdao:hasUrl',
#            '-content'    => 'http://8ball.sdsc.edu:6666/treebase-web/PhyloWS/tree/TreeBASE:2602',
#        )
#    )
#);

{
    my @fields = \( my( %property, %content ) );
    my $TYPE_CONSTANT      = _META_;
    my $CONTAINER_CONSTANT = $TYPE_CONSTANT;

    sub new {
        return shift->SUPER::new( '-tag' => 'meta', @_ );
    }
    
    sub set_content {
        my ( $self, $content ) = @_;
        $content{ $self->get_id } = $content;
        if ( not ref $content ) {
            if ( $content =~ m|^http://| ) {
                $self->set_attributes( 'href' => $content );
                $self->set_attributes( 'xsi:type' => 'nex:ResourceMeta' );
                if ( my $prop = $self->get_attributes( 'property' ) ) {
                    $self->set_attributes( 'rel' => $prop );
                    $self->unset_attribute( 'property' );
                }
            }
            else {
                $self->set_attributes( 'content' => $content );
                $self->set_attributes( 'xsi:type' => 'nex:LiteralMeta' );            
                if ( looks_like_number $content ) {
                    if ( $content == int($content) && $content !~ /\./ ) {
                        $self->set_attributes( 'datatype' => 'xsd:integer' );
                    }
                    else {
                        $self->set_attributes( 'datatype' => 'xsd:float' );
                    }
                }
                else {
                    $self->set_attributes( 'datatype' => 'xsd:string' );
                }        
            }
        }
        else {
            if ( isa($content, 'Bio::Phylo') and $content->_type == $TYPE_CONSTANT ) {
                $self->insert($content);
                $self->set_attributes( 'xsi:type' => 'nex:ResourceMeta' );
                if ( my $prop = $self->get_attributes( 'property' ) ) {
                    $self->set_attributes( 'rel' => $prop );
                    $self->unset_attribute( 'property' );
                }                
            }
            else {
                $self->set_attributes( 'xsi:type' => 'nex:LiteralMeta' );
                $self->set_attributes( 'datatype' => 'rdf:Literal' );
                $self->insert(Bio::Phylo::Meta::XMLLiteral->new($content));
            }        
        }
        return $self;
    }
    
    sub set_property {
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
            throw 'BadString' => 'Not a valid CURIE';
        }
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
        my $obj = $$self;
        my $xml = '';

        # for RDF::Core::Model objects
        if ( isa($obj, 'RDF::Core::Model') ) {
            eval {
                require RDF::Core::Model::Serializer;
                my $serialized_model = '';
                my $serializer = RDF::Core::Model::Serializer->new(
                    'Model'  => $value,
                    'Output' => \$serialized_model,
                );   
                $xml = $serialized_model;     			
            };
            if ( $@ ) {
                throw 'API' => $@;
            }
        }         
        
        # for XML::XMLWriter object
        elsif ( isa($obj, 'XML::XMLWriter') ) {
            $xml = $obj->get;
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
        return $xml;        
    }
    sub _type { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }    
    sub _cleanup {}    
}
1;