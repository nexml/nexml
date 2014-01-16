# $Id$
package util;
use strict;
use warnings;

BEGIN {
    require Exporter;
    use vars qw(@ISA @EXPORT_OK %EXPORT_TAGS);
    @ISA       = qw(Exporter);
    @EXPORT_OK = qw(include htmlify);
    %EXPORT_TAGS = ( 'all' => [ @EXPORT_OK ] );
}

=head1 SUBROUTINES

=over

=item include()

 Title:    include()
 Type:     Function
 Usage:    my @contents = include(@files);
 Returns:  Includes raw contents of @files
 Comments: 

=cut

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

=item htmlify()

 Title:    htmlify()
 Type:     Function
 Usage:    my @contents = htmlify(@files);
 Returns:  Includes html'ified contents of @files
 Comments: 

=cut

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
