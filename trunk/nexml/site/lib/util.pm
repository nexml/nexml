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

=head1 SUBROUTINES

=over

=item svninfo()

 Title:    svninfo()
 Type:     Function
 Usage:    my %info = svninfo($file);
 Returns:  A hash keyed in the names of `svn info $file` fields
 Comments: Requires either env var $SVN with path to subversion
           executable, or if undefined, svn on $PATH

=cut

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
