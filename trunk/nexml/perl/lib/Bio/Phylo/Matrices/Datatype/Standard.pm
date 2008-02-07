# $Id: Standard.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Matrices::Datatype::Standard;
use Bio::Phylo::Matrices::Datatype;
use strict;
use vars qw($LOOKUP @ISA $MISSING $GAP);
@ISA = qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Standard - Datatype subclass,
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

 $Id: Standard.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

$LOOKUP = {
    '0' => [ '0' ],
    '1' => [ '1' ],
    '2' => [ '2' ],
    '3' => [ '3' ],
    '4' => [ '4' ],
    '5' => [ '5' ],
    '6' => [ '6' ],
    '7' => [ '7' ],
    '8' => [ '8' ],
    '9' => [ '9' ],
};

$MISSING = '?';

1;