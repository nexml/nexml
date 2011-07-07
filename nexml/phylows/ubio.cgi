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
BEGIN {
    $ENV{'UBIO_KEYCODE'} = '286a57553cde107e0e44e67799332a995ba66c7a';
}
use strict;
use warnings;
use CGI;
use Bio::Phylo::PhyloWS::Service::Ubio;
use Bio::Phylo::Util::Logger ':levels';

my $logger = Bio::Phylo::Util::Logger->new;
open my $fh, '>', 'ubio.log' or die $!;
$logger->VERBOSE( '-level' => DEBUG );
$logger->set_listeners( sub { print $fh shift } );

eval {
	my $service = Bio::Phylo::PhyloWS::Service::Ubio->new( '-url' => $ENV{'SCRIPT_URI'} );
	my $cgi = CGI->new;
	$service->handle_request( $cgi );
};
if ( $@ ) {
	$logger->fatal("\n$@");
	die $@;
}
