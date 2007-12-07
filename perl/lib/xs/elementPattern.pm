# $Id$
package xs::elementPattern;
use strict;
use warnings;

sub new { return bless {}, shift }

sub elementName {
    my $self = shift;
    my $name = ref $self;
    $name =~ s/.*://;
    return $name;
}

sub minOccurs {
    my $self = shift;
    if ( @_ ) {
        $self->{'minOccurs'} = shift;
    }
    return $self->{'minOccurs'};
}

sub maxOccurs {
    my $self = shift;
    if ( @_ ) {
        $self->{'maxOccurs'} = shift;
    }
    return $self->{'maxOccurs'};
}

1;