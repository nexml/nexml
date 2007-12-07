# $Id$
package xs::sequence;
use strict;
use warnings;
use xs::recursiveElementPattern;
our @ISA=qw(xs::recursiveElementPattern);
sub explain { 'A sequence means that out of the items in the list multiple may appear.' }
1;