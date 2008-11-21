package Bio::Phylo::Annotation;
use strict;
use Bio::Phylo::Util::XMLWritable;
use Bio::Phylo::Util::CONSTANT qw(_ANNOTATION_ _DICTIONARY_);
use vars '@ISA';
use UNIVERSAL 'can';
@ISA=qw(Bio::Phylo::Util::XMLWritable);
{
    my @fields = \( my ( %key, %datatype, %value ) );
    my $TYPE_CONSTANT = _ANNOTATION_;
    my $CONTAINER_CONSTANT = _DICTIONARY_;
    
    sub new {
        return shift->SUPER::new( '-tag' => 'key', @_ );     
    }
    
    sub set_key {
        my $self = shift;
        $key{ $self->get_id } = shift;
        return $self;
    }
    
    sub set_type {
        my $self = shift;
        $datatype{ $self->get_id } = shift;
        return $self;
    }
    
    sub set_value {
        my $self = shift;
        $value{ $self->get_id } = shift;
    }
    
    sub get_key {
        my $self = shift;
        return $key{ $self->get_id };
    }
    
    sub get_type {
        my $self = shift;
        return $datatype{ $self->get_id };
    }
    
    sub get_value {
        my $self = shift;
        return $value{ $self->get_id };
    }
    
    sub to_xml {
        my $self = shift;
        my $key = $self->get_key || $self->get_xml_id;
        my $xml = '<key>' . $key . '</key>';
        my $value = $self->get_value;
        if ( not can( $value, 'to_xml' ) ) {
            my $type = $self->get_type || 'string';        
            $xml .= "<${type}>" . $value . "</${type}>";
        }
        else {
            $xml .= $value->to_xml;
        }
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