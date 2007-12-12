# $Id$
package util;
use strict;
use warnings;

BEGIN {
    require Exporter;
    use vars qw(@ISA @EXPORT_OK %EXPORT_TAGS);
    @ISA       = qw(Exporter);
    @EXPORT_OK = qw(svninfo include htmlify);
    %EXPORT_TAGS = ( 'all' => [ @EXPORT_OK ] );
}

sub svninfo {
    my $file = shift;
    my %info;
    my $svn = $ENV{'SVN'} || 'svn';
    my $info = `${svn} info ${file}`;
    for my $line ( split /\n/, $info ) {
        if ( $line =~ m/^(.*?): (.*)$/ ) {
            my ( $key, $value ) = ( $1, $2 );
            $key =~ s/\s*//g;
            $info{$key} = $value;
        }
    }
    return %info;
}

sub include {
    my @contents;
    for my $file ( @_ ) {
        my $lines = '';
        eval {
            open my $fh, '<', $file;
            while(<$fh>) {
                $lines .= $_;
            }
            close $fh;
            push @contents, $lines;
        };
        undef($@);
    }
    return @contents;
}

sub htmlify {
    my @contents;
    for my $file ( @_ ) {
        my $lines = '';
        eval {
            open my $fh, '<', $file;
            while(<$fh>) {
                $_ = '<br/><br/>' if /^\s*$/;
                s|(https?://\S+)|<a href="$1">$1</a>|g;
                s|^\s*\*(.*)$|<li>$1</li>|;
                $lines .= $_;
            }
            close $fh;
            push @contents, $lines;
        };
        undef($@);
    }
    return @contents;
}

package util::paths;
use File::Spec;

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

sub include {
    my ( $self, $file ) = @_;
    return File::Spec->canonpath( $self->{'include'} . '/' . $file );
}

sub transform {
    my ( $self, $file ) = @_;
    for my $rw ( @{ $self->{'rewrite'} } ) {
        $file = $rw->( $file );
    }
    return $file;
}

sub strip {
    my ( $self, $file ) = @_;
    $file =~ s/^\Q$self->{'prefix'}\E//;
    return $file;
}

sub breadCrumbs {
    my ( $self, $url ) = @_;
    my $root;
    if ( $url =~ m|^(http://[^/]+/)| ) {
        $root = $1;
    }
    $url =~ s|^\Q$root\E||;
    my @fragments = split(/\//, $url);
    my @crumbs = ( { 'name' => '~', 'url' => $root } );
    for my $i ( 0 .. $#fragments ) {
        push @crumbs, {
            'name' => $fragments[$i],
            'url'  => $root . join( '/', @fragments[ 0 .. $i ] ),
        };
    }
    delete $crumbs[-1]->{'url'};
    return @crumbs;
}

1;