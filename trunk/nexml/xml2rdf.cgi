#!/usr/bin/perl
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    use lib $ENV{'DOCUMENT_ROOT'} . '/perllib';	
    use lib $ENV{'DOCUMENT_ROOT'} . '/perllib/arch';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib';
}
use util;
use strict;
use warnings;
use XML::Twig;
use File::Temp;
use LWP::UserAgent;
use CGI ':standard';
use UNIVERSAL 'isa';
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Util::Exceptions 'throw';
use constant MAX_SIZE => 2_000_000;
use constant COMPACT  =>   500_000;

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

my $cdao = `java -cp java/jars/saxon9he.jar net.sf.saxon.Transform -s:$name -xsl:xslt/nexml2cdao.xsl`;
my $rdfa = `java -cp java/jars/saxon9he.jar net.sf.saxon.Transform -s:$name -xsl:xslt/RDFa2RDFXML.xsl`;
unlink $name;

my $cdao_twig = XML::Twig->new( 'pretty_print' => 'indented' );
$cdao_twig->parse( $cdao );
my $cdao_root = $cdao_twig->root;

my $rdfa_twig = XML::Twig->new( 'pretty_print' => 'indented' );
$rdfa_twig->parse( $rdfa );
my $rdfa_root = $rdfa_twig->root;

for my $child ( $rdfa_root->children ) {
	$child->cut;
	$child->paste( $cdao_root );
}

print "content-type: application/rdf+xml\n\n";
$cdao_twig->print;
