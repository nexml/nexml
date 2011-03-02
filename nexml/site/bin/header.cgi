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

$util::siteFactory::VARIABLE_SERVER_NAME=1;
my $fac = util::siteFactory->new;

# instantiate T::T object for site html
my $template = $fac->create_plain_template(
	'INCLUDE_PATH' => [ 
		$fac->prefix . $fac->subtree, 
		$fac->include 
	]
);

# if cwd is under svn, and doesn't hold only a README.html
my $svnPath;
my $SVN = $ENV{'SVN'} || 'svn';
my $svnStat = `$SVN stat`;
opendir my $dh, $fac->prefix . $fac->subtree or die $!;
my @files = grep { $_ !~ /^\./ } readdir($dh);
my $dirIsReadmeOnly = ( @files == 1 && $files[0] eq 'README.html' );
if ( $svnStat !~ /is not a working copy/ && ! $dirIsReadmeOnly ) {
    $svnPath = 'https://nexml.svn.sourceforge.net/svnroot/nexml/trunk' . $fac->subtree;
}

# make title
my $subtree = $fac->subtree;
$subtree =~ s|.+/([^/]+)/|$1|;
my $title = ucfirst $subtree;

# variables to be interpolated in templates
my $vars = $fac->create_template_vars(
    'title'       => 'nexml - index of ' . $fac->subtree,
    'mainHeading' => $title,
    'svnPath'     => $svnPath,
    'svnStat'     => $svnStat,
);

# if there is a README.html
if ( -e $fac->prefix . $fac->subtree . 'README.html' ) {
     $template->process( 'README.html', $vars, \$vars->{'README'} );
}

my $cgi = CGI->new;
print $cgi->header;
$template->process( 'header.tmpl', $vars ) || die $template->error();
if ( $dirIsReadmeOnly ) {
	print '<div style="display:none">';
}
else {
	print '<div class="directoryIndex">';
}
