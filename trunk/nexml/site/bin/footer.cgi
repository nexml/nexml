#!/usr/bin/perl
BEGIN {
    use lib '../../../perllib';	
    use lib '../../../perllib/arch';
    unshift @INC, '../../perl/lib';
    unshift @INC, '../../site/lib';
}
use strict;
use warnings;
use CGI;
use CGI::Carp 'fatalsToBrowser';
use util;
use util::siteFactory;

my $fac = util::siteFactory->new;

# variables to be interpolated in template
my $vars = $fac->create_template_vars();

my $cgi = CGI->new;
print $cgi->header;
print '</div>';
my $template = $fac->create_plain_template;
$template->process( 'footer.tmpl', $vars ) || die $template->error();
