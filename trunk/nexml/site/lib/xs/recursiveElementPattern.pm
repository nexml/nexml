# $Id: recursiveElementPattern.pm 183 2007-12-07 08:16:31Z rvos $
package xs::recursiveElementPattern;
use strict;
use warnings;
use xs::elementPattern;
our @ISA=qw(xs::elementPattern);

sub elementPatterns {
    my $self = shift;
    if ( @_ ) {
        $self->{'elementPatterns'} = \@_;
    }
    return $self->{'elementPatterns'} ? @{ $self->{'elementPatterns'} } : ();
}

sub children { shift->{'elementPatterns'} }

1;