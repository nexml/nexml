package Bio::Phylo::Matrices::Character;
use strict;
use base 'Bio::Phylo::Matrices::TypeSafeData';
use Bio::Phylo::Util::CONSTANT qw'_CHARACTER_ _CHARACTERS_';

=head1 NAME

Bio::Phylo::Matrices::Character - A character (column) in a matrix

=head1 SYNOPSIS

 # No direct usage

=head1 DESCRIPTION

Objects of this type represent a single character in a matrix. By default, a
matrix will adjust the number of such objects it requires automatically as its
contents grow or shrink. The main function, at present, for objects of this
type is to facilitate NeXML serialization of characters and their annotations.

=head1 METHODS

=head2 SERIALIZERS

=over

=item to_xml()

Serializes characters to nexml format.

 Type    : Format convertor
 Title   : to_xml
 Usage   : my $xml = $characters->to_xml;
 Function: Converts characters object into a nexml element structure.
 Returns : Nexml block (SCALAR).
 Args    : NONE

=cut

sub to_xml {
    my $self = shift;
    if ( my $to = $self->get_type_object ) {
        if ( $to->get_type !~ m/continuous/i ) {
            $self->set_attributes( 'states' => $to->get_xml_id );
        }
    }
    return $self->SUPER::to_xml;
}

sub _validate  { 1 }
sub _container { _CHARACTERS_ }
sub _type      { _CHARACTER_ }
sub _tag       { 'char' }

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Matrices::TypeSafeData>

This object inherits from L<Bio::Phylo::Matrices::TypeSafeData>, so the
methods defined therein are also applicable to characters objects
objects.

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

1;