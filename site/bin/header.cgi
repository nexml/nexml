#!/usr/bin/perl
use lib '../../site/lib';
use lib '../../../perllib';
use lib '../../../perllib/arch';
use strict;
use warnings;
use CGI;
use CGI::Carp 'fatalsToBrowser';
use util;

my $fac = util::siteFactory->new;

# instantiate T::T object for site html
my $template = $fac->create_plain_template(
	'INCLUDE_PATH' => [ 
		$fac->prefix . $fac->subtree, 
		$fac->include 
	]
);

my $cgi = CGI->new;

# variables to be interpolated in template
my $vars = $fac->create_template_vars(
    'title'       => 'nexml - index of ' . $fac->subtree,
    'mainHeading' => 'Directory listing',
);

print $cgi->header;
$template->process( 'header.tmpl', $vars ) || die $template->error();
if ( -e $fac->prefix . $fac->subtree . 'README.html' ) {
     $template->process( 'README.html', $vars );
}
print '<div class="directoryIndex">';
