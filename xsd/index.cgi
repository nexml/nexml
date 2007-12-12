#!/usr/bin/perl
# $Id$
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use lib '../perl/lib';
}
use lib '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
use strict;
use warnings;
use xs::schema;
use util 'svninfo';
use Template;
use Cwd;

# unfortunately this has to be hardcoded, we can't
# use Sys::Hostname::hostname() because we're 
# generating docs from a cron job (so no CGI vars)
# and we'd be running on a virtual host anyway
my $hostname = $ENV{'NEXML_HOSTNAME'} || 'eupoa.local';

# subtree for this part of the site structure, i.e.
# the schema documentation
my $subtree = $ENV{'NEXML_SUBTREE'} || '/nexml/html/doc/schema-1';

# $prefix is the path to docroot, so on server-side
# includes we need it (hence it is part of $include),
# but on the client side (e.g. paths to images in an
# html page) it needs to be stripped
my $prefix;
if ( $ENV{'NEXML_HOME'} ) {
    $prefix = $ENV{'NEXML_HOME'};
}
elsif ( -d '/Users/rvosa/Documents/workspace' ) {
    $prefix = '/Users/rvosa/Documents/workspace';
}
else {
    $prefix = $ENV{'HOME'};
}

# $include is used to find server side includes, e.g.
# when we embed javascript or css directly into a page.
# on the client side we need to strip $prefix of it.
my $include = $prefix . '/nexml/html/include';

# the root file of the schema, i.e. nexml.xsd
my $baseFile = Cwd::realpath( $ARGV[0] );

# the current file we're processing, i.e. a file being
# included into nexml.xsd, recursively
my $currentFile = $ARGV[1] ? Cwd::realpath( $ARGV[1] ) : $baseFile;

# a function exported by util.pm, does a `svninfo` and
# turns the results into a hash so we can put it in a page
my %svninfo = svninfo($currentFile);

# the schema object, creates a representation of the nexml
# schema which we can place in the templates in an MVC-style
my $schema = xs::schema->new( $baseFile );

# the paths object is a utility object that translates between
# server side paths (i.e. relative to system root) and browser 
# side paths (i.e. relative to docroot)
my $paths = util::paths->new(
    '-prefix'   => $prefix,
    '-include'  => $include,
    '-rewrite'  => [ \&rewrite_xsd ],
);

my $template = Template->new(
    'INCLUDE_PATH' => $include,      # or list ref
    'POST_CHOMP'   => 1,             # cleanup whitespace
    'PRE_PROCESS'  => 'header.tmpl', # prefix each template
    'POST_PROCESS' => 'footer.tmpl', # suffix each template
    'START_TAG'    => '<%',
    'END_TAG'      => '%>',
    'OUTPUT_PATH'  => $prefix,
);

# for dot
my $dottemplate = Template->new(
    'INCLUDE_PATH' => $include,      # or list ref
    'POST_CHOMP'   => 1,             # cleanup whitespace
    'START_TAG'    => '<%',
    'END_TAG'      => '%>',
    'OUTPUT_PATH'  => $prefix,
);

my $vars = {
    'schema'      => $schema,
    'currentFile' => $currentFile,
    'baseFile'    => $baseFile,
    'svninfo'     => \%svninfo,
    'title'       => 'nexml schema 1.0',
    'mainHeading' => 'Overview',
    'currentURL'  => 'http://' . $hostname . $subtree,
    'currentDate' => my $time = localtime,
    'currentFeed' => $subtree . '/schema.rss',
    'paths'       => $paths,
};

$template->process( 'overview.html', $vars, $subtree . '/index.html' ) || die $template->error();

for my $currentFile ( $schema->files ) {
    my $stripped = $paths->strip( $currentFile );
    
    $vars->{'currentFile'} = $currentFile;
    $vars->{'title'}       = "nexml schema 1.0 ~" . $paths->strip( $currentFile );
    $vars->{'mainHeading'} = "Schema module documentation";
    $vars->{'currentURL'}  = 'http://' . $hostname . $paths->transform( $currentFile );
    $vars->{'currentDate'} = my $time = localtime;
    
    my $outFile = $paths->transform( $currentFile ) . 'index.html';
    my $outDot = $paths->transform( $currentFile ) . 'index.dot';
    
    $template->process( 'schema.html', $vars, $outFile ) || die $template->error();
    $dottemplate->process( 'dotinheritance.tmpl', $vars, $outDot ) || die $template->error();

}

# rewrite rule to turn code from which we're creating documentation
# into relative urls pointing to the generated docs
sub rewrite_xsd {
    my $file = shift;
    
    # this rule only applies to files with the xsd extension
    if ( $file =~ m/\.xsd(#.*?)?$/ ) {
    
        # first standardize path into full path
        my $realpath = Cwd::realpath( $file );
        
        # strip $prefix to docroot
        $realpath =~ s/^\Q$prefix\E//;
        
        # transform folder path to point to doc path
        $realpath =~ s|^/nexml/xsd|$subtree|;
        
        # strip file extension, turn file into folder
        if ( $realpath =~ m|.xsd(#.+?)$| ) {
            $realpath =~ s|.xsd(#.+?)$|/$1|;
        }
        else {
            $realpath =~ s|.xsd$|/|;
        }
        
        return $realpath;
    }
    else {
        return $file;
    }
}