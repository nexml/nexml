# $Id: Makefile.PL,v 1.1 2005/09/06 17:27:13 rvosa Exp $
# Subversion: $Rev: 177 $
use strict;
use warnings;
use ExtUtils::MakeMaker;

WriteMakefile(
    'NAME'         => 'Bio-Phylo-Nexml',
    'AUTHOR'       => 'Rutger Vos <rvosa@interchange.ubc.ca>',
    'PL_FILES'     => {},
    'EXE_FILES'    => [],
    'VERSION_FROM' => 'lib/Bio/Phylo/Parsers/Nexml.pm',    
    'clean'        => {},    
    'dist'         => { 'COMPRESS' => 'gzip -9f', 'SUFFIX' => 'gz', },
    'PREREQ_PM'    => {
        'Bio::Phylo' => 0,
        'XML::Twig'  => 0,
    },
    
);
