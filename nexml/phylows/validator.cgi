#!/usr/bin/perl
# $Id: validator.cgi 1723 2011-12-25 18:05:27Z rvos $
BEGIN {
    use lib '../../perllib';	
    use lib '../../perllib/arch';
    use lib '../../bio-phylo/lib';
    unshift @INC, '../site/lib';
}
use strict;
use warnings;
use Cwd;
use CGI ':standard';
use CGI::Carp 'fatalsToBrowser';
use File::Temp;
use File::Spec;
use HTML::Entities;
use IO::Handle;
use Scalar::Util 'blessed';
use Template;
use util;
use util::siteFactory;
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Util::Exceptions 'throw';

my $q = CGI->new; # the CGI object, parses request parameters, generates response markup
my $logger = get_logger(); # the logger, transmits messages from Bio::Phylo
my @logmessages; # will hold marked up logging messages
my $code = '201 Created'; # will hold code as per http://users.sdsc.edu/~lcchan/rest-api-table1.html
my @lines; # will hold lines in the file
my $title = 'nexml'; # will say "valid" or "invalid"
my $fac = util::siteFactory->new;
$logger->debug("created helper objects");

####################################################################################################
# PARSING
my $file = $q->upload('file') || shift(@ARGV); # file name can also be provided on command line
if ( not defined $file ) {
	$logger->error("couldn't fetch file name from query string or command line");
	$@ = 'No file specified';
}
else {
	my $filename;	
	( $filename, @lines ) = read_file( $file );
	$logger->debug("read file '$file', copied contents to '$filename'");
	eval { 
		my @cmd = make_java_cmd($filename);
		$logger->info("executing java validator");		
		my $output = `@cmd &> validator.log`;
		open my $fh, '<', 'validator.log' or die $!;
		while(<$fh>) {
			my $logline = $_;
			chomp($logline);
			if ( $logline =~ qr/^\[(.*?)\] :(\d+?):\d+?: (.*)$/ ) {
				my ( $level, $line, $msg ) = ( lc( $1 ), $2, $3 );				
				if ( $level eq 'error' ) {
					Bio::Phylo::Util::Exceptions::API->throw( 
						'line'  => $line, 
						'error' => $msg
					);
				}				
				push @logmessages, make_log_message( $level, $msg, $line );
			}			
		}
		$logger->info("executing perl validator");
		parse( '-format' => 'nexml', '-file' => $filename ); 
	};
}
if ( $@ ) {
    $code = '400 Bad Request';
    my ( $error, $line );
    if ( blessed $@ ) {
    	( $error, $line ) = ( $@->error, $@->line );
    }
    else {
    	( $error, $line ) = ( $@, 1 );
    }
    $logger->error($error);
    push @logmessages, make_log_message( 'fatal', $error, $line );
    $title .= ': FAIL';
}
else {
	$logger->info('validation succeeded');
	$title .= ': SUCCESS';
}

####################################################################################################
# WRITE RESULTS

# Template::Toolkit object to write results
my $template = $fac->create_site_template;

# the paths object is a utility object that translates between
# server side paths (i.e. relative to system root) and browser 
# side paths (i.e. relative to docroot)
my $paths = $fac->create_path_handler;

# extended template variables
my $vars = $fac->create_template_vars(
    'currentFile' => $file,
    'title'       => $title,
    'mainHeading' => $title,
    'logmessages' => \@logmessages,
    'lines'       => \@lines,
    'styleSheets' => [ 'validator.css' ],
    'favicon'     => $paths->strip( $paths->include( $title =~ qr/FAIL/ ? 'cross.png' : 'tick.png' ) ),
);

# write http header
print header('text/html', $code); # response code (201 or 400) here as second arg

# write results
$template->process( 'validator.tmpl', $vars ) || die $template->error();

=head1 SUBROUTINES

=over

=item make_java_cmd()

This subroutine constructs the command line arguments to invoke a Xerces-J grammar validator on 
an input file. This way we can re-use Terri's validation class and run files against the same 
validator used by Eclipse's Oxygen plugin, which we use to design the schema. The result of this 
subroutine is an array of arguments (@cmd), e.g.:

 @cmd = "java", "-classpath", "${base}/build:${base}/jars/xercesImpl.jar",
 "-Dxml=$xml", "-Dxsd=$xsd", "-Dns=$ns", "validator.XmlValidator";

