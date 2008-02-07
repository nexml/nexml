# $Id: 18-taxlist.t 4444 2007-08-21 13:04:36Z rvosa $
use strict;
#use warnings;
use Test::More tests => 1;
use Bio::Phylo;
use Bio::Phylo::Parsers::Taxlist;
ok( my $taxlist = Bio::Phylo::Parsers::Taxlist->_new, '1 init obj' );
