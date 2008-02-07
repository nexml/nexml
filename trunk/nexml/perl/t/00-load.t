# $Id: 00-load.t 4186 2007-07-11 02:15:56Z rvosa $
use Test::More tests => 1;

BEGIN {
    use_ok('Bio::Phylo');
}
diag("Testing Bio::Phylo $Bio::Phylo::VERSION, Perl $]");
