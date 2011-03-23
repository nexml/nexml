package Bio::Phylo::Matrices::Characters;
use strict;
use Bio::Phylo::Util::CONSTANT qw'_CHARACTERS_ _NONE_';
use Bio::Phylo::Matrices::TypeSafeData;
use Bio::Phylo::Factory;
use vars '@ISA';
@ISA=qw(Bio::Phylo::Matrices::TypeSafeData);

sub to_xml {
    my $self = shift;
    return join '', map { $_->to_xml } @{ $self->get_entities };
}

sub validate   { 1 }
sub _container { _NONE_ }
sub _type      { _CHARACTERS_ }
sub _tag       { undef }

1;