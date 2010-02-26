# $Id$
use strict;
#use warnings;
use Test::More tests => 14;
use Bio::Phylo::IO qw(parse);

my $data;
while (<DATA>) {
    $data .= $_;
}

ok( 1 );

ok( my $trees = parse(
    -string => $data,
    -format => 'newick' )
);

ok( $trees->get_by_value(
    -value  => 'calc_tree_length',
    -lt => 15 )
);

ok( ! scalar @{$trees->get_by_value(
    -value => 'calc_tree_length',
    -lt => 1 )}
);

ok( $trees->get_by_value(
    -value => 'calc_tree_length',
    -le => 14 )
);

ok( $trees->get_by_value(
    -value  => 'calc_tree_length',
    -gt => 5 )
);

ok( ! scalar @{$trees->get_by_value(
    -value => 'calc_tree_length',
    -gt => 30 )}
);

ok( $trees->get_by_value(
    -value  => 'calc_tree_length',
    -ge => 14 )
);

ok( ! scalar @{$trees->get_by_value(
    -value => 'calc_tree_length',
    -ge => 30 )}
);

ok( $trees->get_by_value(
    -value => 'calc_tree_length',
    -eq => 14 )
);

eval { $trees->insert('BAD!') };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ) );
ok( $trees->_container );
ok( $trees->_type );

my $newick = <<NEWICK;
((A,B),C);
((A,C),B);
((A,B),C);
NEWICK

my $forest = parse( '-format' => 'newick', '-string' => $newick );
my $cons = $forest->make_consensus('-fraction' => 0.5);

ok( $forest->first->calc_symdiff($cons) == 0, 'simple consensus' );

__DATA__
((H:1,I:1):1,(G:1,(F:0.01,(E:0.3,(D:2,(C:0.1,(A:1,B:1)cherry:1):1):1):1):1):1):0;
(H:1,(G:1,(F:1,(E:1,(D:1,(C:1,(A:1,B:1):1):1):1):1):1):1):0;
