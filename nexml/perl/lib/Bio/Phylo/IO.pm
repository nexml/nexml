# $Id$
package Bio::Phylo::IO;
use Bio::Phylo ();
use Bio::Phylo::Util::CONSTANT qw(looks_like_class looks_like_hash);
use Bio::Phylo::Util::Exceptions 'throw';
use IO::File;
use strict;

BEGIN {
	use Exporter ();
	use vars qw(@ISA @EXPORT_OK);

	# classic subroutine exporting
	@ISA       = qw(Exporter);
	@EXPORT_OK = qw(&parse &unparse);
}

=head1 NAME

Bio::Phylo::IO - Front end for parsers and serializers

=head1 SYNOPSIS

 use Bio::Phylo::IO qw(parse unparse);

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
 
 # returns a Bio::Phylo::Project object
 my $project = parse(
 	'-file'       => $file,
 	'-format'     => 'nexus',
 	'-as_project' => 1
 )
 my ($taxa) = @{ $project->get_taxa };

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

 print unparse(

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

The IO module is the front end for parsing and serializing phylogenetic
data objects. It is a non-OO module that optionally exports the 'parse' and
'unparse' subroutines into the caller's namespace, using the
C<< use Bio::Phylo::IO qw(parse unparse); >> directive. Alternatively, you can
call the subroutines as class methods. The C<< parse >> and
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
	# first argument could be the package name or an object reference
	# if called as Bio::Phylo::IO->parse or as $io->parse, respectively
	shift if $_[0] and $_[0] eq __PACKAGE__ or ref $_[0] eq __PACKAGE__;
	
	# arguments were provided on the command line, in @ARGV
	if ( @ARGV ) {
		my $i = 0;
		while ( $i < @ARGV ) {
			my ( $key, $value ) = ( $ARGV[$i], $ARGV[ $i + 1 ] );
			
			# shell words have no -dash prefix, so we
			# add it here
			$key = "-$key" if $key !~ /^-/;
			
			# we put @ARGV key/value pairs at the
			# front of the @_ array
			unshift @_, $key, $value;
			$i += 2;
		}
	}
	
	# turn merged @ARGV and @_ arguments into a hash
	my %opts = looks_like_hash @_;
	
	# there must be at least one of these args as a data source
	my @sources = qw(-file -string -handle -url);
	my ($source) = grep { defined $_ } @opts{@sources};
	
	# check provided arguments
	throw 'OddHash' => 'Odd number of elements in hash assignment' if ! @_;
	throw 'BadArgs' => 'No format specified' unless $opts{'-format'};
	throw 'BadArgs' => 'No parseable data source specified' unless $source;

	# instantiate parser subclass and process data
	my $lib = 'Bio::Phylo::Parsers::' . ucfirst $opts{'-format'};
	return looks_like_class( $lib )->_new(@_)->_process;
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

=item L<Bio::Phylo::Parsers::Nexml>

=item L<Bio::Phylo::Parsers::Nexus>

=item L<Bio::Phylo::Parsers::Phylip>

=item L<Bio::Phylo::Parsers::Phyloxml>

=item L<Bio::Phylo::Parsers::Table>

=item L<Bio::Phylo::Parsers::Taxlist>

=item L<Bio::Phylo::Parsers::Tolweb>

=item L<Bio::Phylo::Unparsers::Mrp>

=item L<Bio::Phylo::Unparsers::Newick>

=item L<Bio::Phylo::Unparsers::Nexml>

=item L<Bio::Phylo::Unparsers::Nexus>

=item L<Bio::Phylo::Unparsers::Pagel>

=item L<Bio::Phylo::Unparsers::Phylip>

=item L<Bio::Phylo::Unparsers::Phyloxml>

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=back

=head1 REVISION

 $Id$

=cut

1;
