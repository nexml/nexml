# $Id: sequence.pm 183 2007-12-07 08:16:31Z rvos $
package xs::sequence;
use strict;
use warnings;
use xs::recursiveElementPattern;
our @ISA=qw(xs::recursiveElementPattern);
sub explain { 'A sequence means that out of the items in the list multiple may appear.' }
1;