package Bio::Phylo::Matrices::Character;
use strict;
use Bio::Phylo::Util::CONSTANT qw'_CHARACTER_ _CHARACTERS_';
use Bio::Phylo::Matrices::TypeSafeData;
use vars '@ISA';
@ISA=qw(Bio::Phylo::Matrices::TypeSafeData);

sub to_xml {
    my $self = shift;
    if ( my $to = $self->get_type_object ) {
        if ( $to->get_type !~ m/continuous/i ) {
            $self->set_attributes( 'states' => $to->get_xml_id );
        }
    }
    return $self->SUPER::to_xml;
}

sub validate   { 1 }
sub _container { _CHARACTERS_ }
sub _type      { _CHARACTER_ }
sub _tag       { 'char' }

1;