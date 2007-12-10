#!/usr/bin/perl
use lib '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
use strict;
use warnings;
use xs::schema;
use util 'svninfo';
use Template;
use Cwd;

# $prefix is the path to docroot, so on server-side
# includes we need it (hence it is part of $include),
# but on the client side (e.g. paths to images in an
# html page) it needs to be stripped
my $prefix = '/Users/rvosa/Documents/workspace';

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

# the schema object,
my $schema = xs::schema->new( $baseFile );

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

my $vars = {
    'schema'      => $schema,
    'currentFile' => $currentFile,
    'baseFile'    => $baseFile,
    'svninfo'     => \%svninfo,
    'title'       => 'nexml schema version 1.0: overview',
    'mainHeading' => 'Overview',
    'currentURL'  => 'http://fixme.org',
    'currentDate' => my $time = localtime,
    'currentFeed' => undef,
    'paths'       => $paths,
};

$template->process( 'overview.html', $vars, '/nexml/html/doc/schema-1/index.html' ) || die $template->error();

for my $currentFile ( $schema->files ) {
    my $stripped = $paths->strip( $currentFile );
    
    $vars->{'currentFile'} = $currentFile;
    $vars->{'title'}       = "nexml schema version 1.0: ~" . $paths->strip( ${currentFile} );
    $vars->{'mainHeading'} = "Schema module <a href=\"${stripped}\">~${stripped}</a>";
    $vars->{'currentURL'}  = 'http://fixme.org';
    $vars->{'currentDate'} = my $time = localtime;
    
    my $outFile = $paths->transform( $currentFile ) . 'index.html';
    
    $template->process( 'schema.html', $vars, $outFile ) || die $template->error();

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
        $realpath =~ s|^/nexml/xsd|/nexml/html/doc/schema-1|;
        
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