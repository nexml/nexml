package Bio::Phylo::Util::IDPool;
use strict;
{
    my @reclaim;
    my $obj_counter = 1;

    sub _initialize {
        my $obj_ID = 0;
        if ( @reclaim ) {
            $obj_ID = shift( @reclaim );
        }
        else {
            $obj_ID = $obj_counter;
            $obj_counter++;
        }
        return \$obj_ID;
    }

    sub _reclaim {
        my ( $class, $obj ) = @_;
        push @reclaim, $obj->get_id;
    }
}
1;
__END__

=head1 NAME

Bio::Phylo::Util::IDPool - Utility class for generating object IDs. No serviceable parts inside.

=head1 DESCRIPTION

This package defines utility functions for generating and reclaiming object
IDs. These functions are called by object constructors and destructors,
respectively. There is no direct usage.

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut
