package Bio::Phylo::Unparsers::Abstract;
use strict;
use Bio::Phylo::IO ();
use Bio::Phylo::Util::Logger;
use vars qw(@ISA);
@ISA=qw(Bio::Phylo::IO);

my $logger = Bio::Phylo::Util::Logger->new;

=head1 NAME

Bio::Phylo::Unparsers::Abstract - Superclass for unparsers used by Bio::Phylo::IO

=head1 DESCRIPTION

This package is subclassed by all other packages within Bio::Phylo::Unparsers::.*.
There is no direct usage.

=cut

sub _logger { $logger }

sub _new {
	my $class = shift;
	my $self  = {};
	if (@_) {
		my %opts = @_;
		for my $key ( keys %opts ) {
			my $localkey = uc $key;
			$localkey =~ s/-//;
			unless ( ref $opts{$key} ) {
				$self->{$localkey} = uc $opts{$key};
			}
			else {
				$self->{$localkey} = $opts{$key};
			}
		}
	}
	bless $self, $class;
	return $self;
}

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

 $Id: Abstract.pm 1530 2010-11-26 00:48:39Z rvos $

=cut

1;