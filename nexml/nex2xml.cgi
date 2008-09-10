#!/usr/bin/perl
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/5.8.6/darwin-thread-multi-2level/';
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib';
    unshift @INC, '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
}
use strict;
use warnings;
use Bio::Phylo::IO 'parse';
my $blocks = parse( 
    '-format' => 'nexus',
    '-handle' => \*STDIN,
);

if ( UNIVERSAL::isa( $blocks, 'ARRAY' ) ) {
    print "Content-Type: application/xml\n\n";
    print $blocks->[0]->get_root_open_tag;
    for my $block ( @{ $blocks } ) {
        print $block->to_xml;
    }
    print $blocks->[-1]->get_root_close_tag;
}