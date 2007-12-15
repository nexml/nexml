# $Id: 02-newick.t 4444 2007-08-21 13:04:36Z rvosa $
use strict;
#use warnings;
use Test::More tests => 3;
use Bio::Phylo;
use Bio::Phylo::IO qw(parse);

my $string = '(H:1,(G:1,(F:1,(E:1,(D:1,(C:1,(A:1,B):1):1):1):1):1):1):0;';

ok( my $phylo = Bio::Phylo->new,                                      '1 init'      );
ok( !Bio::Phylo->VERBOSE( -level => 0 ),                              '2 set terse' );
ok( Bio::Phylo::IO->parse( -string => $string, -format => 'newick' ), '3 parse'     );
