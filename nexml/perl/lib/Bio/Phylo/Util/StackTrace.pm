package Bio::Phylo::Util::StackTrace;
use strict;

sub new {
	my $class = shift;
	my $self = [];
	my $i = 0;
	my $j = 0;
	package DB; # to get @_ stack from previous frames, see perldoc -f caller
	while( my @frame = caller($i) ) {
		my $package = $frame[0];
		if ( not Bio::Phylo::Util::StackTrace::_skip_me( $package ) ) {
			my @args = @DB::args;
			$self->[$j++] = [ @frame, @args ];
		}
		$i++;
	}
	package Bio::Phylo::Util::StackTrace;
	shift @$self; # to remove "throw" frame
	return bless $self, $class;
}

sub _skip_me {
	my $class = shift;
	my $skip = 0;
	if ( UNIVERSAL::isa( $class, 'Bio::Phylo::Util::Exceptions') ) {
		$skip++;
	}
	if ( UNIVERSAL::isa( $class, 'Bio::Phylo::Util::ExceptionFactory' ) ) {
		$skip++;
	}
	return $skip;
}

=begin comment

fields in frame:
 [
 0   'main',
+1   '/Users/rvosa/Desktop/exceptions.pl',
+2   102,
+3   'Object::this_dies',
 4   1,
 5   undef,
 6   undef,
 7   undef,
 8   2,
 9   'UUUUUUUUUUUU',
+10  bless( {}, 'Object' ),
+11  'very',
+12  'violently'
 ],

=end comment

=cut

sub as_string {
	my $self = shift;
	my $string = "";
	for my $frame ( @$self ) {
		my $method = $frame->[3];
		my @args;
		for my $i ( 10 .. $#{ $frame } ) {
			push @args, $frame->[$i];
		}
		my $file = $frame->[1];
		my $line = $frame->[2];
		$string .= $method . "(" . join(', ', map { "'$_'" } grep { $_ } @args ) . ") called at $file line $line\n";
	}
	return $string;
}

1;