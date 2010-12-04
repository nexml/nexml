package Bio::Phylo::Models::Substitution::Dna::F81;
use Bio::Phylo::Models::Substitution::Dna;
use strict;
use vars '@ISA';
@ISA = qw(Bio::Phylo::Models::Substitution::Dna);

# subst rate
sub get_rate {
	my $self = shift;
	return $self->get_pi(shift);
}

sub get_nst { 1 }

1;