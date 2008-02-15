#!/usr/bin/perl
# $Id: validator.cgi 424 2008-02-07 05:06:10Z rvos $
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/5.8.6/darwin-thread-multi-2level/';
    use lib $ENV{'DOCUMENT_ROOT'} . '/lib/lib/perl5/site_perl/';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/perl/lib';
    unshift @INC, $ENV{'DOCUMENT_ROOT'} . '/nexml/site/lib'; 
    unshift @INC, '../perl/lib';   
    unshift @INC, '../site/lib';
    push @INC, '/Users/rvosa/CIPRES-and-deps/cipres/build/lib/perl/lib';
}
use strict;
use warnings;
use CGI ':standard';
use CGI::Carp 'fatalsToBrowser';
use File::Temp;
use Scalar::Util 'blessed';
use util;
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Util::Exceptions 'throw';

my $q = CGI->new; # the CGI object, parses request parameters, generates response markup

# process file
my $file = $q->upload('file') || shift(@ARGV); # file name can also be provided on command line
my ( $blocks, $error, $line );
if ( not defined $file ) {
	$@ = 'No file specified';
}
else {
	my ( $filename, @lines ) = read_file( $file );
	eval {
		$blocks = parse( 
			'-file'   => $filename, 
			'-format' => $q->param('informat') 
		);
	};
}

# create output
if ( $@ ) {
    if ( blessed $@ ) {
    	( $error, $line ) = ( $@->error, $@->line );
    }
    else {
    	( $error, $line ) = ( $@, 1 );
    }
}
else {
	
}

print "Content-type: text/xml\n\n";
if ( blessed $blocks ) {
	
}
else {
	my $xml = '';
	$xml .= $blocks->[0]->get_root_open_tag;
	for my $block ( @{ $blocks } ) {
		my $tmp;
		eval {
			$tmp = $block->to_xml;
		};
		if ( $@ ) {
			#print $@;
		}
		else {
			$xml .= $tmp;
		}
	} 
	$xml .= $blocks->[0]->get_root_close_tag;
	print $xml; 
	#print $open, map { print $_->to_xml } @$blocks, $close;
}

=head1 SUBROUTINES

=over

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

=back

=cut
