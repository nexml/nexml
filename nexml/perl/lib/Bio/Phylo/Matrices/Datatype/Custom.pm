# $Id$
package Bio::Phylo::Matrices::Datatype::Custom;
use strict;
use vars qw(@ISA);
@ISA = qw(Bio::Phylo::Matrices::Datatype);

=head1 NAME

Bio::Phylo::Matrices::Datatype::Custom - Validator subclass,
no serviceable parts inside

=head1 DESCRIPTION

The Bio::Phylo::Matrices::Datatype::* classes are used to validated data
contained by L<Bio::Phylo::Matrices::Matrix> and L<Bio::Phylo::Matrices::Datum>
objects.

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

sub _new { 
        my $class = shift;
        my $self  = shift;
        my %args  = @_;
        die if not $args{'-lookup'};
        bless $self, $class; 
        $self->set_lookup( $args{'-lookup'} );
        return $self;
}

1;