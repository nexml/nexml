package util::siteFactory;
use vars qw'$AUTOLOAD $VARIABLE_SERVER_NAME';
use File::Spec::Functions;
use Template;
use util::paths;
use util::encoder;

sub new {
	my %defaults = (
		'hostname' => $VARIABLE_SERVER_NAME ? $ENV{'SERVER_NAME'} : 'www.nexml.org',
		'prefix'   => $ENV{'DOCUMENT_ROOT'},
		'subtree'  => $ENV{'REQUEST_URI'},
		'include'  => catdir( $ENV{'DOCUMENT_ROOT'}, 'nexml', 'html', 'include' ),
	);
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
	    'POST_CHOMP'   => 1,                   # cleanup whitespace
	    'START_TAG'    => '<%',
	    'END_TAG'      => '%>',
	    'OUTPUT_PATH'  => $self->prefix,
    );	
    my %template_args = ( %template_defaults, @_ );
    my $template = Template->new( %template_args );
    if ( not $template ) {
    	die "Couldn't instantiate Template";
    }
    else {
    	return $template;
    }
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
	    'currentURL'  => 'http://' . $self->hostname . ( $self->subtree ? $self->subtree : '/' ),
	    'currentDate' => my $time = localtime,
	    'paths'       => $self->create_path_handler,
	    'hostName'    => $self->hostname,
	    'encoder'     => util::encoder->new,
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