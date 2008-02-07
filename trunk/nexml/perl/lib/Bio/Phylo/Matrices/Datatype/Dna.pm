# $Id: Dna.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Matrices::Datatype::Dna;
use Bio::Phylo::Matrices::Datatype;
use strict;
use vars qw($LOOKUP @ISA $MISSING $GAP);
@ISA = qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Dna - Datatype subclass,
no serviceable parts inside

=head1 DESCRIPTION

The Bio::Phylo::Matrices::Datatype::* classes are used to validate data
contained by L<Bio::Phylo::Matrices::Matrix> and L<Bio::Phylo::Matrices::Datum>
objects.

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Matrices::Datatype>

This class subclasses L<Bio::Phylo::Matrices::Datatype>.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Dna.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

$LOOKUP = {
    'A' => [ 'A'                ],
    'C' => [ 'C'                ],
    'G' => [ 'G'                ],
    'T' => [ 'T'                ],
    'M' => [ 'A', 'C'           ],
    'R' => [ 'A', 'G'           ],
    'W' => [ 'A', 'T'           ],
    'S' => [ 'C', 'G'           ],
    'Y' => [ 'C', 'T'           ],
    'K' => [ 'G', 'T'           ],
    'V' => [ 'A', 'C', 'G'      ],
    'H' => [ 'A', 'C', 'T'      ],
    'D' => [ 'A', 'G', 'T'      ],
    'B' => [ 'C', 'G', 'T'      ],
    'X' => [ 'G', 'A', 'T', 'C' ],
    'N' => [ 'G', 'A', 'T', 'C' ],
};

$MISSING = '?';

$GAP = '-';

1;