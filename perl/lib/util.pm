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

1;