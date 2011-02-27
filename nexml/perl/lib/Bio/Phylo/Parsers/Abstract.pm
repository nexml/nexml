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
	open my $handle, '<', $file_name or throw 'FileError' => $OS_ERROR;
	return $handle;
}

# argument is a string, which, at perl version >5.8, 
# we can treat as a handle by opening it by reference
sub _open_string {
	my $string_value = shift;
	open my $handle, '<', \$string_value or throw 'FileError' => $OS_ERROR;
	return $handle;
}

# argument is a url, 
sub _open_url {
	my $url = shift;
	my $handle;
	
	# we need to use LWP::UserAgent to fetch the resource, but
	# we don't "use" it at the top of the module because that
	# would make it a required dependency
	eval { require LWP::UserAgent };
	if ( $EVAL_ERROR ) {
		throw 'ExtensionError' => 
			"Providing a -url argument requires\nsuccesful loading of LWP::UserAgent.\n" .
			"However, there was an error when I\ntried that:\n" .
			$EVAL_ERROR
	}
	
	# apparently it's installed, so let's instantiate a client
	my $ua = LWP::UserAgent->new;
	$ua->timeout(10);
	$ua->env_proxy;
	
	# fetch the resource, get an HTTP::Response object
	my $response = $ua->get($url);
	
	# i.e. 200
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

# deal with all possible data sources, return
# a handle to whatever it is or throw an exception
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

# open a Bio::Phylo::Project if asked (if the -as_project flag
# was provided.) If the user has supplied one (the -project flag)
# simply return that or undefined otherwise.
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

# this constructor is called by the Bio::Phylo::IO::parse
# subroutine
sub _new {
	my $class = shift;
	my %args = looks_like_hash @_;
	
	# factory is either user supplied or a private static
	my $fac = $args{'-factory'} || $factory;
	
	# values of these object fields will be accessed
	# by child classes through the appropriate protected
	# getters
	return bless {
		'_fac'    => $fac,
		'_handle' => _open_handle( %args ),
		'_proj'   => _open_project( $fac, %args ),
		'_args'   => \%args, # for child-specific arguments
	}, $class;
}

# child classes can override this to specify
# that their return value is a single scalar
# (e.g. a tree block, as is the case for newick),
# instead of an array of blocks
sub _return_is_scalar { 0 }

# this is called by Bio::Phylo::IO::parse, and
# in turn it calls the _parse method of whatever
# the concrete child instance is.
sub _process {
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

# once this is called, the handle will have read to
# the end of the stream, so it needs to be rewound
# if we want to read from the top
sub _string {
	my $self = shift;
	my $handle = $self->_handle;
	my $string = do { local $/; <$handle> };
	return $string;
}

sub _logger { $logger }

sub _project { shift->{'_proj'} }

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

=head1 CITATION

If you use Bio::Phylo in published research, please cite it:

B<Rutger A Vos>, B<Jason Caravas>, B<Klaas Hartmann>, B<Mark A Jensen>
and B<Chase Miller>, 2011. Bio::Phylo - phyloinformatic analysis using Perl.
I<BMC Bioinformatics> B<12>:63.
L<http://dx.doi.org/10.1186/1471-2105-12-63>

=head1 REVISION

 $Id$

=cut

1;