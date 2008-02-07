# $Id: 14-nexus.t 4444 2007-08-21 13:04:36Z rvosa $
use strict;
#use warnings;
use Test::More tests => 5;
use Bio::Phylo::IO qw(parse);

#Bio::Phylo::IO->VERBOSE( -level => 1 );

# Up until the next big block of comment tokens, a number of nexus strings is
# defined.
################################################################################
################################################################################
################################################################################
################################################################################

# This string holds a valid (mesquite) nexus file
my $testparse = <<TESTPARSE
#NEXUS
[written Wed Jun 08 00:30:00 CEST 2005 by Mesquite  version 1.02+ (build g8)]

BEGIN TAXA;
	DIMENSIONS NTAX=5;
	TAXLABELS
		taxon_1 taxon_2 taxon_3 taxon_4 taxon_5
	;

END;


BEGIN CHARACTERS;
[! Simulated Matrices on Current Tree:  Matrix #1; Simulator: Evolve DNA Characters; most recent tree: Default ladder [seed for matrix sim. 1118183366345]
     Evolve DNA Characters:  Simulated evolution using model Jukes-Cantor with the following parameters:
        Root states model (Equal Frequencies): Equal Frequencies
        Equilibrium states model (Equal Frequencies): Equal Frequencies
        Character rates model (Equal Rates): Equal Rates
        Rate matrix model (Single Rate): single rate

         Stored Probability Model for Simulation:  Current model "Jukes-Cantor":         Root states model (Equal Frequencies): Equal Frequencies
        Equilibrium states model (Equal Frequencies): Equal Frequencies
        Character rates model (Equal Rates): Equal Rates
        Rate matrix model (Single Rate): single rate

         Stored Matrices:  Character Matrices from file: Project with home file "testparse.nex"
     Tree of context:  Tree(s) used from Tree Window 2 showing Stored Trees. Last tree used: Default ladder  [tree: (1,(2,(3,(4,5))));]
]
	DIMENSIONS NCHAR=10;
	FORMAT DATATYPE = DNA GAP = - MISSING = ?;
	MATRIX
	taxon_1  TACCACTTGT
	taxon_2  GTTCTCTTCT
	taxon_3  AGCGTCTTTC
	taxon_4  ACTTTGTTTC
	taxon_5  GCCCCTCGAG


;


END;

BEGIN ASSUMPTIONS;
	TYPESET * UNTITLED   =  unord:  1 -  10;

END;

BEGIN MESQUITECHARMODELS;
	ProbModelSet * UNTITLED   =  'Jukes-Cantor':  1 -  10;
END;

BEGIN TREES;
[!Parameters: ]
	TRANSLATE
		1 taxon_1,
		2 taxon_2,
		3 taxon_3,
		4 taxon_4,
		5 taxon_5;
	TREE Default_ladder = (1,(2,(3,(4,5))));
	TREE Default_bush = (1,2,3,4,5);
	TREE Default_symmetrical = ((1,2),(3,(4,5)));

END;
TESTPARSE
;

# this string holds a valid nexus tree block
my $testparse_trees = <<TESTPARSE_TREES
#NEXUS
BEGIN TREES;
[!Parameters: ]
	TRANSLATE
		1 taxon_1,
		2 taxon_2,
		3 taxon_3,
		4 taxon_4,
		5 taxon_5;
	TREE Default_ladder = (1,(2,(3,(4,5))));
	TREE Default_bush = (1,2,3,4,5);
	TREE Default_symmetrical = ((1,2),(3,(4,5)));

END;
TESTPARSE_TREES
;

# this string holds a nexus file with a bad nchar specification.
my $testparse_bad = <<TESTPARSE_BAD
#NEXUS
[written Wed Jun 08 00:30:00 CEST 2005 by Mesquite  version 1.02+ (build g8)]

BEGIN TAXA;
	DIMENSIONS NTAX=5;
	TAXLABELS
		taxon_1 taxon_2 taxon_3 taxon_4 taxon_5
	;

