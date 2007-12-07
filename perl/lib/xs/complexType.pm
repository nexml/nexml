# $Id$
package xs::complexType;
use strict;
use warnings;
use xs::anyType;
our @ISA=qw(xs::anyType);

sub explain { 'A complexType is an element with any attributes and child elements.' }

sub elementPatterns {
    my $self = shift;
    if ( @_ ) {
        $self->{'elementPatterns'} = \@_;
    }
    return $self->{'elementPatterns'} ? @{ $self->{'elementPatterns'} } : ();
}

sub attributes {
    my $self = shift;
    if ( @_ ) {
        $self->{'attributes'} = \@_;
    }
    return $self->{'attributes'} ? @{ $self->{'attributes'} } : ();
}

1;