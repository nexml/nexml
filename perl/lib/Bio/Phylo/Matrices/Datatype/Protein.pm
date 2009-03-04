# $Id$
package Bio::Phylo::Matrices::Datatype::Protein;
use Bio::Phylo::Matrices::Datatype;
use strict;
use vars qw($LOOKUP @ISA $MISSING $GAP);
@ISA = qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Protein - Validator subclass,
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

=head1 FORUM

CPAN hosts a discussion forum for Bio::Phylo. If you have trouble
using this module the discussion forum is a good place to start
posting questions (NOT bug reports, see below):
L<http://www.cpanforum.com/dist/Bio-Phylo>

=head1 REVISION

 $Id$

=cut

$LOOKUP = {
    'A' => [ 'A'      ],
    'B' => [ 'D', 'N' ],
    'C' => [ 'C'      ],
    'D' => [ 'D'      ],
    'E' => [ 'E'      ],
    'F' => [ 'F'      ],
    'G' => [ 'G'      ],
    'H' => [ 'H'      ],
    'I' => [ 'I'      ],
    'K' => [ 'K'      ],
    'L' => [ 'L'      ],
    'M' => [ 'M'      ],
    'N' => [ 'N'      ],
    'P' => [ 'P'      ],
    'Q' => [ 'Q'      ],
    'R' => [ 'R'      ],
    'S' => [ 'S'      ],
    'T' => [ 'T'      ],
    'U' => [ 'U'      ],
    'V' => [ 'V'      ],
    'W' => [ 'W'      ],
    'X' => [ 'X'      ],
    'Y' => [ 'Y'      ],
    'Z' => [ 'E', 'Q' ],
    '*' => [ '*'      ],
};

$MISSING = '?';

$GAP = '-';
            
1;