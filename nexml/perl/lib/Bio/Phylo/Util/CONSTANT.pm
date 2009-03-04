# $Id$
package Bio::Phylo::Util::CONSTANT;
use strict;
use Scalar::Util ();
use Bio::Phylo::Util::Exceptions 'throw';
BEGIN {
    require Exporter;
    use vars qw(@ISA @EXPORT_OK %EXPORT_TAGS);

    # classic subroutine exporting
    @ISA       = qw(Exporter);
    @EXPORT_OK = qw(
    	_NONE_ 
    	_NODE_ 
    	_TREE_ 
    	_FOREST_ 
    	_TAXON_
      	_TAXA_ 
      	_CHAR_ 
      	_DATUM_ 
      	_MATRIX_ 
      	_MATRICES_ 
      	_SEQUENCE_ 
      	_ALIGNMENT_
      	_CHARSTATE_ 
      	_CHARSTATESEQ_ 
      	_MATRIXROW_
      	_PROJECT_
      	_ANNOTATION_
      	_DICTIONARY_
      	looks_like_number
      	looks_like_object
		looks_like_hash
		looks_like_class      	
    );
    %EXPORT_TAGS = ( 
        'all'         => [ @EXPORT_OK ],
        'objecttypes' => [
            qw(
                _NONE_ 
                _NODE_ 
                _TREE_ 
                _FOREST_ 
                _TAXON_
                _TAXA_ 
                _CHAR_ 
                _DATUM_ 
                _MATRIX_ 
                _MATRICES_ 
                _SEQUENCE_ 
                _ALIGNMENT_ 
                _CHARSTATE_ 
                _CHARSTATESEQ_ 
                _MATRIXROW_
                _PROJECT_
                _ANNOTATION_
                _DICTIONARY_
            )
        ],
        'funtions' => [
        	qw(
        		looks_like_number
        		looks_like_object
        		looks_like_hash
        		looks_like_class
        	)
        ],    
    );
}

# according to perlsub:
# "Functions with a prototype of () are potential candidates for inlining. 
# If the result after optimization and constant folding is either a constant 
# or a lexically-scoped scalar which has no other references, then it will 
# be used in place of function calls made without & or do."
sub _NONE_      () { 1  }
sub _NODE_      () { 2  }
sub _TREE_      () { 3  }
sub _FOREST_    () { 4  }
sub _TAXON_     () { 5  }
sub _TAXA_      () { 6  }
sub _DATUM_     () { 7  }
sub _MATRIX_    () { 8  }
sub _MATRICES_  () { 9  }
sub _SEQUENCE_  () { 10 }
sub _ALIGNMENT_ () { 11 }
sub _CHAR_      () { 12 }
sub _PROJECT_   () { 9  }

sub _CHARSTATE_    () { 13 }
sub _CHARSTATESEQ_ () { 14 }
sub _MATRIXROW_    () { 15 }
sub _ANNOTATION_   () { 16 }
sub _DICTIONARY_   () { 17 }

# this is a drop in replacement for Scalar::Util's function
my $looks_like_number;
{
	eval { Scalar::Util::looks_like_number(0) };
	if ( $@ ) {
		my $LOOKS_LIKE_NUMBER_RE = qr/^([-+]?\d+(\.\d+)?([eE][-+]\d+)?|Inf|NaN)$/;
		$looks_like_number = sub {
			my $num = shift;
			if ( defined $num and $num =~ $LOOKS_LIKE_NUMBER_RE ) {
				return 1;
			}
			else {
				return;
			}
		}
	}
	else {
		$looks_like_number = \&Scalar::Util::looks_like_number;
	}
	undef($@);
}

sub looks_like_number($) { return $looks_like_number->(shift) }

sub looks_like_object($$) {
	my ( $object, $constant ) = @_;
	my $type;
	eval { $type = $object->_type };
	if ( $@ or $type != $constant ) {
		throw 'ObjectMismatch'  => 'Invalid object!';
	}
	else {
		return 1;
	}
}

sub looks_like_hash(@) {
	my @array = @_;
	my %hash;
	eval { %hash = @array };
	if ( $@ ) {
		throw 'OddHash' => $@;
	}
	else {
		return @array;
	}
}

sub looks_like_class($) {
	my $class = shift;
	my $path  =  $class;
	$path     =~ s|::|/|g;
	$path    .=  '.pm';
	if ( not exists $INC{$path} ) {
		eval { require $path };
		if ( $@ ) {
			throw 'ExtensionError' => $@;
		}
	}
	return $class;
}

1;
__END__

=head1 NAME

Bio::Phylo::Util::CONSTANT - Global constants and utility functions

=head1 DESCRIPTION

This package defines globals used in the Bio::Phylo libraries. The constants
are called internally by the other packages, they have no direct usage. In
addition, several useful subroutines are optionally exported, which are
described below.

=head1 SUBROUTINES

The following subroutines are utility functions that can be imported using:

 use Bio::Phylo::Util::CONSTANT ':functions';

The subroutines use prototypes for more concise syntax, e.g.:

 looks_like_number $num;
 looks_like_object $obj, $const;
 looks_like_hash @_;
 looks_like_class $class;

These subroutines are used for argument processing inside method calls.

=over

=item looks_like_number()

Tests if argument looks like a number.

 Type    : Utility function
 Title   : looks_like_number
 Usage   : do 'something' if looks_like_number $var;
 Function: Tests whether $var looks like a number.
 Returns : TRUE or undef
 Args    : $var = a variable to test

=item looks_like_object()

Tests if argument looks like an object of specified type constant.

 Type    : Utility function
 Title   : looks_like_object
 Usage   : do 'something' if looks_like_object $obj, $const;
 Function: Tests whether $obj looks like an object.
 Returns : TRUE or throws ObjectMismatch
 Args    : $obj   = an object to test
 		   $const = a constant as defined in this package

=item looks_like_hash()

Tests if argument looks like a hash.

 Type    : Utility function
 Title   : looks_like_hash
 Usage   : do 'something' if looks_like_hash @_;
 Function: Tests whether argument looks like a hash.
 Returns : hash (same order as arg) or throws OddHash
 Args    : An array of hopefully even key/value pairs

=item looks_like_class()

Tests if argument looks like a loadable class name.

 Type    : Utility function
 Title   : looks_like_class
 Usage   : do 'something' if looks_like_class $class;
 Function: Tests whether argument looks like a class.
 Returns : $class or throws ExtensionError
 Args    : A hopefully loadable class name

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

