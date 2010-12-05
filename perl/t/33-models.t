use strict;
use Test::More 'no_plan';
use Bio::Phylo::Models::Substitution::Dna;

my $model = Bio::Phylo::Models::Substitution::Dna->new(
    '-type'  => 'GTR',
    '-pi'    => [ 0.23, 0.27, 0.24, 0.26 ],
    '-kappa' => 2,
    '-alpha' => 0.9,
    '-pinvar'=> 0.5,
    '-ncat'  => 6,
    '-median'=> 1,
    '-rate'  => [
        [ 0.23, 0.23, 0.23, 0.23 ],
        [ 0.23, 0.26, 0.26, 0.26 ],
        [ 0.27, 0.26, 0.26, 0.26 ],
        [ 0.24, 0.26, 0.26, 0.26 ]
    ]
);

print $model->to_string( '-format' => 'garli' );