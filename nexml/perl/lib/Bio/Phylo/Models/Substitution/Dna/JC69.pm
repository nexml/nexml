package Bio::Phylo::Models::Substitution::Dna::JC69;
use Bio::Phylo::Models::Substitution::Dna;
use strict;
use vars '@ISA';
@ISA = qw(Bio::Phylo::Models::Substitution::Dna);

# base freq
sub get_pi { 0.25 }

# substitution rate
sub get_rate { shift->get_mu / 4 }

sub get_nst { 1 }

1;