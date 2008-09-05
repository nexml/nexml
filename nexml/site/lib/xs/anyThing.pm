# $Id$
package xs::anyThing;
use strict;
use warnings;
use HTML::Entities;
my $anonymous_types_counter = 0;

sub new { return bless {}, shift }

sub file {
    my $self = shift;
    if ( @_ ) {
        $self->{'file'} = shift;
    }
    return $self->{'file'};
}

sub url {
    my $self = shift;
    my $name = $self->name;
    if ( $name =~ m/^xs:(.*)$/ ) {
        return 'http://www.w3.org/TR/xmlschema-2/#' . $1;
    }
    else {
        return $self->file . '#' . $name;
    }
}

sub name {
    my $self = shift;
    if ( @_ ) {
        $self->{'name'} = shift;
    }
    if ( not defined $self->{'name'} ) {
        my $type = ref $self;
        $type =~ s/.*://;
        $self->{'name'} = $type . ++$anonymous_types_counter;
    }
    return $self->{'name'};
}

sub xml {
    my $self = shift;
    if ( @_ ) {
        $self->{'xml'} = shift;
    }
    return $self->{'xml'};
}

sub escapedXml {
    my $escaped = encode_entities( shift->{'xml'} );
    $escaped =~ s|(\S+)=|<span class="attrName">$1</span>=|g;
    $escaped =~ s|(&quot;.*?&quot;)|<span class="attrVal">$1</span>|g;
    $escaped =~ s|(&lt;/?)(\S+)([ /&])|$1<span class="eltName">$2</span>$3|g;
    $escaped =~ s|(&lt;)|<span class="xmlToken">$1</span>|g;
    $escaped =~ s|(&gt;)|<span class="xmlToken">$1</span>|g;
    $escaped =~ s|>/<|><span class="xmlToken">/</span><|g;
    $escaped =~ s|>=<|><span class="xmlToken">=</span><|g;
    return $escaped;
}

1;