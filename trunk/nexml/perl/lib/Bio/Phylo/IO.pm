# $Id$
package Bio::Phylo::IO;
use Bio::Phylo;
use Bio::Phylo::Util::CONSTANT qw(looks_like_class looks_like_hash);
use Bio::Phylo::Util::Exceptions 'throw';
use IO::File;
use strict;
my @parsers        = qw(Newick Nexus Table Taxlist);
my @unparsers      = qw(Newick Pagel Svg);
my $cached_parsers = {};

BEGIN {
	use Exporter ();
	use vars qw(@ISA @EXPORT_OK);

	# classic subroutine exporting
	@ISA       = qw(Exporter);
	@EXPORT_OK = qw(&parse &unparse);
}

=head1 NAME

Bio::Phylo::IO - Input and output of phylogenetic data.

=head1 SYNOPSIS

B<Import the module, optionally with functions.>

 use Bio::Phylo::IO 'parse';

B<Parsing nexus files.>

 # returns an unblessed array reference of block objects,
 # i.e. taxa, matrix or forest objects
 my $blocks = parse(
    '-file'   => $file,
    '-format' => 'nexus',
 );
 
 for my $block ( @{ $blocks } ) {
    if ( $block->isa('Bio::Phylo::Taxa') ) {
        my $taxa = $block;
        # do something with the taxa
    }
 }

B<Parsing newick strings.>

 # parsing a tree from a newick string
 my $tree_string = '(((A,B),C),D);';
 my $tree = Bio::Phylo::IO->parse(
    '-string' => $tree_string,
    '-format' => 'newick',
 )->first;

 # note: newick parsers return 
 # 'Bio::Phylo::Forest'! Call 
 # ->first to retrieve the first 
 # tree of the forest.

 # prints 'Bio::Phylo::Forest::Tree'
 print ref $tree, "\n";

B<Parsing tab (or otherwise-) delimited tables.>

 # parsing a table
 my $table_string = qq(A,1,2|B,1,2|C,2,2|D,2,1);
 my $matrix = Bio::Phylo::IO->parse(
    '-string'   => $table_string,
    '-format'   => 'table',

    # Data type, see Bio::Phylo::Parsers::Table
    '-type'     => 'STANDARD',

    # field separator  
    '-fieldsep' => ',',

    # line separator
    '-linesep'  => '|'          
 );

 # prints 'Bio::Phylo::Matrices::Matrix'
 print ref $matrix, "\n"; 

B<Parsing lists of taxa.>

 # parsing a list of taxa
 my $taxa_string = 'A:B:C:D';
 my $taxa = Bio::Phylo::IO->parse(
    '-string'   => $taxa_string,
    '-format'   => 'taxlist',
    '-fieldsep' => ':'
 );

 # prints 'Bio::Phylo::Taxa'
 print ref $taxa, "\n";

 # matches taxon names in tree to $taxa object
 $tree->cross_reference($taxa);  

 # likewise for matrix  
 $matrix->cross_reference($taxa);

B<Writing "Pagel" format files.>

 print Bio::Phylo::IO->unparse(

    # pass the tree object, 
    # crossreferenced to taxa, which
    # are crossreferenced to the matrix
    '-phylo' => $tree,                         
    '-format' => 'pagel'
 );

 # prints a pagel data file:
 #4 2
 #A,n1,0.000000,1,2
 #B,n1,0.000000,1,2
 #n1,n2,0.000000
 #C,n2,0.000000,2,2
 #n2,n3,0.000000
 #D,n3,0.000000,2,1

=head1 DESCRIPTION

The IO module is the unified front end for parsing and unparsing phylogenetic
data objects. It is a non-OO module that optionally exports the 'parse' and
'unparse' subroutines into the caller's namespace, using the
C<< use Bio::Phylo::IO qw(parse unparse); >> directive. Alternatively, you can
call the subroutines as class methods, as in the synopsis. The C<< parse >> and
C<< unparse >> subroutines load and dispatch the appropriate sub-modules at
runtime, depending on the '-format' argument.

=head2 CLASS METHODS

=over

=item parse()

Parses a file or string.

 Type    : Class method
 Title   : parse
 Usage   : my $obj = Bio::Phylo::IO->parse(%options);
 Function: Creates (file) handle, 
           instantiates appropriate parser.
 Returns : A Bio::Phylo::* object
 Args    : -file    => (path),
            or
           -string  => (scalar),
           -format  => (description format),
           -(other) => (parser specific options)
 Comments: The parse method makes assumptions about 
           the capabilities of Bio::Phylo::Parsers::* 
           modules: i) their names match those of the
           -format => (blah) arguments, insofar that 
           ucfirst(blah) . '.pm' is an existing module; 
           ii) the modules implement a _from_handle, 
           or a _from_string method. Exceptions are 
           thrown if either assumption is violated. 
           
           If @ARGV contains even key/value pairs such
           as "format newick file <filename>" (note: no
           dashes) these will be prepended to @_, for
           one-liners.          

