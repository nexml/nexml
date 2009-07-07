#!/usr/bin/perl
BEGIN {
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib'; 
    unshift @INC, 'perl/lib';   
    unshift @INC, 'site/lib';
}
use strict;
use warnings;
use CGI;
use CGI::Carp 'fatalsToBrowser';

my $cgi = CGI->new;
my $path_info = $cgi->path_info;

if ( $path_info =~ m|^/(.+)/phylows/(.+?)$| ) {
    my $service = $1; # tolweb, timetree, cipres
    my $service_arg = $2; # e.g. identifier or search query
    my $service_class = 'Bio::Phylo::PhyloWS::Service::' . ucfirst($service);    
    eval "require ${service_class};";
    if ( !$@ ) {
        eval {
            my $url = $cgi->url() . '/' . lc($service) . '/phylows/';
            my $service = $service_class->new( '-url' => $url );
            $service->handle_request($cgi);
        };
        if ( $@ ) {
            die $@;
        }
    }
    else {
        die $@;
    }
}
else {
    die "malformed URL: needs /\${service}/phylows/\${query_or_id} token ($path_info)";
}