# $Id: Table.pm 1524 2010-11-25 19:24:12Z rvos $
package Bio::Phylo::Parsers::Fasta;
use strict;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Parsers::Abstract;
use vars qw(@ISA);

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::Parsers::Abstract);

=head1 NAME

Bio::Phylo::Parsers::Fasta - Parser used by Bio::Phylo::IO, no serviceable parts inside

=head1 DESCRIPTION

A very symplistic FASTA file parser. To use it, you need to pass an argument
that specifies the data type of the FASTA records into the parse function, i.e.

 my $project = parse(
    -type   => 'dna', # or rna, protein
    -format => 'fasta',
    -file   => 'infile.fa',
    -as_project => 1
 );

For each FASTA record, the first "word" on the definition line is used as the
name of the produced datum object. The entire line is assigned to:

 $datum->set_generic( 'fasta_def_line' => $line )
 
So you can retrieve it by calling:

 my $line = $datum->get_generic('fasta_def_line');

BioPerl actually parses definition lines to get GIs and such out of there, so if
you're looking for that, use L<Bio::SeqIO> from the bioperl-live distribution.
You can always pass the resulting Bio::Seq objects to
Bio::Phylo::Matrices::Datum->new_from_bioperl to turn the L<Bio::Seq> objects
that Bio::SeqIO produces into L<Bio::Phylo::Matrices::Datum> objects. 

=cut

sub _parse {
    my $self = shift;
    my $fh   = $self->_handle;
    my $fac  = $self->_factory;
    my $type = $self->_args->{'-type'} or throw 'BadArgs' => 'No data type specified!';    
    my $matrix = $fac->create_matrix( '-type' => $type );
    my ( $seq, $datum );
    while(<$fh>) {
        chomp;
        my $line = $_;
        if ( $line =~ />(\S+)/ ) {
            my $name = $1;            
            if ( $seq && $datum ) {
                $matrix->insert( $datum->set_char($seq) );
            }
            $datum = $fac->create_datum(
                '-type' => $type,
                '-name' => $name,
                '-generic' => { 'fasta_def_line' => $line }
            );
            $seq = '';
        }
        else {
            $seq .= $line;
        }
    }
    return $matrix;
	
}

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The fasta parser is called by the L<Bio::Phylo::IO|Bio::Phylo::IO> object.
Look there to learn more about parsing.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=back

=head1 REVISION

 $Id: Table.pm 1524 2010-11-25 19:24:12Z rvos $

=cut

1;