$base is the base directory from which the class path is constructed, $xml is the name of the
file to validate, $xsd the name of the schema file, $ns is the namespace.

This list of commands can be passed to perl built-in functions such as system(@cmd), which returns
and hands back the exit code; exec(@cmd), which *never* returns, or: 

 open my $handle, join(' ', @cmd, '|');

Which will pipe the output of the java program back into $handle where it can be read as the 
output of an object implementing L<IO::Handle>.

 Type    : Subroutine
 Title   : make_java_cmd
 Usage   : my @cmd = make_java_cmd( $filename );
 Function: Constructs the command to invoke the Xerces-J grammar validator on an input file.
 Returns : An array of arguments, e.g. to pass to system()
 Args    : The arguments are positional, the first one is required, the rest are optional:
           * the path to the xml file to validate
           * the base location from which the classpath is built, default is `pwd`/java/validator
           * the path to the root schema file, default is `pwd`/xsd/nexml.xsd
           * the namespace to validate, default is 'http://www.nexml.org/2009'

=cut

sub make_java_cmd {
	my ( $xml, $base, $xsd, $ns ) = @_;
	$ns   = 'http://www.nexml.org/2009' if not $ns;
	$base = File::Spec->catdir( getcwd, 'downloads' ) if not $base;
	$xsd  = File::Spec->catfile( getcwd, 'xsd', 'nexml.xsd' ) if not $xsd;
	$xml  = 'infile.xml' if not $xml;	
	die if not -r $xml;
	my @cmd = (
		'java',
		'-classpath',
		"${base}/validator.jar",
		"-Dxml=$xml",
		"-Dxsd=$xsd",
		"-Dns=$ns",
		'validator.XmlValidator'
	);
	$logger->debug("created java validator invocation");
	return @cmd;
}

=item read_file()

 Type    : Subroutine
 Title   : read_file
 Usage   : my @lines = read_file( $file );
 Function: Reads $file into an array @lines
 Returns : Contents of $file, split on @lines
 Args    : $file is either a name or a handle

=cut

sub read_file {
	my $file = shift;
	$logger->debug("going to read file '$file'");
	my @lines;
	if ( fileno( $file ) ) {
		$logger->debug("reading from handle");
		@lines = <$file>;
	}
	else {
		$logger->debug("going to open handle");
		open my $fh, '<', $file or die "Can't open file to validate: $!";
		$logger->debug("reading from handle");
		@lines = <$fh>;
		close $fh;
	}
	my ( $fh, $filename ) = File::Temp::tempfile;
	$logger->debug("created temporary file '$filename'");
	$fh->print( @lines );
	$logger->debug("copied uploaded data to '$filename'");
	$fh->close;
	return $filename, @lines;
}

=item make_log_message()

 Type    : Subroutine
 Title   : make_log_message
 Usage   : my %msg = %{ make_log_message( $level, $msg, $line ) };
 Function: Makes a logmessage hash
 Returns : A hash keyed on 'level', 'msg' and 'line';
 Args    : $level = log level ('DEBUG', 'INFO' etc.)
 		   $msg = the log message
 		   $line = line the message applies to (if any)

=cut

sub make_log_message {
    my ( $level, $msg, $line ) = @_;
    if ( $line ) {
    	$msg .= " (line $line)";
    }
    return {
    	'level' => lc($level),
    	'msg'   => $msg,
    	'line'  => $line,
    };
}

=item get_logger()

 Type    : Subroutine
 Title   : get_logger
 Usage   : my $logger = get_logger();
 Function: Makes a logger object
 Returns : A logger object
 Args    : None

=cut

sub get_logger {
	my $logger = Bio::Phylo::Util::Logger->new;
	
	# set logger to transmit messages at all levels
	$logger->VERBOSE( 
		'-level' => 4, 
		'-class' => 'Bio::Phylo::Parsers::Nexml' 
	);
	$logger->VERBOSE(
		'-level' => 4,
		'-class' => 'main',
	);
	
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
			push @logmessages, make_log_message( $method, $msg, $xline );
		}
	);
	return $logger;

}

=back

=cut
