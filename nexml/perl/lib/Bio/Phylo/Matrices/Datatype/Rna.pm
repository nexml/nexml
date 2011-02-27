# $Id$
package Bio::Phylo::Matrices::Datatype::Rna;
use Bio::Phylo::Matrices::Datatype ();
use strict;
use vars qw($LOOKUP @ISA $MISSING $GAP);
@ISA=qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Rna - Validator subclass,
no serviceable parts inside

=head1 DESCRIPTION

The Bio::Phylo::Matrices::Datatype::* classes are used to validate data
contained by L<Bio::Phylo::Matrices::Matrix> and L<Bio::Phylo::Matrices::Datum>
objects.

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Matrices::Datatype>

This class subclasses L<Bio::Phylo::Matrices::Datatype>.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 CITATION

If you use Bio::Phylo in published research, please cite it:

B<Rutger A Vos>, B<Jason Caravas>, B<Klaas Hartmann>, B<Mark A Jensen>
and B<Chase Miller>, 2011. Bio::Phylo - phyloinformatic analysis using Perl.
I<BMC Bioinformatics> B<12>:63.
L<http://dx.doi.org/10.1186/1471-2105-12-63>

=head1 REVISION

 $Id$

=cut

$LOOKUP = {
    'A' => [ 'A'                ],
    'C' => [ 'C'                ],
    'G' => [ 'G'                ],
    'U' => [ 'U'                ],
    'M' => [ 'A', 'C'           ],
    'R' => [ 'A', 'G'           ],
    'W' => [ 'A', 'U'           ],
    'S' => [ 'C', 'G'           ],
    'Y' => [ 'C', 'U'           ],
    'K' => [ 'G', 'U'           ],
    'V' => [ 'A', 'C', 'G'      ],
    'H' => [ 'A', 'C', 'U'      ],
    'D' => [ 'A', 'G', 'U'      ],
    'B' => [ 'C', 'G', 'U'      ],
    'X' => [ 'G', 'A', 'U', 'C' ],
    'N' => [ 'G', 'A', 'U', 'C' ],
};

$MISSING = '?';

$GAP = '-';

1;