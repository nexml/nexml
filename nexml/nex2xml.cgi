#!/usr/bin/perl
use CGI::Carp 'fatalsToBrowser';
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/5.8.6/darwin-thread-multi-2level/';
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib';
    unshift @INC, '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
}
use strict;
use warnings;
use util;
use Template;
use File::Temp;
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;
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
    '-class' => 'Bio::Phylo::Parsers::Nexus'
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
    ( $filename, @lines ) = read_file( $q->param('file') );
    $blocks = parse( 
        '-format' => 'nexus',
        '-string' => join("\n",@lines),
    )
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
    if ( UNIVERSAL::isa( $blocks, 'ARRAY' ) and $blocks->[0] ) {
        print header('application/xml', 201);
        print $blocks->[0]->get_root_open_tag;
        for my $block ( @{ $blocks } ) {
            print $block->to_xml;
        }
        print $blocks->[-1]->get_root_close_tag;
        exit(0);
    }
    else {
        push @exceptions, "No public nexus blocks in upload";    
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
		open my $fh, '<', $file or die "Can't open file to validate: $!";
		@lines = <$fh>;
		close $fh;
	}
	my ( $fh, $filename ) = File::Temp::tempfile;
	$fh->print( @lines );
	$fh->close;
	return $filename, @lines;
}