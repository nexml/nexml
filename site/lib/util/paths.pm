package util::paths;
use File::Spec;
use Data::Dumper;

=back

=head1 NAME

util::paths - website related path transformations

=head1 METHODS

=over

=item new()

=cut

sub new {
    my $class = shift;
    my %args  = @_;
    my $self  = {
        'prefix'  => $args{'-prefix'},    # e.g. /Users/rvosa/Documents
        'rewrite' => $args{'-rewrite'} || [],
        'include' => $args{'-include'},
    };
    return bless $self, $class;
}

=item include()

=cut

sub include {
    my ( $self, $file ) = @_;
    return File::Spec->canonpath( $self->{'include'} . '/' . $file );
}

=item transform()

=cut

sub transform {
    my ( $self, $file ) = @_;
    for my $rw ( @{ $self->{'rewrite'} } ) {
        $file = $rw->( $file );
    }
    return $file;
}

=item strip()

=cut

sub strip {
    my ( $self, $file ) = @_;

    # this ugly workaround is because
    # for some reason the root folder 
    # is sometimes called /home and 
    # sometimes /home2 on nexml-dev.nescent.org
    my $prefix = $self->{'prefix'};
    if ( $prefix =~ m|^/home2/| ) {
        $file =~ s|^/home/|/home2/|;
    }
    elsif ( $file =~ m|^/home2/| ) {
        $prefix =~ s|^/home/|/home2/|;
    }

    $file =~ s/^\Q$prefix\E//;
    return $file;
}

=item breadCrumbs()

=cut

sub breadCrumbs {
    my ( $self, $url ) = @_;
    my $root;
    if ( $url =~ m|^(http://[^/]+/)| ) {
        $root = $1;
    }
    $url =~ s|^\Q$root\E||;
    my @fragments = split(/\/+/, $url);
    my @crumbs = ( { 'name' => '~', 'url' => '/' } );
    for my $i ( 0 .. $#fragments ) {
        push @crumbs, {
            'name' => $fragments[$i],
            'url'  => '/' . join( '/', @fragments[ 0 .. $i ] ),
        };
    }
    delete $crumbs[-1]->{'url'};
    return @crumbs;
}

1;
