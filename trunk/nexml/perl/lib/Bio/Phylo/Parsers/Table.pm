# $Id$
package Bio::Phylo::Parsers::Table;
use strict;
use Bio::Phylo::IO;
use Bio::Phylo::Matrices::Matrix;
use Bio::Phylo::Matrices::Datum;
use Bio::Phylo::Taxa;
use Bio::Phylo::Taxa::Taxon;
use vars qw(@ISA);

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::IO);

my $logger = Bio::Phylo->get_logger;

=head1 NAME

Bio::Phylo::Parsers::Table - Parses tab- (or otherwise) delimited matrices. No
serviceable parts inside.

=head1 DESCRIPTION

This module is used to import data and taxa from plain text files or strings.
The following additional argument must be used in the call
to L<Bio::Phylo::IO|Bio::Phylo::IO>:

 -type => (one of [DNA|RNA|STANDARD|PROTEIN|NUCLEOTIDE|CONTINUOUS])

In addition, these arguments may be used to indicate line separators (default
is "\n") and field separators (default is "\t"):

 -fieldsep => '\t',
 -linesep  => '\n'

=begin comment

 Type    : Constructor
 Title   : new
 Usage   : my $table = new Bio::Phylo::Parsers::Table;
 Function: Initializes a Bio::Phylo::Parsers::Table object.
 Returns : A Bio::Phylo::Parsers::Table object.
 Args    : none.

=end comment

=cut

sub _new {
    my $class = $_[0];
    my $self  = {};
    bless( $self, $class );
    return $self;
}

=begin comment

 Type    : parser
 Title   : from_handle(%options)
 Usage   : $table->_from_handle(%options);
 Function: Extracts data from file, populates matrix object
 Returns : A Bio::Phylo::Matrices::Matrix object.
 Args    : -handle   => (\*FH),
           -fieldsep => (record separator)
           -linesep  => (line separator)
           -type     => (data type)
 Comments:

=end comment

=cut

*_from_handle = \&_from_both;
*_from_string = \&_from_both;

sub _from_both {
    my $self   = shift;
    my %opts   = @_;
    my $matrix = Bio::Phylo::Matrices::Matrix->new(
        '-type' => $opts{'-type'},
    );
    my $taxa   = Bio::Phylo::Taxa->new;
    $taxa->set_matrix($matrix);
    $matrix->set_taxa($taxa);
    my ( $fieldre, $linere );

    if ( $opts{'-fieldsep'} ) {
        if ( $opts{'-fieldsep'} =~ /^\b$/ ) {
            $fieldre = qr/$opts{'-fieldsep'}/;
        }
        else {
            $fieldre = qr/\Q$opts{'-fieldsep'}/;
        }
    }
    else {
        $fieldre = qr/\t/;
    }
    if ( $opts{'-linesep'} ) {
        if ( $opts{'-linesep'} =~ /^\b$/ ) {
            $linere = qr/$opts{'-linesep'}/;
        }
        else {
            $linere = qr/\Q$opts{'-linesep'}/;
        }
    }
    else {
        $linere = qr/\n/;
    }
    if ( $opts{'-handle'} ) {
        while ( readline( $opts{'-handle'} ) ) {
            chomp;
            my @temp = split( $fieldre, $_ );
            my $taxon = Bio::Phylo::Taxa::Taxon->new( '-name' => $temp[0], );
            $taxa->insert($taxon);
            my $datum = Bio::Phylo::Matrices::Datum->new(
                '-name'  => $temp[0],
                '-type'  => uc $opts{'-type'},
                '-char'  => [ @temp[ 1, -1 ] ],
            );
            $datum->set_taxon($taxon);
            $taxon->set_data($datum);
            $matrix->insert($datum);
        }
    }
    elsif ( $opts{'-string'} ) {
        foreach my $line ( split( $linere, $opts{'-string'} ) ) {
            my @temp = split( $fieldre, $line );
            my $taxon = Bio::Phylo::Taxa::Taxon->new( '-name' => $temp[0], );
            $taxa->insert($taxon);
            my $datum = Bio::Phylo::Matrices::Datum->new(
                '-name' => $temp[0],
                '-type' => uc $opts{'-type'},
                #'-char' => [ @temp[ 1 .. $#temp ] ],
            );
            $datum->set_char(@temp[ 1 .. $#temp ]);
            $datum->set_taxon($taxon);
            $taxon->set_data($datum);
            $matrix->insert($datum);
        }
    }
    
    if ( $opts{'-project'} ) {
    	my $taxa = $matrix->make_taxa();
    	$opts{'-project'}->insert($taxa,$matrix);
    	return $opts{'-project'};
    }
    elsif ( $opts{'-as_project'} ) {
    	require Bio::Phylo::Project;
    	my $proj = Bio::Phylo::Project->new;
    	my $taxa = $matrix->make_taxa();
    	$proj->insert($taxa,$matrix);
    	return $proj;
    }
    else {
    	return $matrix;
    }
}

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The table parser is called by the L<Bio::Phylo::IO|Bio::Phylo::IO> object.
Look there to learn how to parse tab- (or otherwise) delimited matrices.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=back

=head1 REVISION

 $Id$

=cut

1;
