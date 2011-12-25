#!/usr/bin/perl
BEGIN {
    use lib '../../perllib';	
    use lib '../../perllib/arch';
    use lib '../../bio-phylo/lib';
    unshift @INC, '../site/lib';
}
use CGI::Carp 'fatalsToBrowser';
use util;
use strict;
use warnings;
use File::Temp;
use LWP::UserAgent;
use CGI ':standard';
use UNIVERSAL 'isa';

my $q = CGI->new;
my $content;

if ( $ENV{'PATH_INFO'} || $q->param('url') ) {
	my $ua = LWP::UserAgent->new;
	my $url;
	if ( $ENV{'PATH_INFO'} ) {
		$url = 'http:/' . $ENV{'PATH_INFO'};
	}
	else {
		if ( $ENV{'REQUEST_METHOD'} eq 'GET' ) {
			require URI::Escape;
			$url = URI::Escape::uri_unescape($q->param('url'));
		}
		else {
			$url = $q->param('url');
		}
	}
	my $response = $ua->get($url);
	if ( $response->is_success ) {
		$content = $response->content;
	}
}
elsif ( $q->param('file') ) {	
	my $upload = $q->param('file');
	$content = do { local $/; <$upload> };
}
elsif ( $q->param('string') ) {
	$content = $q->param('string');
}
else {
	$content = do { local $/; <> };
}

my ( $fh, $name ) = File::Temp::tempfile;
print $fh $content;
close $fh;

my $cdao = `java -cp ../downloads/validator.jar transformer.NeXML2CDAO $name ../`;
unlink $name;

print "content-type: application/rdf+xml\n\n";
print $cdao;
