# $Id$
package Bio::Phylo::Parsers::Phylip;
use strict;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Parsers::Abstract;
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::Parsers::Abstract);

=head1 NAME

Bio::Phylo::Parsers::Phylip - Parser used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

This module is used for parsing PHYLIP character state matrix files. At present this only
works on non-interleaved files. As PHYLIP files don't indicate what data type they are you 
should indicate this as an argument to the Bio::Phylo::IO::parse function, i.e.:

 use Bio::Phylo::IO 'parse';
 my $file = shift @ARGV;
 my $type = 'dna'; # or rna, protein, restriction, standard, continuous
 my $matrix = parse(
 	'-file'   => $file,
 	'-format' => 'phylip',
 	'-type'   => $type,
 )->[0];
 print ref($matrix); # probably prints Bio::Phylo::Matrices::Matrix;

=cut

sub _parse {
    my $self = shift;
	my $factory = $self->_factory;
	my $type    = $self->_args->{'-type'} || 'standard';
	my $handle  = $self->_handle;
	my $matrix  = $factory->create_matrix( '-type' => $type );
	my ( $ntax, $nchar );	
	while(<$handle>) {
		if ( /^\s*(\d+)\s+(\d+)\s*$/ && ! $ntax && ! $nchar ) {
			( $ntax, $nchar ) = ( $1, $2 );
		}
		elsif ( /\S/ ) {
			my $name = substr( $_, 0, 10 );
			my $seq  = substr( $_, 10 );
			$matrix->insert( $factory->create_datum(
				'-type' => $type,
				'-name' => $name,
				'-char' => $matrix->get_type_object->split($seq),
			) );
		}
	}
	my ( $my_nchar, $my_ntax ) = ( $matrix->get_nchar, $matrix->get_ntax );
	$nchar != $my_nchar && throw 'BadFormat' => "observed ($my_nchar) != expected ($nchar) nchar";
	$ntax  != $my_ntax  && throw 'BadFormat' => "observed ($my_ntax) != expected ($ntax) ntax";
	return $matrix;
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

 $Id$

=cut

1;
