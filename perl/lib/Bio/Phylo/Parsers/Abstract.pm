package Bio::Phylo::Parsers::Abstract;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw(looks_like_hash);
use Bio::Phylo::Util::Logger;
use Bio::Phylo::Factory;
use Bio::Phylo::IO;
use English;
use vars '@ISA';
@ISA=qw(Bio::Phylo::IO);

=head1 NAME

Bio::Phylo::Parsers::Abstract - Superclass for parsers used by Bio::Phylo::IO

=head1 DESCRIPTION

This package is subclassed by all other packages within Bio::Phylo::Parsers::.*.
There is no direct usage.

=cut

my $factory = Bio::Phylo::Factory->new;
my $logger = Bio::Phylo::Util::Logger->new;

# argument is a file name, which we open
sub _open_file {
	my $file_name = shift;
	open my $fh, '<', $file_name or throw 'FileError' => $OS_ERROR;
	return $fh;
}

# argument is a string, which, at perl version >5.8, 
# we can tried as a handle by opening it as a reference
sub _open_string {
	my $string_value = shift;
	open my $fh, '<', \$string_value or throw 'FileError' => $OS_ERROR;
	return $fh;
}

# argument is a url, 
sub _open_url {
	my $url = shift;
	my $handle;
	
	# we need to use LWP::UserAgent to fetch the resource
	eval { require LWP::UserAgent };
	if ( $EVAL_ERROR ) {
		throw 'ExtensionError' => 
			"Providing a -url argument requires\nsuccesful loading of LWP::UserAgent.\n" .
			"However, there was an error when I\ntried that:\n" .
			$EVAL_ERROR
	}
	my $ua = LWP::UserAgent->new;
	$ua->timeout(10);
	$ua->env_proxy;
	my $response = $ua->get($url);
	if ($response->is_success) {
	
		# content is a string, so we create a handle in the same way
		# as when the argument was a string
		$handle = _open_string( $response->content );
	}
	else {
		throw 'NetworkError' => $response->status_line;
	}
	return $handle;
}

sub _open_handle {
	my %args = @_;
	if ( $args{'-handle'} ) {
		return $args{'-handle'};
	}
	elsif ( $args{'-file'} ) {
		return _open_file( $args{'-file'} );
	}	
	elsif ( $args{'-string'} ) {
		return _open_string( $args{'-string'} );
	}	
	elsif ( $args{'-url'} ) {
		return _open_url( $args{'-url'} );		
	}
	else {
		throw 'BadArgs' => 'No data source provided!';
	}
}

sub _open_project {
	my ( $fac, %args ) = @_;
	if ( $args{'-project'} ) {
		return $args{'-project'};
	}
	elsif ( $args{'-as_project'} ) {
		return $fac->create_project;
	}
	else {
		return undef;
	}
}

sub _new {
	my $class = shift;
	my %args = looks_like_hash @_;
	my $fac  = $args{'-factory'} || $factory;
	return bless {
		'_fac'    => $fac,
		'_handle' => _open_handle( %args ),
		'_proj'   => _open_project( $fac, %args ),
		'_args'   => \%args,
	}, $class;
}

sub _return_is_scalar { 0 }


sub _from_handle {
	my $self = shift;
	if ( $self->_return_is_scalar ) {
		my $result = $self->_parse;
		if ( $self->_project ) {
			return $self->_project->insert( $result );
		}
		else {
			return $result;
		}
	}
	else {
		my @result = $self->_parse;
		if ( $self->_project ) {
			return $self->_project->insert( @result );
		}
		else {
			return [ @result ];
		}
	}
}

sub _from_string {
	return _from_handle(@_);
}

sub _project { shift->{'_proj'} }

sub _string {
	my $self = shift;
	my $handle = $self->_handle;
	my $string = do { local $/; <$handle> };
	return $string;
}

sub _logger { $logger }

sub _handle { shift->{'_handle'} }

sub _factory { shift->{'_fac'} }

sub _args { shift->{'_args'} }

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The parsers are called by the L<Bio::Phylo::IO> object.
Look there for examples.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;