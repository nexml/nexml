# $Id: Taxlist.pm 1235 2010-03-02 16:11:07Z rvos $
package Bio::Phylo::Parsers::Phylip;
use strict;
use Bio::Phylo::Factory;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::IO ();
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::IO);

my $fac = Bio::Phylo::Factory->new;

=head1 NAME

Bio::Phylo::Parsers::Phylip - Parser used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module is used for parsing PHYLIP character state matrix files. As PHYLIP files
don't indicate what data type they are you should indicate this as an argument to the
Bio::Phylo::IO::parse function, i.e.:

 use Bio::Phylo::IO 'parse';
 my $file = shift @ARGV;
 my $type = 'dna'; # or rna, protein, restriction, standard, continuous
 my $matrix = parse(
 	'-file'   => $file,
 	'-format' => 'phylip',
 	'-type'   => $type,
 )->[0];
 print ref($matrix); # probably prints Bio::Phylo::Matrices::Matrix;

=begin comment

 Type    : Constructor
 Title   : new
 Usage   : my $taxlist = Bio::Phylo::Parsers::Taxlist->new;
 Function: Initializes a Bio::Phylo::Parsers::Taxlist object.
 Returns : A Bio::Phylo::Parsers::Taxlist object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = $_[0];
    my $self  = {};
    bless $self, $class;
    return $self;
}

=begin comment

 Type    : parser
 Title   : from_handle(%options)
 Usage   : $taxlist->from_handle(%options);
 Function: Reads taxon names from file, populates taxa object
 Returns : A Bio::Phylo::Taxa object.
 Args    : -handle => (\*FH), -file => (filename)
 Comments:

=end comment

=cut

*_from_handle = \&_from_both;
*_from_string = \&_from_both;

sub _from_both {
    my $self = shift;
    my %opts = @_;
	my $factory = $opts{'-factory'} || $fac;
	my $type    = $opts{'-type'} || 'standard';
	my $handle;
	if ( $opts{'-handle'} ) {
		$handle = $opts{'-handle'};
	}
	elsif ( $opts{'-file'} ) {
		open $handle, '<', $opts{'-file'} or throw 'FileError' => $!;
	}
	elsif ( $opts{'-string'} ) {
		open $handle, '<', \$opts{'-string'} or throw 'BadArgs' => $!;
	}
	else {
		throw 'BadArgs' => 'Need a data source to parse';
	}
	my ( $ntax, $nchar );
	my $matrix = $factory->create_matrix( '-type' => $type );
	my $to = $matrix->get_type_object;
	while(<$handle>) {
		if ( /^\s*(\d+)\s+(\d+)\s*$/ && ! $ntax && ! $nchar ) {
			( $ntax, $nchar ) = ( $1, $2 );
		}
		elsif ( /\S+/ ) {
			my $line = $_;
			my $name = substr( $line, 0, 10 );
			my $seq = substr( $line, 10 );
			$matrix->insert( $factory->create_datum(
				'-type' => $type,
				'-name' => $name,
				'-char' => $to->split($seq),
			) );
		}
	}
	if ( $opts{'-project'} ) {
		return $opts{'-project'}->insert($matrix);
	}
	elsif ( $opts{'-as_project'} ) {
		return $factory->create_project->insert($matrix);
	}
	else {
		return [ $matrix ];
	}
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The PHYLIP parser is called by the L<Bio::Phylo::IO> object.
Look there for examples.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Taxlist.pm 1235 2010-03-02 16:11:07Z rvos $

=cut

1;
