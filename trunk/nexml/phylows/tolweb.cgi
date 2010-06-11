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
use CGI::Carp 'fatalsToBrowser';
use Bio::Phylo::IO qw(parse unparse);
use Bio::Phylo::Factory;
use HTML::Entities;
use Bio::Phylo::Util::CONSTANT ':namespaces';
use constant URL => 'http://150.135.239.5/onlinecontributors/app?service=external&page=xml/TreeStructureService&node_id=';

if ( $ENV{'QUERY_STRING'} =~ /wsdl/ ) {
	my $file = $0;
	$file =~ s/\.cgi$/.wsdl/;
	open my $fh, '<', $file or die $!;
	my $wsdl = do { local $/; <$fh>};
	print "Content-type: text/xml\n\n" . $wsdl;
	exit 0; 	
}
if ( $ENV{'PATH_INFO'} and $ENV{'PATH_INFO'} =~ m|tree/ToL:([0-9]+)$| ) {
	my $nexml;
	my $id  = $1;
	my $url = URL . $id;	
	my $fac = Bio::Phylo::Factory->new;
	eval {
		my $tree = parse(
			'-format' => 'tolweb',
			'-url'    => $url
		);
		my ( $forest, $project ) = ( $fac->create_forest, $fac->create_project );
		$forest->insert( $tree );
		$project->insert( $forest );
		$forest->add_meta( 
			$fac->create_meta(
				'-namespaces' => { 'dcterms' => _NS_DCTERMS_ },
				'-triple'     => { 'dcterms:identifier' => $id },
			)
		);
		$forest->add_meta( 
			$fac->create_meta(
				'-namespaces' => { 'owl' => _NS_OWL_ },		
				'-triple'     => { 'owl:sameAs' => 'http://tolweb.org/' . $id }
			)
		);		
		$nexml = $project->to_xml; 
	};
	if ( $nexml and not $@ ) {
		print "Content-type: text/xml\n\n" . $nexml;			
	}
	else {
		die $@;
	}		
}
else {
	die "$ENV{'PATH_INFO'} => not a valid tolweb ID! URL needs to be /tree/ToL:{id}";
}

