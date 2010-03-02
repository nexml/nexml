# $Id$
package Bio::Phylo::Matrices::Datatype::Restriction;
use Bio::Phylo::Matrices::Datatype ();
use strict;
use vars qw($LOOKUP @ISA $MISSING $GAP);
@ISA = qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Restriction - Validator subclass,
no serviceable parts inside

=head1 DESCRIPTION

The Bio::Phylo::Matrices::Datatype::* classes are used to validate data
contained by L<Bio::Phylo::Matrices::Matrix> and L<Bio::Phylo::Matrices::Datum>
objects.

=head2 METHODS

=over

=item get_ids_for_special_symbols()

Gets state-to-id mapping for missing and gap symbols

 Type    : Accessor
 Title   : get_ids_for_special_symbols
 Usage   : my %ids = %{ $obj->get_ids_for_special_symbols };
 Function: Returns state-to-id mapping
 Returns : An empty hash reference
 Args    : None
 Notes   : This method is here as an override
           because restriction site data has
           no missing or gap symbols, just
           presence/absence

=cut

    sub get_ids_for_special_symbols {
        return {};
    }

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Matrices::Datatype>

This class subclasses L<Bio::Phylo::Matrices::Datatype>.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

$LOOKUP = {
    '0' => [ '0' ],
    '1' => [ '1' ],
};

1;