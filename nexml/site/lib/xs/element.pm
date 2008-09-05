# $Id$
package xs::element;
use strict;
use warnings;
use xs::elementPattern;

our @ISA=qw(xs::elementPattern);

sub explain { 'An element is one of those pointy things.' }

sub type {
    my $self = shift;
    if ( @_ ) {
        $self->{'type'} = shift;
    }
    return $self->{'type'};
}

sub name {
    my $self = shift;
    if ( @_ ) {
        $self->{'name'} = shift;
    }
    return $self->{'name'};
}

1;