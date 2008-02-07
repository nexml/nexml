# $Id: choice.pm 183 2007-12-07 08:16:31Z rvos $
package xs::choice;
use strict;
use warnings;
use xs::recursiveElementPattern;
our @ISA=qw(xs::recursiveElementPattern);
sub explain { 'A choice means that out of the items in the list only one may appear.' }
1;