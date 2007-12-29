# $Id: util.pm 210 2007-12-12 10:24:05Z rvos $
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

package util::paths;
use File::Spec;

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
    $file =~ s/^\Q$self->{'prefix'}\E//;
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
    my @fragments = split(/\//, $url);
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

package util::encoder;
use URI::Escape ();
use HTML::Entities ();

=back

=head1 NAME

util::encoder - website related string transformations

=head1 METHODS

=over

=item new()

=cut

sub new { return bless {}, shift }

=item uri_escape()

=cut

sub util::encoder::uri_escape { URI::Escape::uri_escape(pop) }

=item uri_unescape()

=cut

sub util::encoder::uri_unescape { URI::Escape::uri_unescape(pop) }

=item encode_entities()

=cut

sub util::encoder::encode_entities { HTML::Entities::encode_entities(pop) }

=item decode_entities()

=cut

sub util::encoder::decode_entities { HTML::Entities::decode_entities(pop) }

=item chomp()

=cut

sub util::encode::chomp { CORE::chomp($_[1]) }

=back

=head1 SEE ALSO

Also see the website: L<http://www.nexml.org>

=head1 REVISION

 $Id: Phylo.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

package util::siteFactory;
use vars '$AUTOLOAD';
use File::Spec::Functions;
use Template;

my %defaults = (
	'hostname' => $ENV{'SERVER_NAME'},
	'prefix'   => $ENV{'DOCUMENT_ROOT'},
	'subtree'  => $ENV{'SCRIPT_URL'},
	'include'  => catdir( $ENV{'DOCUMENT_ROOT'}, 'nexml', 'html', 'include' ),
);

sub new {
	my $class  = shift;
	my %fields = ( %defaults, @_ );
	my $self   = \%fields;
	return bless $self, $class; 
}

sub create_path_handler {
	my $self = shift;
	return util::paths->new(
	    '-prefix'  => $self->prefix,
	    '-include' => $self->include,
	);
}

sub create_plain_template {
	my $self = shift;
	my %template_defaults = (     
		'INCLUDE_PATH' => $self->include,      # or list ref
	    'POST_CHOMP'   => 1,             # cleanup whitespace
	#    'PRE_PROCESS'  => 'header.tmpl', # prefix each template
	#    'POST_PROCESS' => 'footer.tmpl', # suffix each template
	    'START_TAG'    => '<%',
	    'END_TAG'      => '%>',
	    'OUTPUT_PATH'  => $self->prefix,
    );	
    my %template_args = ( %template_defaults, @_ );
    return Template->new( %template_args );
}

sub create_site_template {
	my $self = shift;
	my %defaults = (
		'PRE_PROCESS'  => 'header.tmpl',
		'POST_PROCESS' => 'footer.tmpl',	
	);
	my %args = ( %defaults, @_ );
	return $self->create_plain_template( %args );
}

sub create_template_vars {
	my $self = shift;
	my %default_vars = (
	    'currentURL'  => 'http://' . $self->hostname . $self->subtree,
	    'currentDate' => my $time = localtime,
	    'paths'       => $self->create_path_handler,
	    'hostName'    => $self->hostname,		
	);
	my %args = ( %default_vars, @_ );
	return \%args;
}

sub AUTOLOAD {
	my $self = shift;
	my $method = $AUTOLOAD;
	$method =~ s/.*://;
	if ( $method =~ qr/^[A-Z]+$/ or exists $self->{$method} ) {
		if ( exists $self->{$method} ) {
			if ( @_ ) {
				$self->{$method} = shift;
			}
			return $self->{$method};
		}
	}
	else {
		die "No such method: $AUTOLOAD";
	}
}

1;
