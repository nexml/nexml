# $Id$
package xs::anyType;
use strict;
use warnings;
use xs::anyThing;
our @ISA=qw(xs::anyThing);

sub abstract {
    my $self = shift;
    if ( @_ ) {
        $self->{'abstract'} = shift;
    }
    return $self->{'abstract'};
}

sub base {
    my $self = shift;
    if ( @_ ) {
        $self->{'base'} = shift;
    }
    return $self->{'base'};
}

sub inheritance {
    my $self = shift;
    if ( @_ ) {
        $self->{'inheritance'} = shift;
    }
    return $self->{'inheritance'};
}

sub documentation {
    my $self = shift;
    if ( @_ ) {
        $self->{'documentation'} = shift;
    }
    return $self->{'documentation'};
}

sub type { ref shift }

1;