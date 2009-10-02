# $Id$
use strict;
#use warnings;
use Test::More tests => 6;
use Bio::Phylo::IO qw(parse unparse);

eval { parse() };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::OddHash' ), '1 parse no opts' );

eval { parse( 'A', 'B', 'C' ) };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::OddHash' ), '2 parse wrong args' );

eval { parse( -format => 'none' ) };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::BadArgs' ), '3 parse bad format' );

ok( parse( -format => 'nexus', -string => 'blah' ), '4 parse string' );

eval { parse( -string => 'blah' ) };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::BadArgs' ), '5 parse no format' );

my $taxa = 'A|B|C|D|E';
ok( parse( -format => 'taxlist', -fieldsep => '|', -string => $taxa ),
    '6 parse taxon list' );
