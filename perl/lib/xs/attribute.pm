# $Id$
package xs::attribute;
use strict;
use warnings;
use xs::anyThing;
our @ISA=qw(xs::anyThing);

sub explain { 'An attribute is a key/value pair such as id="MyID" inside the pointy bits of xml.' }

sub xs::attribute::use {
    my $self = shift;
    if ( @_ ) {
        $self->{'use'} = shift;
    }
    return $self->{'use'};
}

sub type {
    my $self = shift;
    if ( @_ ) {
        $self->{'type'} = shift;
    }
    return $self->{'type'};
}

1;