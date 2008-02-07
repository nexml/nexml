# $Id: 04-generator.t 4444 2007-08-21 13:04:36Z rvosa $
use Test::More;
BEGIN {
    eval { require Math::Random };
    if ( $@ ) {
         plan 'skip_all' => 'Math::Random not installed';
    }
    else {
        plan 'tests' => 2;
    }
}
use strict;
use Bio::Phylo;
require Bio::Phylo::Generator;

ok( my $gen = new Bio::Phylo::Generator, '1 init' );

ok( $gen->gen_rand_pure_birth(
    -model => 'yule',
    -tips => 10,
    -trees => 10 ),
'2 gen yule' );

# ok( $gen->gen_rand_pure_birth(
#     -model => 'hey',
#     -tips => 10,
#     -trees => 10 ),
# '3 gen hey' );
# 
# ok( $gen->gen_exp_pure_birth(
#     -model => 'yule',
#     -tips => 10,
#     -trees => 10 ),
# '4 gen yule' );
# 
# ok( $gen->gen_exp_pure_birth(
#     -model => 'hey',
#     -tips => 10,
#     -trees => 10 ),
# '5 gen hey' );
# 
# eval { $gen->gen_exp_pure_birth( -model => 'dummy', -tips => 10, -trees => 10 ); };
# ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::BadFormat' ), '6 ! gen' );
# 
# eval { $gen->gen_rand_pure_birth( -model => 'dummy', -tips => 10, -trees => 10 ); };
# ok( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions::BadFormat' ), '7 ! gen' );
# 
# ok( my $trees = $gen->gen_equiprobable( -tips => 32, -trees => 5 ),  '8 gen tree' );
# 
# ok( $gen->DESTROY,                                                   '9 destroy' );
