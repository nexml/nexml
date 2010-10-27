#!/usr/bin/perl
# $Id$
BEGIN {
    use Config;
    $ENV{ $Config{'ldlibpthname'} } = '../expat/lib';
}
BEGIN {
    unshift @INC, '../perl/lib';   
    unshift @INC, '../site/lib';
    use lib '../../perllib';	
    use lib '../../perllib/arch';    
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
my ( $project, $error, $line );
if ( not defined $file ) {
	die 'No file specified';
}
my ( $filename, @lines ) = read_file( $file );
eval {
$project = parse( 
	'-file'       => $filename, 
	'-format'     => $q->param('informat'),
	'-as_project' => 1, 
);		
};
die $@ if $@;


print "Content-type: text/xml\n\n";
print $project->to_xml( '-compact' => $q->param('mode') eq 'compact' );

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
