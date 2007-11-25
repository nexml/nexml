#!/usr/bin/perl
BEGIN {
    unshift @INC, 'perl/lib';
    unshift @INC, '../phylo/lib'; # i.e. the latest Bio::Phylo on CIPRES svn, in framework/perl
}
use strict;
use warnings;
use CGI ':standard';
use CGI::Carp 'fatalsToBrowser';
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;
use HTML::Entities;

my $q = CGI->new; # the CGI object, parses request parameters, generates response markup
my $logger = Bio::Phylo::Util::Logger->new; # the logger, transmits messages from Bio::Phylo
my @logmessages; # will hold marked up logging messages
my $code = '201 Created'; # will hold code as per http://users.sdsc.edu/~lcchan/rest-api-table1.html
my @lines; # will hold lines in the file

####################################################################################################
# LOGGER CONFIGURATION

# set logger to transmit messages at all levels
$logger->VERBOSE( 
    '-level' => 4, 
    '-class' => 'Bio::Phylo::Parsers::Nexml' 
);

# closure to turn log messages into html
my $make_html_msg = sub {
    my ( $level, $msg, $line ) = @_;
    my $link = defined $line ? ' at line ' . a( { '-href' => '#line' . $line }, $line ) : '';
    return pre(
        { '-class' => lc( $level ) },
        encode_entities( $msg ) . $link
    );
};

# attach a listener that passes log messages thru make_html_msg closure and pushes onto stack
$logger->set_listeners(
    sub {
        # check Bio::Phylo::Util::Logger for details on this argument stack
        my ( $log_string, $method, $subroutine, $filename, $line, $msg ) = @_;
        
        # these will hold line, column and byte location of xml parser 
        my ( $xline, $xcol, $xbyte );
        
        # log messages are prefixed with 1:2:3 for line 1, col 2, byte 3
        if ( $msg =~ m/^(\d+):(\d+):(\d+)\b/ ) {
            ( $xline, $xcol, $xbyte ) = ( $1, $2, $3 );
            $msg =~ s/^(\d+:\d+:\d+)\s*//;
        }
        
        # appends <pre class="info">$msg at line <a href="#line12">12</a></pre>
        push @logmessages, $make_html_msg->( $method, $msg, $xline );
    }
);

####################################################################################################
# PARSING

my $file = $q->upload('file') || shift(@ARGV); # file name can also be provided on command line
if ( not defined $file ) {
	$@ = 'No file specified';
}
else {
	if ( fileno( $file ) ) {
		@lines = <$file>;
	}
	else {
		open my $fh, '<', $file or die $!;
		@lines = <$fh>;
		close $fh;
	}
	eval { 
		parse( 
			'-format' => 'nexml', 
			'-string' => join( '', @lines ),
		) 
	};
}
if ( $@ ) {
    $code = '400 Bad Request';
    my $error = UNIVERSAL::can( $@, 'error' ) ? $@->error : 'Probably not xml: ' . $@;
    my $line  = UNIVERSAL::can( $@, 'line' ) ? $@->line : 1;
    push @logmessages, $make_html_msg->( 'fatal', $error, $line );
}

####################################################################################################
# WRITE RESULTS

print header('text/html', $code); # response code (201 or 400) here as second arg
print start_html(
    '-title' => 'nexml validation results',
    '-style' => { 
        '-src'  => 'style.css',
        '-type' => 'text/css',
    },
    '-script' => { 
        '-type' => 'text/javascript',
        '-src'  => 'script.js', 
    },
);
my @links;
for ( qw(debug info warn error fatal) ) {
    push @links, a( { '-href' => "javascript:toggle('$_')", '-class' => $_, }, $_ );
}
print 'Show: ', join '|', @links;
print @logmessages;
my $i = 0;
print table(
    map {
        Tr(
            td( pre( ++$i, a( { '-name' => 'line' . $i } ) ) ),
            td( pre( encode_entities( $_ ) ) )
        )
    } @lines
);
print end_html;