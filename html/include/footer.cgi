#!/usr/bin/perl
use lib '../../perl/lib';
use strict;
use warnings;
use CGI;
use CGI::Carp 'fatalsToBrowser';
use Data::Dumper;
use util;
use Template;
use Cwd qw(getcwd realpath);
use File::Spec::Functions;

# because we're running under CGI we can
# obtain the host name from the environment.
my $hostname = $ENV{'SERVER_NAME'};

# subtree for this part of the site structure, 
# we can obtain this from the CGI environment
my $subtree = $ENV{'REQUEST_URI'};

# $prefix is the path to docroot, so on server-side
# includes we need it (hence it is part of $include),
# but on the client side (e.g. paths to images in an
# html page) it needs to be stripped
my $prefix  = realpath( catdir( getcwd, '../../../' ) );

# $include is used to find server side includes, e.g.
# when we embed javascript or css directly into a page.
# on the client side we need to strip $prefix of it.
my $include = catdir( $prefix, 'nexml', 'html', 'include' );

# the paths object is a utility object that translates between
# server side paths (i.e. relative to system root) and browser 
# side paths (i.e. relative to docroot)
my $paths = util::paths->new(
    '-prefix'  => $prefix,
    '-include' => $include,
);

# instantiate T::T object for site html
my $template = Template->new(
    'INCLUDE_PATH' => $include,      # or list ref
    'POST_CHOMP'   => 1,             # cleanup whitespace
#    'PRE_PROCESS'  => 'header.tmpl', # prefix each template
#    'POST_PROCESS' => 'footer.tmpl', # suffix each template
    'START_TAG'    => '<%',
    'END_TAG'      => '%>',
    'OUTPUT_PATH'  => $prefix,
);

# variables to be interpolated in template
my $vars = {
    'title'       => 'nexml - index of ' . $ENV{'REQUEST_URI'},
    'mainHeading' => 'In this section:',
    'currentURL'  => 'http://' . $hostname . $subtree,
    'currentDate' => my $time = localtime,
    'paths'       => $paths,
    'hostName'    => $hostname,
};

my $cgi = CGI->new;
print $cgi->header;
print '</div>';
$template->process( 'footer.tmpl', $vars ) || die $template->error();