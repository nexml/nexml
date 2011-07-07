#!/usr/bin/perl
BEGIN {
    use CGI::Carp qw(fatalsToBrowser);
}
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    use lib '../../perllib';
    use lib '../../perllib/arch';
}

use strict;
use warnings;
use CGI;
use Bio::Phylo::PhyloWS::Service::Tolweb;
use Bio::Phylo::Util::Logger ':levels';

my $logger = Bio::Phylo::Util::Logger->new;
open my $fh, '>', 'tolweb.log' or die $!;
$logger->VERBOSE( '-level' => DEBUG );
$logger->set_listeners( sub { print $fh shift } );

eval {
	my $service = Bio::Phylo::PhyloWS::Service::Tolweb->new( '-url' => $ENV{'SCRIPT_URI'} );
	my $cgi = CGI->new;
	$service->handle_request( $cgi );
};
if ( $@ ) {
	$logger->fatal("\n$@");
	die $@;
}
