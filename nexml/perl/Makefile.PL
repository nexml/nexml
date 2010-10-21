# $Id$
use strict;
use warnings;
use ExtUtils::MakeMaker;
use File::Find;

check_prereq( qw(
    SVG
    Math::Random
    XML::Twig
    XML::LibXML
    Math::CDF
    List::Util
    SWF::Builder
) );

WriteMakefile(
    'NAME'         => 'Bio-Phylo',
    'AUTHOR'       => 'Rutger Vos',
    'PL_FILES'     => {},
    'EXE_FILES'    => [],
    'VERSION_FROM' => 'lib/Bio/Phylo.pm',  
    'clean'        => {},    
    'dist'         => { 'COMPRESS' => 'gzip -9f', 'SUFFIX' => 'gz', },
	'MIN_PERL_VERSION' => '5.8.0',
);

sub check_prereq {
    my @classes = @_;
    for my $class ( @classes ) {
        eval "require $class";
        if ( $@ ) {
            print "*** The optional dependency \"$class\" can't be loaded.\n";
            print "    I will continue, but some functionality will not be \n";
            print "    available. Refer to the Bio::Phylo README file for\n";
            print "    more information.\n\n";
        }
    }
}