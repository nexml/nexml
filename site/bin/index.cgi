#!/usr/bin/perl
# $Id: index.cgi 337 2007-12-29 22:45:20Z rvos $
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use lib '../../../perllib';	
    use lib '../../../perllib/arch';
    unshift @INC, '../../perl/lib';
    unshift @INC, '../../site/lib';
}
use strict;
use warnings;
use xs::schema;
use util;
use util::siteFactory;
use Template;
use Cwd;
use Bio::Phylo::Util::Logger ':levels';

# set up logging, which we will write out as
# comments in the template footer
my $logMessages = [];
my $logger = Bio::Phylo::Util::Logger->new;
#$logger->VERBOSE( '-level' => DEBUG );
$logger->set_listeners(
	sub {
		my ($msg) = @_;
		push @{ $logMessages }, $msg;
	}
);

# unfortunately this has to be hardcoded, we can't
# use Sys::Hostname::hostname() because we're 
# generating docs from a cron job (so no CGI vars)
# and we'd be running on a virtual host anyway
my $hostname = $ENV{'SERVER_NAME'} || 'eupoa.local';
$logger->debug("hostname is $hostname");

# subtree for this part of the site structure, i.e.
# the schema documentation
my $subtree = $ENV{'STATIC_ROOT'} . '/doc/schema-1';
$logger->debug("subtree is $subtree");

# $prefix is the path to docroot, so on server-side
# includes we need it (hence it is part of $include),
# but on the client side (e.g. paths to images in an
# html page) it needs to be stripped
my $prefix = $ENV{'NEXML_HOME'};
$logger->debug("prefix is $prefix");

# $include is used to find server side includes, e.g.
# when we embed javascript or css directly into a page.
# on the client side we need to strip $prefix of it.
my $include = $prefix . '/nexml/html/include';
$logger->debug("include is $include");

# the root file of the schema, i.e. nexml.xsd
my $baseFile = $ARGV[0];
$logger->debug("baseFile is $baseFile");

# the current file we're processing, i.e. a file being
# included into nexml.xsd, recursively
my $currentFile = $ARGV[1] || $baseFile;
$logger->debug("currentFile is $currentFile");

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

# instantiate T::T object for site html
my $template = Template->new(
    'INCLUDE_PATH' => $include,      # or list ref
    'POST_CHOMP'   => 1,             # cleanup whitespace
    'PRE_PROCESS'  => 'header.tmpl', # prefix each template
    'POST_PROCESS' => 'footer.tmpl', # suffix each template
    'START_TAG'    => '<%',
    'END_TAG'      => '%>',
    'OUTPUT_PATH'  => $prefix,
);

# T::T object for dot, so no html/css/js inclusions
my $dottemplate = Template->new(
    'INCLUDE_PATH' => $include,      # or list ref
    'POST_CHOMP'   => 1,             # cleanup whitespace
    'START_TAG'    => '<%',
    'END_TAG'      => '%>',
    'OUTPUT_PATH'  => $prefix,
);

# variables to be interpolated in template
my $vars = {
    'schema'      => $schema,
    'currentFile' => $currentFile,
    'baseFile'    => $baseFile,
    'title'       => 'nexml schema 1.0',
    'mainHeading' => 'Overview',
    'currentURL'  => 'http://' . $hostname . $subtree,
    'currentDate' => my $time = localtime,
    'currentFeed' => $subtree . '/schema.rss',
    'paths'       => $paths,
    'hostName'    => $hostname,
    'logMessages' => $logMessages,
};

# create the root document
$template->process( 'overview.html', $vars, $subtree . '/index.html' ) || die $template->error();

# iterate over schema *.xsd files
for my $currentFile ( $schema->files ) {
    my $stripped = $paths->strip( $currentFile );
    $logger->debug("processing file $currentFile stripped as $stripped");
    
    # *.xsd file specific variables to be interpolated in template
    $vars->{'currentFile'} = $currentFile;
    $vars->{'title'}       = "nexml schema 1.0 ~" . $paths->strip( $currentFile );
    $vars->{'mainHeading'} = "Schema module documentation";
    $vars->{'currentURL'}  = 'http://' . $hostname . $paths->transform( $currentFile );
    $vars->{'currentDate'} = my $time = localtime;
    
    # create the *.xsd specific html file
    my $outFile = $paths->transform( $currentFile ) . 'index.html';  
    $logger->debug("outfile is $outFile");
    $template->process( 'schema.html', $vars, $outFile ) || die $template->error();
    
    my $dir = $paths->transform( $currentFile );
    $logger->debug("dir is $dir");
    for my $graphtype ( qw(inheritance inclusions) ) {  
        write_image_map( $dir, $graphtype, $dottemplate, $vars, $prefix );
    }
}

sub write_image_map {
    my ( $dir, $graphtype, $dottemplate, $vars, $prefix ) = @_;
    $vars->{'graphtype'} = $graphtype; # to put in templates
    
    # first generate the dot graph file by expanding the template vars
    my $templateFile = 'dot' . $graphtype . '.tmpl';   # dotinclusions.tmpl (no path)
    my $dotFile = $dir . $graphtype . '.dot';          # /path/to/inclusions.dot
    $logger->debug("writing $dotFile from $templateFile");
    $dottemplate->process( $templateFile, $vars, $dotFile ) || die $dottemplate->error();
    
    # generate the html file that will hold the image map
    my $htmlFile = $dir . $graphtype . '/index.html'; # /path/to/inclusions.html
    $logger->debug("writing $htmlFile from imageMap.tmpl");
    $dottemplate->process( 'imageMap.tmpl', $vars, $htmlFile ) || die $dottemplate->error();
    
    # the dot executable can either be an env var, or we hope to find it on the path
    my $dot = $ENV{'DOT'} || 'dot';
    
    # we generate a *.map file and a *.png
    for my $filetype ( qw(imap png) ) {
    
        # turn type flag into file extension
        my $extension = $filetype;
        $extension =~ s/i//;
        my $outfile = $dir . $graphtype . '/' . $graphtype . '.' . $extension; # /path/to/inclusions.png
        system( $dot, '-q5', "-T${filetype}", "-o${prefix}${outfile}", $prefix . $dotFile );
        $logger->debug("running dot for $outfile");
    }  
}

# rewrite rule to turn code from which we're creating documentation
# into relative urls pointing to the generated docs
sub rewrite_xsd {
    my $file = shift;
    $logger->debug("rewriting $file");
    
    # this rule only applies to files with the xsd extension
    if ( $file =~ m/\.xsd(#.*?)?$/ ) {
    
        # first standardize path into full path
        my $realpath = Cwd::realpath( $file );
        $logger->debug("realpath is $realpath");
	if ( $realpath =~ m|^/home/| ) {
		$logger->debug("real path starts with /home/, should be /home2/");
		$realpath =~ s|^/home/|/home2/|;
	}
        
        # strip $prefix to docroot
        $realpath =~ s/^\Q$prefix\E//;
        $logger->debug("stripped $prefix from $realpath");
        
        # transform folder path to point to doc path
        $realpath =~ s|^/nexml/xsd|$subtree|;
        $logger->debug("transformed to point to $realpath by prefixing with $subtree");
        
        # strip file extension, turn file into folder
        if ( $realpath =~ m|.xsd(#.+?)$| ) {
            $realpath =~ s|.xsd(#.+?)$|/$1|;
        }
        else {
            $realpath =~ s|.xsd$|/|;
        }
        $logger->debug("turned into directory $realpath");
        
        return $realpath;
    }
    else {
        return $file;
    }
}
