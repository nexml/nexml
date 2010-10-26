#!/usr/bin/perl
use lib '../../site/lib';
use lib '../../../perllib';
use lib '../../../perllib/arch';
use strict;
use warnings;
use CGI;
use CGI::Carp 'fatalsToBrowser';
use util;
use util::siteFactory;
use File::Find;

my $fac = util::siteFactory->new;

# instantiate T::T object for site html
my $template = $fac->create_site_template();

# variables to be interpolated in template
my $vars = $fac->create_template_vars();

my $include = $fac->include;
find(
    sub {
        my $dir  = $File::Find::dir;
        my $name = $File::Find::name;
        if ( $name =~ qr/\.tmpl$/ ) {
            $name =~ s/^\Q$include\E\///;
            my $outfile = $name;
            $outfile =~ s|^static|nexml/html|;
            $outfile =~ s|tmpl$|html|;
            {
                open my $fh, '<', $File::Find::name or die $!;
                while(<$fh>) {
                    if ( /<!--\s+TITLE:\s*(.*?)\s*-->/i ) {
                        $vars->{'title'} = $1;
                    }
                    if ( /<!--\s+HEADING:\s*(.*?)\s*-->/i ) {
                        $vars->{'mainHeading'} = $1;
                    }
                }
            }
            $template->process( $name, $vars, $outfile ) || die $template->error();
        }
    },
    $include . '/static'
);


