# $Id: simpleType.pm 183 2007-12-07 08:16:31Z rvos $
package xs::simpleType;
use strict;
use warnings;
use xs::anyType;
our @ISA=qw(xs::anyType);

sub explain { 'A simpleType is an atomic type such as a number or a string.' }

sub facets {
    my $self = shift;
    if ( @_ and 1 < @_ ) {
        my %facets = @_;
        $self->{'facets'} = \%facets;
    }
    elsif ( 1 == @_ ) {
        return $self->{'facets'} ? $self->{'facets'}->{shift} : undef;
    }
    return $self->{'facets'} ? $self->{'facets'} : {};
}

1;