END;


BEGIN CHARACTERS;
[! Simulated Matrices on Current Tree:  Matrix #1; Simulator: Evolve DNA Characters; most recent tree: Default ladder [seed for matrix sim. 1118183366345]
     Evolve DNA Characters:  Simulated evolution using model Jukes-Cantor with the following parameters:
        Root states model (Equal Frequencies): Equal Frequencies
        Equilibrium states model (Equal Frequencies): Equal Frequencies
        Character rates model (Equal Rates): Equal Rates
        Rate matrix model (Single Rate): single rate

         Stored Probability Model for Simulation:  Current model "Jukes-Cantor":         Root states model (Equal Frequencies): Equal Frequencies
        Equilibrium states model (Equal Frequencies): Equal Frequencies
        Character rates model (Equal Rates): Equal Rates
        Rate matrix model (Single Rate): single rate

         Stored Matrices:  Character Matrices from file: Project with home file "testparse.nex"
     Tree of context:  Tree(s) used from Tree Window 2 showing Stored Trees. Last tree used: Default ladder  [tree: (1,(2,(3,(4,5))));]
]
	DIMENSIONS NCHAR=11;
	FORMAT DATATYPE = DNA GAP = - MISSING = ?;
	MATRIX
	taxon_1  TACCACTTGT
	taxon_2  GTTCTCTTCT
	taxon_3  AGCGTCTTTC
	taxon_4  ACTTTGTTTC
	taxon_5  GCCCCTCGAG


;


END;

BEGIN ASSUMPTIONS;
	TYPESET * UNTITLED   =  unord:  1 -  10;

END;

BEGIN MESQUITECHARMODELS;
	ProbModelSet * UNTITLED   =  'Jukes-Cantor':  1 -  10;
END;

BEGIN TREES;
[!Parameters: ]
	TRANSLATE
		1 taxon_1,
		2 taxon_2,
		3 taxon_3,
		4 taxon_4,
		5 taxon_5;
	TREE Default_ladder = (1,(2,(3,(4,5))));
	TREE Default_bush = (1,2,3,4,5);
	TREE Default_symmetrical = ((1,2),(3,(4,5)));

END;
TESTPARSE_BAD
;

# this string holds a taxa block with a bad ntax specification
my $testparse_taxa_bad = <<TESTPARSE_TAXA_BAD
#NEXUS
[written Wed Jun 08 00:30:00 CEST 2005 by Mesquite  version 1.02+ (build g8)]

BEGIN TAXA;
	DIMENSIONS NTAX=6;
	TAXLABELS
		taxon_1 taxon_2 taxon_3 taxon_4 taxon_5
	;

END;
TESTPARSE_TAXA_BAD
;

################################################################################
################################################################################
################################################################################
################################################################################
# Done defining nexus tokens, let's try to parse them.

print "--------------------------------------------------------------------\n";
eval { parse( '-format' => 'nexus', '-string' => $testparse ) };
if ( $@ ) {
    print $@->trace->as_string;
    die $@;
}
ok( parse( '-format' => 'nexus', '-string' => $testparse ), '1 good parse' );
print "--------------------------------------------------------------------\n";
ok( parse( '-format' => 'nexus', '-string' => $testparse_trees ), '2 tree block' );
print "--------------------------------------------------------------------\n";
eval { parse( '-format' => 'nexus', '-string' => $testparse_taxa_bad ) };
ok( $@->isa('Bio::Phylo::Util::Exceptions::BadFormat'), '3 bad ntax' );
print "--------------------------------------------------------------------\n";
eval { parse( '-format' => 'nexus', '-string' => $testparse_bad ) };
ok( $@->isa('Bio::Phylo::Util::Exceptions::BadFormat' ), '4 bad nchar' );
print "--------------------------------------------------------------------\n";
eval { parse( '-format' => 'nexus', '-file' => 'DOES_NOT_EXIST' ) };
ok( $@->isa('Bio::Phylo::Util::Exceptions::FileError'), '5 file error' );
