# $Id$
use strict;
use Bio::Phylo::Util::CONSTANT 'looks_like_instance';
use Test::More 'no_plan';
use Bio::Phylo::IO qw(parse unparse);
use Bio::Phylo::Forest::Node;
use Bio::Phylo::Forest::Tree;

my $data;
while (<DATA>) {
    $data .= $_;
}

Bio::Phylo->VERBOSE( -level => 0 );

ok( 1, '1 init' );

ok( my $trees = parse(
    -string => $data,
    -format => 'newick' ),
'2 parse' );

ok( my $treeset = $trees->get_entities, '3 trees' );
my $tree        = $treeset->[0];
my $unresolved  = $treeset->[2];

ok( my $root       = $tree->get_root,           '4 get root' );
ok( my $node       = $root->get_first_daughter, '5 get first daughter' );
ok( my $other_node = $root->get_last_daughter,  '6 get last daughter' );
ok( my $children   = $root->get_children,       '7 get children' );

# get
ok( $tree->get('calc_tree_length'), '8 get ctl' );
ok( $tree->get_entities,            '9 get n' );
ok( $tree->get_internals,           '10 get int' );
ok( $tree->get_terminals,           '11 get term' );

#ok($tree->get_by_name('cherry'),                                   '12 gbn');
ok( $tree->get_by_value(
    -value => 'get_branch_length',
    -lt    => 0.5 ),
'12 get lt' );

ok( $tree->get_by_value(
    -value => 'get_branch_length',
    -eq    => 2 ),
'13 get eq' );

ok( $tree->get_by_value(
    -value => 'get_branch_length',
    -le    => 0.4 ),
'14 get le' );

ok( $tree->get_by_value(
    -value => 'get_branch_length',
    -ge    => 0.1 ),
'15 get ge' );

ok( $tree->get_by_value(
    -value => 'get_branch_length',
    -gt    => 0.2 ),
'16 get gt' );

ok( $tree->is_binary, '17 is binary' );

# methods on unresolved tree
ok( !$unresolved->is_binary,             '18 is binary' );
ok( !$unresolved->is_ultrametric,        '19 is ultrametric' );

eval { $unresolved->calc_rohlf_stemminess };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ), '20 calc rohlf stemminess: ' . ref($@));

eval { $unresolved->calc_imbalance };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ), '21 calc imbalance' );

eval { $unresolved->calc_branching_times };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ), '22 calc branching times' );

eval { $unresolved->calc_ltt };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ), '23 calc ltt' );

eval { $tree->insert('BAD!') };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ), '24 insert bad obj' );

# tests
ok( !$tree->is_ultrametric(0.01), '25 is ultrametric' );
ok( !$tree->is_monophyletic( $children, $node ), '26 not monophyletic' );

# test for monophyly
my $poly = $unresolved->get_by_regular_expression(
    -value => 'get_name',
    -match => qr/^poly$/
);

my $e = $unresolved->get_by_regular_expression(
    -value => 'get_name',
    -match => qr/^E$/
);

my $desc = $poly->[0]->get_descendants;

ok( $tree->is_monophyletic( $desc, $e->[0] ), '27 is monophyletic' );

# calculations
ok( $tree->calc_tree_length,         '28 calc tree length' );
ok( $tree->calc_tree_height,         '29 calc tree height' );
ok( $tree->calc_number_of_nodes,     '30 calc num nodes' );
ok( $tree->calc_number_of_terminals, '31 calc num terminals' );
ok( $tree->calc_number_of_internals, '32 calc num internals' );
ok( $tree->calc_total_paths,         '33 calc total paths' );
ok( $tree->calc_redundancy,          '34 calc redundancy' );
ok( $tree->calc_imbalance,           '35 calc imbalance' );

# balance calculation
my $balanced = $treeset->[3];
ok( $tree->calc_imbalance, '36 calc imbalance' );

# ultrametric calculations
ok( $tree = $tree->ultrametricize, '37 ultrametricize' );
ok( $tree->calc_fiala_stemminess, '38 calc fiala stemminess' );
ok( $tree->calc_rohlf_stemminess, '39 calc rohlf stemminess' );
ok( $tree->calc_resolution,       '40 calc resolution' );
ok( $tree->calc_branching_times,  '41 calc branching times' );
ok( $tree->calc_ltt,              '42 calc ltt' );
ok( $tree->scale(10),             '43 scale' );

# testing on undef branch lengths
my $undef = $treeset->[3];
$root = $undef->get_root;

eval { $undef->calc_rohlf_stemminess };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::ObjectMismatch' ), '44 calc rohlf stemminess: ' . ref($@) );

eval { $undef->get('BAD!') };
ok( looks_like_instance( $@, 'Bio::Phylo::Util::Exceptions::UnknownMethod' ), '45 bad arg get' );
ok( $undef->calc_imbalance,         '46 calc imbalance' );

# trying to create a cyclical tree, no mas!
my $node1    = new Bio::Phylo::Forest::Node;
my $node2    = new Bio::Phylo::Forest::Node;
my $cyclical = new Bio::Phylo::Forest::Tree;
$node1->set_parent($node2);
$node2->set_parent($node1);
$cyclical->insert($node1);
$cyclical->insert($node2);
ok( $cyclical->get_root, '47 no root in cycle' );
ok( $tree->DESTROY, '48 destroy' );

my $left  = '((((A,B),C),D),E);';
my $right = '(E,(D,(C,(A,B))));';
my $ladder = parse( '-format' => 'newick', '-string' => $left )->first;
ok( $ladder->ladderize->to_newick eq $right, '49 ladderize' );

__DATA__
((H:1,I:1):1,(G:1,(F:0.01,(E:0.3,(D:2,(C:0.1,(A:1,B:1)cherry:1):1):1):1):1):1):0;
(H:1,(G:1,(F:1,((C:1,(A:1,B:1):1):1,(D:1,E:1):1):1):1):1):0;
(H:1,(G:1,(F:1,((C:1,(A:1,I:1,B:1)poly:1):1,(D:1,E:1):1):1):1):1):0;
((((A,B),(C,D)),(E,F)),((G,H),(I,J)));

