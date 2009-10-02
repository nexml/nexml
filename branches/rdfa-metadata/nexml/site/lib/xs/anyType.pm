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
    my $base = $self->{'base'};
    if ( $base ) {
        $base =~ s/^xs://;
    }
    return $base;
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

sub type {
    my $self = shift;
    my $class = ref $self;
    $class =~ s/.*://;
    return $class;
}

1;