=cut

sub parse {
	if ( $_[0] and $_[0] eq __PACKAGE__ or ref $_[0] eq __PACKAGE__ ) {
		shift;
	}
	my %opts;
	if ( @ARGV and not scalar @ARGV % 2 ) {
		my $i = 0;
		while ( $i < scalar @ARGV ) {
			my ( $key, $value ) = ( $ARGV[$i], $ARGV[ $i + 1 ] );
			$key = "-$key" if $key !~ /^-/;
			unshift @_, $key, $value;
			$i += 2;
		}
	}
	if ( !@_ || scalar @_ % 2 ) {
		throw 'OddHash' => 'Odd number of elements in hash assignment';
	}
	%opts = looks_like_hash @_;
	if ( !$opts{'-format'} ) {
		throw 'BadArgs' => 'no format specified';
	}
	if ( !grep ucfirst( $opts{'-format'} ), @parsers ) {
		throw 'BadFormat' => 'no parser available for specified format.';
	}
	if ( not ( $opts{'-file'} or $opts{'-string'} or $opts{'-handle'} or $opts{'-url'} ) ) {
		throw 'BadArgs' => 'no parseable data source specified.';
	}
	my $lib = 'Bio::Phylo::Parsers::' . ucfirst($opts{'-format'});
    my $parser;
    if ( exists $cached_parsers->{$lib} ) {
        $parser = $cached_parsers->{$lib};
    }
    else {
        $parser = looks_like_class( $lib )->_new;
        $cached_parsers->{$lib} = $parser;
    }
    if ( ( $opts{-file} or $opts{-handle} ) and $parser->can('_from_handle') ) {
		if ( not $opts{'-handle'} ) {
        	my $fh = IO::File->new;
        	$fh->open( $opts{-file}, 'r' ) or throw 'FileError' => $!;
        	$opts{-handle} = $fh;
		}
        return $parser->_from_handle(%opts);
    }
    elsif ( $opts{'-string'} ) {
        if ( $parser->can('_from_string') ) {
        	return $parser->_from_string(%opts);
        }
        else {
        	throw 'BadArgs' => "$opts{-format} parser can't handle strings";
        }
    }
    elsif ( $opts{'-url'} ) {
        if ( $parser->can('_from_url') ) {
        	return $parser->_from_url(%opts);
        }
        else {
        	throw 'BadArgs' => "$opts{-format} parser can't handle URLs";
        }    	
    }
}

=item unparse()

Unparses object(s) to a string.

 Type    : Class method
 Title   : unparse
 Usage   : my $string = Bio::Phylo::IO->unparse(
               %options
           );
 Function: Turns Bio::Phylo object into a 
           string according to specified format.
 Returns : SCALAR
 Args    : -phylo   => (Bio::Phylo object),
           -format  => (description format),
           -(other) => (parser specific options)

=cut

sub unparse {
    if ( $_[0] and $_[0] eq __PACKAGE__ or ref $_[0] eq __PACKAGE__ ) {
        shift;
    }
    my %opts;
    if ( ! @_ || scalar @_ % 2 ) {
        throw 'OddHash' => 'Odd number of elements in hash assignment';
    }
    %opts = looks_like_hash @_;
    if ( ! $opts{-format} ) {
        throw 'BadFormat' => 'no format specified.';
    }
    if ( ! $opts{-phylo} ) {
        throw 'BadArgs' => 'no object to unparse specified.'
    }
    my $lib = 'Bio::Phylo::Unparsers::' . ucfirst $opts{-format};
    my $unparser = looks_like_class( $lib )->_new(%opts);
    if ( $unparser->can('_to_string') ) {
        return $unparser->_to_string;
    }
    else {
        throw 'ObjectMismatch' => 'the unparser can\'t convert to strings.'
    }
}

# this just to prevent from calling __PACKAGE__->SUPER::DESTROY
sub DESTROY {
    return 1;
}

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Parsers::Newick>

=item L<Bio::Phylo::Parsers::Nexus>

=item L<Bio::Phylo::Parsers::Table>

=item L<Bio::Phylo::Parsers::Taxlist>

=item L<Bio::Phylo::Unparsers::Mrp>

=item L<Bio::Phylo::Unparsers::Newick>

=item L<Bio::Phylo::Unparsers::Nexus>

=item L<Bio::Phylo::Unparsers::Pagel>

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=back

=head1 REVISION

 $Id$

=cut

1;
