package Bio::Phylo::Annotation;
use strict;
use Bio::Phylo::Util::XMLWritable;
use Bio::Phylo::Util::CONSTANT qw(_ANNOTATION_ _DICTIONARY_);
use vars '@ISA';
use UNIVERSAL qw'can isa';
use Bio::Phylo::Util::Exceptions 'throw';
@ISA=qw(Bio::Phylo::Util::XMLWritable);
{
    my @fields = \( my ( %key, %datatype, %value ) );
    my $TYPE_CONSTANT = _ANNOTATION_;
    my $CONTAINER_CONSTANT = _DICTIONARY_;
    
    sub new {
        return shift->SUPER::new( '-tag' => 'string', @_ );     
    }
    
    sub set_value {
        my ( $self, $value ) = @_;
        $value{ $self->get_id } = $value;
    }
    
    sub get_value {
        my $self = shift;
        return $value{ $self->get_id };
    }
    
    sub to_xml {
        my $self = shift;
        my $key = $self->get_xml_id;
        my $xml = '';
        my $value = $self->get_value;
        if ( ref($value) ) {

        	# for RDF::Core::Model objects
        	if ( isa($value, 'RDF::Core::Model') ) {
        		eval {
        			require RDF::Core::Model::Serializer;
        			my $serialized_model = '';
  					my $serializer = RDF::Core::Model::Serializer->new(
  						'Model'  => $value,
                        'Output' => \$serialized_model,
                        # BaseURI => 'URI://BASE/',
                    );   
                    $value = $serialized_model;     			
        		};
        		if ( $@ ) {
        			throw 'API' => $@;
        		}
        	}         
        	
        	# for XML::XMLWriter object
        	elsif ( isa($value, 'XML::XMLWriter') ) {
        		$value = $value->get;
        	}
        	
        	else {
        		my $concatenated = '';
        		my @values;
        		if ( ref($value) eq 'ARRAY' ) {
        			@values = @{ $value };
        		}
        		else {
        			push @values, $value;
        		}
        		for my $v ( @values ) {
	        		# duck-typing
		        	# Bio::Phylo => to_xml, XML::DOM,XML::GDOME => toString, XML::Twig => sprint
		        	# XML::DOM2 => xmlify, XML::DOMBacked => as_xml,
		        	# XML::Handler => dump_tree, XML::Element => as_XML
		        	# XML::API => _as_string, XML::Code => code
	
		        	my @methods = qw(to_xml toString sprint _as_string code xmlify as_xml dump_tree as_XML);
		        	SERIALIZER: for my $method ( @methods ) {
		        		if ( can($v,$method) ) {
		        			$concatenated .= $v->$method;
		        			last SERIALIZER;
		        		}
		        	}
        		}
        		$value = $concatenated;
        	}
        }
        my $type = $self->get_tag;
        $xml .= "<${type} id=\"${key}\">" . $value . "</${type}>";
        return $xml;
    }
    
    sub _type { $TYPE_CONSTANT }
    sub _container { $CONTAINER_CONSTANT }
    sub _cleanup {
        my $self = shift;
        my $id = $self->get_id;
        for my $field ( @fields ) {
            delete $field->{$id};
        }
    }    
}
1;