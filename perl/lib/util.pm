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
    my $info = `svn info ${file}`;
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

my %defaults = (
    'suffix' => {
        '.pm' => sub { 
            my $path = shift;
            $path =~ s|(\w+).pm(#\w+)?$|$1/$2|;
            return $path;
        },
        '.xsd' => sub { 
            my $path = shift;
            if ( $path =~ m/#\w+$/ ) {
                $path =~ s|(\w+).xsd(#\w+)?$|$1/$2|;
            }
            else {
                $path =~ s|(\w+).xsd$|$1/|;
            }
            return $path;
        },        
    }

);

sub new {
    my $class = shift;
    my %args  = @_;
    my $self  = {
        'images'   => $args{'-images'},
        'css'      => $args{'-css'},
        'js'       => $args{'-js'},
        'prefix'   => $args{'-prefix'},    # e.g. /Users/rvosa/Documents
        'protocol' => $args{'-protocol'}, # e.g. http://
        'hostname' => $args{'-hostname'}, # e.g. www.nexml.org
        'suffix'   => $defaults{'suffix'},
        
    };
    return bless $self, $class;
}

sub images {
    my ( $self, $file ) = @_;
    return File::Spec->canonpath( $self->{'images'} . '/' . $file );
}

sub home {
    my ( $self, $file ) = @_;
    return $self->{'protocol'} . $self->{'hostname'} . '/' . $file;
}

sub transform {
    my ( $self, $file ) = @_;
    $file =~ s/^\Q$self->{'prefix'}\E//;
    my $suffix;
    if ( $file =~ qr/(\.\w+)(?:#\w+)?$/ ) {
        $suffix = $1;
        if ( UNIVERSAL::isa( $self->{'suffix'}->{$suffix}, 'CODE' ) ) {
            $file = $self->{'suffix'}->{$suffix}->( $file );
        }
    }
    my $path = $self->home( $file );
    $path =~ s|/+|/|g;
    $path =~ s|^(\w+):/|$1://|;
    $path =~ s|^file:/+|file:///|;
    return $path;
}

sub strip {
    my ( $self, $file ) = @_;
    $file =~ s/^\Q$self->{'prefix'}\E//;
    return $file;
}

1;