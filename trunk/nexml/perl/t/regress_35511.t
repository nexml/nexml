use strict;
use Test::More 'no_plan';
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;

my $logger = Bio::Phylo::Util::Logger->new;
$logger->VERBOSE( '-level' => 0 );

my $newick = <<NEWICK;
[ lh=-4464.484953 ](Methanococcus_voltae:0.32692,(('Pyrococcus furiosus (includes Pyrococcus woesei)':0.05887,Pyrococcus_abyssi:0.03869)100:0.36861,
(((Sulfolobus_solfataricus:0.08344,Sulfolobus_tokodaii:0.10668)100:0.15268,Aeropyrum_pernix:0.20003)
100:0.09351,Desulfuroccus_amylolyticus:0.18345)100:0.28706)100:0.41157,'Methanococcus jannaschii (aka Methanocaldococcus jannaschii)':0.00001);
NEWICK

my @tips = (
    "Methanococcus_voltae",
    "'Pyrococcus furiosus (includes Pyrococcus woesei)'",
    "Pyrococcus_abyssi",
    "Sulfolobus_solfataricus",
    "Sulfolobus_tokodaii",
    "Aeropyrum_pernix",
    "Desulfuroccus_amylolyticus",
    "'Methanococcus jannaschii (aka Methanocaldococcus jannaschii)'",
);

my $tree = parse( '-format' => 'newick', '-string' => $newick )->first;

for my $tipname ( @tips ) {
    eval { $tree->prune_tips( [ $tipname ] ) };
    ok( ! $@ );
}
ok( scalar @{ $tree->get_entities } == 1 );