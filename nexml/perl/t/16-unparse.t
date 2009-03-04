# $Id$
use strict;
#use warnings;
use Test::More tests => 4;
use Bio::Phylo::IO qw(parse unparse);
Bio::Phylo->VERBOSE( -level => 0 );

eval { unparse() };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::OddHash' ) );

eval { unparse( 'A', 'B', 'C' ) };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::OddHash' ) );

eval { unparse( -format => 'bogus', -phylo => 'bogus' ) };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::ExtensionError' ) );

eval { unparse( -tokkie => 'bogus', -phylo => 'bogus' ) };
ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::BadFormat' ) );
