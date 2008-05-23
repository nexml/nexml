#!/usr/bin/perl
# $Id: validator.cgi 424 2008-02-07 05:06:10Z rvos $
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/5.8.6/darwin-thread-multi-2level/';
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib'; 
    unshift @INC, '../perl/lib';   
    unshift @INC, '../site/lib';
    push @INC, '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
}
use strict;
use warnings;
use CGI::Carp 'fatalsToBrowser';
use Bio::Phylo::IO qw(parse unparse);
use Bio::Phylo::Forest;
use LWP::UserAgent;

my $url = 'http://tolweb.org/onlinecontributors/app?service=external&page=xml/TreeStructureService&node_id=';
my $tolwebid;
my $pathinfo = $ENV{'PATH_INFO'};
if ( $pathinfo and $pathinfo =~ m|/([0-9]+)$| ) {
	$tolwebid = $1;
}
else {
	die "not a valid tolweb ID";
}

my $ua = LWP::UserAgent->new;
my $response = $ua->get($url.$tolwebid);
 
if ($response->is_success) {
	my $nexml;	
	my $content = $response->content;
	$content =~ s/\Q<?xml version="1.0" standalone="yes"?>\E//;
	$content =~ s/.*<TREE>/<TREE>/s;
	$content =~ s/<\/TREE>.*/<\/TREE>/s;
	eval {
		my $tree = parse('-format'=>'tolweb','-string'=>$content);
		$nexml = unparse(
			'-format' => 'nexml',
			'-phylo'  => Bio::Phylo::Forest->new()->insert($tree)
		) 
	};
	if ( $@ ) {
		die $@, $content;
	}
	else {
		print "Content-type: text/xml\n\n" . $nexml;
	}
	
}
else {
	die $response->status_line;
}