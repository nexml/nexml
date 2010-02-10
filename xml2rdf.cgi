#!/usr/bin/perl
use CGI::Carp 'fatalsToBrowser';
use strict;
use warnings;
use XML::Twig;
use File::Temp;
use LWP::UserAgent;
use CGI;

my $xsltproc = 'xsltproc';
my $rdfaxsl = 'xslt/RDFa2RDFXML.xsl';
my $cdaoxsl = 'xslt/nexml2cdao.xsl';
my $indata;

if ( $ENV{'PATH_INFO'} ) {
	my $ua = LWP::UserAgent->new;
	my $path = $ENV{'PATH_INFO'};
	my $response = $ua->get('http://'.$path);
	if ( $response->is_success ) {
		$indata = $response->content;
	}
}
else {
	my $q = CGI->new;
	my ( $filename, @lines ) = read_file( $q->param('file') || \*STDIN );
	unlink $filename;
	$indata = join "\n", @lines;
}

my ( $fh, $filename ) = File::Temp::tempfile;
print $fh $indata;
my $cdao = `$xsltproc $cdaoxsl $filename`;
my $rdfa = `Â$xsltproc $rdfaxsl $filename`;
print "content-type: application/rdfa+xml\n\n";
print $cdao;

sub read_file {
	my $file = shift;
	my @lines;
	if ( fileno( $file ) ) {
		@lines = <$file>;
	}
	else {
		open my $fh, '<', $file or die "Can't open file to translate: $!";
		@lines = <$fh>;
		close $fh;
	}
	my ( $fh, $filename ) = File::Temp::tempfile;
	$fh->print( @lines );
	$fh->close;
	return $filename, @lines;
}
