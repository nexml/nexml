# $Id: Rna.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Matrices::Datatype::Rna;
use Bio::Phylo::Matrices::Datatype;
use strict;
use vars qw($LOOKUP @ISA $MISSING $GAP);
@ISA=qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Rna - Datatype subclass,
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

 $Id: Rna.pm 4786 2007-11-28 07:31:19Z rvosa $

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