#!/usr/bin/perl
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use lib '../../perllib';	
    use lib '../../perllib/arch';
    use lib '../../bio-phylo/lib';
    unshift @INC, '../site/lib';
}
use strict;
use warnings;
use util;
use util::siteFactory;
use Bio::Phylo::Util::Logger;
use Template;
use File::Temp;
use XML::XML2JSON;
use CGI ':standard';

my ( 
    @logmessages, # messages from the logger object, formatted for template
    @exceptions,  # trapped exception and stack traces, unformatted
    $blocks,      # nexus blocks, if any
    $filename,    # filename as returned from upload + read
    @lines        # lines in file
);

my $q = CGI->new;
my $logger = Bio::Phylo::Util::Logger->new;
$logger->VERBOSE(
    '-level' => 4,
    '-class' => 'main'
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
    ( $filename, @lines ) = read_file( $q->param('file') || \*STDIN );
    my $xml2json = XML::XML2JSON->new;
    $blocks = $xml2json->convert(join '', @lines);
};
if ( $@ ) {
    if ( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions' ) ) {
        push @exceptions, $@->error . ' (' . $@->description . ')';
        for my $frame ( @{ $@->trace } ) {
            my $file = $frame->[1];
            my $line = $frame->[2];       
            my $method = $frame->[3];
            push @exceptions, "---STACK: [${method}]\n";
        }
    }
    else {
        push @exceptions, $@; 
    }
};
if ( not $@ ) {
    print header('application/json', 201);
    print $blocks;
    exit(0);
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
print header('text/html', 400); # response code (201 or 400) here as second arg

# write results
$template->process( 'validator.tmpl', $vars ) || die $template->error();

sub read_file {
	my $file = shift;
	my @lines;
	if ( fileno( $file ) ) {
		@lines = <$file>;
	}
	else {
		open my $fh, '<', $file or die "Can't open file to translate: $!";
		@lines = <$fh>;
		close $fh;
	}
	my ( $fh, $filename ) = File::Temp::tempfile;
	$fh->print( @lines );
	$fh->close;
	return $filename, @lines;
}
