#!/usr/bin/perl
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use lib '../../perllib';	
    use lib '../../perllib/arch';
    use lib '../../bio-phylo/lib';
    unshift @INC, '../site/lib';
}
use util;
use util::siteFactory;
use strict;
use warnings;
use Template;
use File::Temp;
use CGI ':standard';
use UNIVERSAL 'isa';
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Util::Exceptions 'throw';
use constant MAX_SIZE => 2_000_000;
use constant COMPACT  =>   500_000;

my ( 
    @logmessages, # messages from the logger object, formatted for template
    @exceptions,  # trapped exception and stack traces, unformatted
    $project,     # nexus blocks, if any
    $filename,    # filename as returned from upload + read
    @lines,       # lines in file
    $bytes,       # bytes in file
    $status,      # status line
);

my $q = CGI->new;
my $logger = Bio::Phylo::Util::Logger->new;
$logger->VERBOSE(
    '-level' => 3,
    '-class' => 'Bio::Phylo::Parsers::Newick'
);
$logger->set_listeners(
    sub {
        my (
           $log_string, # the formatted log string
           $level,      # log level, i.e DEBUG, INFO, WARN, ERROR or FATAL
           $subroutine, # the calling subroutine
           $filename,   # filename where log method was called
           $line,       # line where log method was called
           $msg         # the unformatted message
        ) = @_;
        push @logmessages, {
            'level' => lc($level),
            'line'  => 0,
            'msg'   => $msg,
        };
    }
);
eval {
    ( $filename, $bytes, @lines ) = read_file( $q->param('file') );
    throw 'FileError' => "File too big, $bytes > " . MAX_SIZE . ' bytes' if $bytes > MAX_SIZE;
    $project = parse( 
        '-format'     => 'newick',
        '-string'     => join("\n",@lines),
        '-as_project' => 1,
    )
};
if ( $@ ) {
    if ( isa( $@, 'Bio::Phylo::Util::Exceptions' ) ) {
        $status = $@->error;
        push @exceptions, $@->error;
        for my $frame ( @{ $@->trace } ) {
            my $file   = $frame->[1];
            my $line   = $frame->[2];       
            my $method = $frame->[3];
            push @exceptions, "---STACK: ${method}\n";
        }
    }
    else {
        $status = $@;
        push @exceptions, $@; 
    }
};
if ( not $@ ) {
    if ( isa( $project, 'Bio::Phylo::Project' ) ) {
        print header('application/xml', 201);
        print $project->to_xml( '-compact' => $bytes > COMPACT );
        exit(0);
    }
    else {
        push @exceptions, "No newick data in upload";    
    }
}

my @formatted = @logmessages;
push @formatted, map { 
    {
        'level' => 'fatal',
        'line'  => 0,
        'msg'   => $_,
    }
} @exceptions;

####################################################################################################
# WRITE RESULTS
my $fac = util::siteFactory->new;

# Template::Toolkit object to write results
my $template = $fac->create_site_template;

# the paths object is a utility object that translates between
# server side paths (i.e. relative to system root) and browser 
# side paths (i.e. relative to docroot)
my $paths = $fac->create_path_handler;

# extended template variables
my $vars = $fac->create_template_vars(
    'currentFile' => '',
    'title'       => 'FAIL',
    'mainHeading' => 'FAIL',
    'logmessages' => \@formatted,
    'lines'       => \@lines,
    'styleSheets' => [ 'validator.css' ],
    'favicon'     => $paths->strip( $paths->include( 'cross.png' ) ),
);

# write http header
print $q->header(
    '-type'   => 'text/html', 
    '-status' => "400 $status",
);

# write results
$template->process( 'validator.tmpl', $vars ) || die $template->error();

# clean up temp file
unlink $filename;

sub read_file {
	my $file  = shift;
	my $bytes = 0;	
	my @lines;
	if ( $file ) {
        if ( fileno( $file ) ) {
            @lines = <$file>;
        }
        else {
            open my $fh, '<', $file or die "Can't open file to validate: $!";
            @lines = <$fh>;
            close $fh;
        }
	}
	else {
	    @lines = <>;
	}
	$bytes += length($_) for @lines;
	my ( $fh, $filename ) = File::Temp::tempfile;
	$fh->print( @lines );
	$fh->close;
	return $filename, $bytes, @lines;
}
