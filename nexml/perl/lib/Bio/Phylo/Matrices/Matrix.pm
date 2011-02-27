# $Id$
package Bio::Phylo::Matrices::Matrix;
use vars '@ISA';
use strict;
use Bio::Phylo::Factory;
use Bio::Phylo::Taxa::TaxaLinker;
use Bio::Phylo::IO qw(unparse);
use Bio::Phylo::Util::CONSTANT qw(
	:objecttypes 
	looks_like_hash 
	looks_like_instance 
	looks_like_number
);
use Bio::Phylo::Util::Exceptions qw(throw);
use Bio::Phylo::NeXML::Writable ();
use Bio::Phylo::Matrices::TypeSafeData ();
use Bio::Phylo::Matrices::Datum ();
@ISA = qw(
  Bio::Phylo::Matrices::TypeSafeData
  Bio::Phylo::Taxa::TaxaLinker
);

eval { require Bio::Align::AlignI };
if ( not $@ ) {
	push @ISA, 'Bio::Align::AlignI';
}
else {
	undef($@);
}
my $LOADED_WRAPPERS = 0;


{
	my $CONSTANT_TYPE      = _MATRIX_;
	my $CONSTANT_CONTAINER = _MATRICES_;
	my $logger             = __PACKAGE__->get_logger;
	my $factory            = Bio::Phylo::Factory->new;
	my @inside_out_arrays  = \(
		my (
			%type,  
			%charlabels,
			%statelabels,
			%gapmode,
			%matchchar,
			%polymorphism,
			%case_sensitivity,
		)
	);

=head1 NAME

Bio::Phylo::Matrices::Matrix - Character state matrix

=head1 SYNOPSIS

 use Bio::Phylo::Factory;
 my $fac = Bio::Phylo::Factory->new;

 # instantiate taxa object
 my $taxa = $fac->create_taxa;
 for ( 'Homo sapiens', 'Pan paniscus', 'Pan troglodytes' ) {
     $taxa->insert( $fac->create_taxon( '-name' => $_ ) );
 }

 # instantiate matrix object, 'standard' data type. All categorical
 # data types follow semantics like this, though with different
 # symbols in lookup table and matrix
 my $standard_matrix = $fac->create_matrix(
     '-type'   => 'STANDARD',
     '-taxa'   => $taxa,
     '-lookup' => { 
         '-' => [],
         '0' => [ '0' ],
         '1' => [ '1' ],
         '?' => [ '0', '1' ],
     },
     '-labels' => [ 'Opposable big toes', 'Opposable thumbs', 'Not a pygmy' ],
     '-matrix' => [
         [ 'Homo sapiens'    => '0', '1', '1' ],
         [ 'Pan paniscus'    => '1', '1', '0' ],
         [ 'Pan troglodytes' => '1', '1', '1' ],
     ],
 );
 
 # note: complicated constructor for mixed data!
 my $mixed_matrix = Bio::Phylo::Matrices::Matrix->new( 
    
    # if you want to create 'mixed', value for '-type' is array ref...
    '-type' =>  [ 
    
        # ...with first field 'mixed'...                
        'mixed',
        
        # ...second field is an array ref...
        [
            
            # ...with _ordered_ key/value pairs...
            'dna'      => 10, # value is length of type range
            'standard' => 10, # value is length of type range
            
            # ... or, more complicated, value is a hash ref...
            'rna'      => {
                '-length' => 10, # value is length of type range
                
                # ...value for '-args' is an array ref with args 
                # as can be passed to 'unmixed' datatype constructors,
                # for example, here we modify the lookup table for
                # rna to allow both 'U' (default) and 'T'
                '-args'   => [
                    '-lookup' => {
                        'A' => [ 'A'                     ],
                        'C' => [ 'C'                     ],
                        'G' => [ 'G'                     ],
                        'U' => [ 'U'                     ],
                        'T' => [ 'T'                     ],
                        'M' => [ 'A', 'C'                ],
                        'R' => [ 'A', 'G'                ],
                        'S' => [ 'C', 'G'                ],
                        'W' => [ 'A', 'U', 'T'           ],
                        'Y' => [ 'C', 'U', 'T'           ],
                        'K' => [ 'G', 'U', 'T'           ],
                        'V' => [ 'A', 'C', 'G'           ],
                        'H' => [ 'A', 'C', 'U', 'T'      ],
                        'D' => [ 'A', 'G', 'U', 'T'      ],
                        'B' => [ 'C', 'G', 'U', 'T'      ],
                        'X' => [ 'G', 'A', 'U', 'T', 'C' ],
                        'N' => [ 'G', 'A', 'U', 'T', 'C' ],
                    },
                ],
            },
        ],
    ],
 );
 
 # prints 'mixed(Dna:1-10, Standard:11-20, Rna:21-30)'
 print $mixed_matrix->get_type;

=head1 DESCRIPTION

This module defines a container object that holds
L<Bio::Phylo::Matrices::Datum> objects. The matrix
object inherits from L<Bio::Phylo::Listable>, so the
methods defined there apply here.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Matrix constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $matrix = Bio::Phylo::Matrices::Matrix->new;
 Function: Instantiates a Bio::Phylo::Matrices::Matrix
           object.
 Returns : A Bio::Phylo::Matrices::Matrix object.
 Args    : -type   => optional, but if used must be FIRST argument, 
                      defines datatype, one of dna|rna|protein|
                      continuous|standard|restriction|[ mixed => [] ]

           -taxa   => optional, link to taxa object
           -lookup => character state lookup hash ref
           -labels => array ref of character labels
           -matrix => two-dimensional array, first element of every
                      row is label, subsequent are characters

=cut

	sub new {

		# could be child class
		my $class = shift;

		# notify user
		$logger->info("constructor called for '$class'");
		
		if ( not $LOADED_WRAPPERS ) {
			eval do { local $/; <DATA> };
			die $@ if $@;
			$LOADED_WRAPPERS++;
		}		

		# go up inheritance tree, eventually get an ID
		my $self = $class->SUPER::new( @_ );
		return $self;
	}

=item new_from_bioperl()

Matrix constructor from Bio::Align::AlignI argument.

 Type    : Constructor
 Title   : new_from_bioperl
 Usage   : my $matrix = 
           Bio::Phylo::Matrices::Matrix->new_from_bioperl(
               $aln           
           );
 Function: Instantiates a 
           Bio::Phylo::Matrices::Matrix object.
 Returns : A Bio::Phylo::Matrices::Matrix object.
 Args    : An alignment that implements Bio::Align::AlignI

=cut
	
	sub new_from_bioperl {
	    my ( $class, $aln, @args ) = @_;
		if ( looks_like_instance( $aln, 'Bio::Align::AlignI' ) ) {
		    $aln->unmatch;
		    $aln->map_chars('\.','-');
		    my @seqs = $aln->each_seq;
		    my ( $type, $missing, $gap, $matchchar ); 
		    if ( $seqs[0] ) {
		    	$type = $seqs[0]->alphabet || $seqs[0]->_guess_alphabet || 'dna';
		    }
		    else {
		    	$type = 'dna';
		    }
			my $self = $factory->create_matrix( 
				'-type' => $type,
				'-special_symbols' => {
			    	'-missing'   => $aln->missing_char || '?',
			    	'-matchchar' => $aln->match_char   || '.',
			    	'-gap'       => $aln->gap_char     || '-',					
				},
				@args 
			);			
			# XXX create raw getter/setter pairs for annotation, accession, consensus_meta source
			for my $field ( qw(description accession id annotation consensus_meta score source) ) {
				$self->$field( $aln->$field );
			}			
			my $to = $self->get_type_object;			
            for my $seq ( @seqs ) {
            	my $datum = Bio::Phylo::Matrices::Datum->new_from_bioperl(
            		$seq, '-type_object' => $to
            	);                                         	
                $self->insert($datum);
            }
            return $self;
		}
		else {
			throw 'ObjectMismatch' => 'Not a bioperl alignment!';
		}
	}

=back

=head2 MUTATORS

=over

=item set_special_symbols

Sets three special symbols in one call

 Type    : Mutator
 Title   : set_special_symbols
 Usage   : $matrix->set_special_symbols( 
 		       -missing   => '?', 
 		       -gap       => '-', 
 		       -matchchar => '.' 
 		   );
 Function: Assigns state labels.
 Returns : $self
 Args    : Three args (with distinct $x, $y and $z):
  		       -missing   => $x, 
 		       -gap       => $y, 
 		       -matchchar => $z
 Notes   : This method is here to ensure
           you don't accidentally use the
           same symbol for missing AND gap

=cut

	sub set_special_symbols {
		my $self = shift;
		my %args;
		if ( ( @_ == 1 && ref($_[0]) eq 'HASH' && ( %args = %{$_[0]} ) ) || ( %args = looks_like_hash @_ ) ) {
			if ( ! defined $args{'-missing'} || ! defined $args{'-gap'} || ! defined $args{'-matchchar'} ) {
				throw 'BadArgs' => 'Need -missing => $x, -gap => $y, -matchchar => $z arguments, not '."@_";
			}
			my %values = map { $_ => 1 } values %args;
			my @values = keys %values;
			if ( scalar @values < 3 ) {
				throw 'BadArgs' => 'Symbols must be distinct, not ' . join(', ', values %args);
			}
			my %old_special_symbols = ( 
				$self->get_missing   => 'set_missing',
				$self->get_gap       => 'set_gap',
				$self->get_matchchar => 'set_matchchar',
			);	
			my %new_special_symbols = (
				$args{'-missing'}    => 'set_missing',
				$args{'-gap'}        => 'set_gap',
				$args{'-matchchar'}  => 'set_matchchar',
			);
			my %dummies;
			while ( %new_special_symbols ) {
				for my $sym ( keys %new_special_symbols ) {
					if ( not $old_special_symbols{$sym} ) {
						my $method = $new_special_symbols{$sym};
						$self->$method($sym);
						delete $new_special_symbols{$sym};
					}
					elsif ( $old_special_symbols{$sym} eq $new_special_symbols{$sym} ) {
						delete $new_special_symbols{$sym};
					}
					else {
						DUMMY: for my $dummy ( qw(! @ $ % ^ & *) ) {
							if ( ! $new_special_symbols{$dummy} && ! $old_special_symbols{$dummy} && ! $dummies{$dummy} ) {
								my $method = $old_special_symbols{$sym};
								$self->$method($dummy);
								$dummies{$dummy} = 1;
								delete $old_special_symbols{$sym};
								$old_special_symbols{$dummy} = $method;
								last DUMMY;
							}
						}
					}
				}
			}
		}
		return $self;
	}

=item set_statelabels()

Sets argument state labels.

 Type    : Mutator
 Title   : set_statelabels
 Usage   : $matrix->set_statelabels( [ [ 'state1', 'state2' ] ] );
 Function: Assigns state labels.
 Returns : $self
 Args    : ARRAY, or nothing (to reset);
           The array is two-dimensional, 
           the first index is to indicate
           the column the labels apply to,
           the second dimension the states
           (sorted numerically or alphabetically,
           depending on what's appropriate)

=cut

	sub set_statelabels {
		my ( $self, $statelabels ) = @_;
		
		# it's an array ref, but what about its contents?
		if ( looks_like_instance( $statelabels, 'ARRAY' ) ) {
			for my $col ( @{$statelabels} ) {
				if ( not looks_like_instance( $col, 'ARRAY') ) {
					throw 'BadArgs' => "statelabels must be a two dimensional array ref";
				}
			}
		}

		# it's defined but not an array ref
		elsif ( defined $statelabels && ! looks_like_instance( $statelabels, 'ARRAY' ) ) {
			throw 'BadArgs' => "statelabels must be a two dimensional array ref";
		}

		# it's either a valid array ref, or nothing, i.e. a reset
		$statelabels{$self->get_id} = $statelabels || [];
		return $self;		
	}

=item set_charlabels()

Sets argument character labels.

 Type    : Mutator
 Title   : set_charlabels
 Usage   : $matrix->set_charlabels( [ 'char1', 'char2', 'char3' ] );
 Function: Assigns character labels.
 Returns : $self
 Args    : ARRAY, or nothing (to reset);

=cut

	sub set_charlabels {
		my ( $self, $charlabels ) = @_;

		# it's an array ref, but what about its contents?
		if ( looks_like_instance( $charlabels, 'ARRAY' ) ) {
			for my $label ( @{$charlabels} ) {
				if ( ref $label ) {
					throw 'BadArgs' => "charlabels must be an array ref of scalars";
				}
			}
		}

		# it's defined but not an array ref
		elsif ( defined $charlabels && ! looks_like_instance( $charlabels, 'ARRAY' ) ) {
			throw 'BadArgs' => "charlabels must be an array ref of scalars";
		}

		# it's either a valid array ref, or nothing, i.e. a reset
		$charlabels{$self->get_id} = defined $charlabels ? $charlabels : [];
		return $self;
	}

=item set_gapmode()

Defines matrix gapmode.

 Type    : Mutator
 Title   : set_gapmode
 Usage   : $matrix->set_gapmode( 1 );
 Function: Defines matrix gapmode ( false = missing, true = fifth state )
 Returns : $self
 Args    : boolean

=cut

	sub set_gapmode {
		my ( $self, $gapmode ) = @_;
		$gapmode{$self->get_id} = !!$gapmode;
		return $self;
	}

=item set_matchchar()

Assigns match symbol.

 Type    : Mutator
 Title   : set_matchchar
 Usage   : $matrix->set_matchchar( $match );
 Function: Assigns match symbol (default is '.').
 Returns : $self
 Args    : ARRAY

=cut

	sub set_matchchar {
		my ( $self, $match ) = @_;
		my $missing = $self->get_missing;
		my $gap     = $self->get_gap;
		if ( $match eq $missing ) {
			throw 'BadArgs' => "Match character '$match' already in use as missing character";
		}
		elsif ( $match eq $gap ) {
			throw 'BadArgs' => "Match character '$match' already in use as gap character";
		}
		else {
			$matchchar{$self->get_id} = $match;
		}
		return $self;
	}

=item set_polymorphism()

Defines matrix 'polymorphism' interpretation.

 Type    : Mutator
 Title   : set_polymorphism
 Usage   : $matrix->set_polymorphism( 1 );
 Function: Defines matrix 'polymorphism' interpretation
           ( false = uncertainty, true = polymorphism )
 Returns : $self
 Args    : boolean

=cut

	sub set_polymorphism {
		my ( $self, $poly ) = @_;
		$polymorphism{$self->get_id} = !!$poly;
		return $self;
	}

=item set_raw()

Set contents using two-dimensional array argument.

 Type    : Mutator
 Title   : set_raw
 Usage   : $matrix->set_raw( [ [ 'taxon1' => 'acgt' ], [ 'taxon2' => 'acgt' ] ] );
 Function: Syntax sugar to define $matrix data contents.
 Returns : $self
 Args    : A two-dimensional array; first dimension contains matrix rows,
           second dimension contains taxon name / character string pair.

=cut

	sub set_raw {
		my ( $self, $raw ) = @_;
		if ( defined $raw ) {
			if ( looks_like_instance( $raw, 'ARRAY' ) ) {
				my @rows;
				for my $row ( @{$raw} ) {
					if ( defined $row ) {
						if ( looks_like_instance( $row, 'ARRAY' ) ) {
							my $matrixrow = $factory->create_datum(
								'-type_object' => $self->get_type_object,
								'-name'        => $row->[0],
								'-char' => join( ' ', @$row[ 1 .. $#{$row} ] ),
							);
							push @rows, $matrixrow;
						}
						else {
							throw 'BadArgs' => "Raw matrix row must be an array reference";
						}
					}
				}
				$self->clear;
				$self->insert($_) for @rows;
			}
			else {
				throw 'BadArgs' => "Raw matrix must be an array reference";
			}
		}
		return $self;
	}

=item set_respectcase()

Defines matrix case sensitivity interpretation.

 Type    : Mutator
 Title   : set_respectcase
 Usage   : $matrix->set_respectcase( 1 );
 Function: Defines matrix case sensitivity interpretation
           ( false = disregarded, true = "respectcase" )
 Returns : $self
 Args    : boolean

=cut

	sub set_respectcase {
		my ( $self, $case_sensitivity ) = @_;
		$case_sensitivity{$self->get_id} = !!$case_sensitivity;
		return $self;
	}

=back

=head2 ACCESSORS

=over

=item get_special_symbols()

Retrieves hash ref for missing, gap and matchchar symbols

 Type    : Accessor
 Title   : get_special_symbols
 Usage   : my %syms = %{ $matrix->get_special_symbols };
 Function: Retrieves special symbols
 Returns : HASH ref, e.g. { -missing => '?', -gap => '-', -matchchar => '.' }
 Args    : None.

=cut

	sub get_special_symbols {
		my $self = shift;
		return {
			'-missing'   => $self->get_missing,
			'-matchchar' => $self->get_matchchar,
			'-gap'       => $self->get_gap
		};
	}

=item get_statelabels()

Retrieves state labels.

 Type    : Accessor
 Title   : get_statelabels
 Usage   : my @statelabels = @{ $matrix->get_statelabels };
 Function: Retrieves state labels.
 Returns : ARRAY
 Args    : None.

=cut

	sub get_statelabels { $statelabels{ $_[0]->get_id } || [] }

=item get_charlabels()

Retrieves character labels.

 Type    : Accessor
 Title   : get_charlabels
 Usage   : my @charlabels = @{ $matrix->get_charlabels };
 Function: Retrieves character labels.
 Returns : ARRAY
 Args    : None.

=cut

	sub get_charlabels { $charlabels{ $_[0]->get_id } || [] }

=item get_gapmode()

Returns matrix gapmode.

 Type    : Accessor
 Title   : get_gapmode
 Usage   : do_something() if $matrix->get_gapmode;
 Function: Returns matrix gapmode ( false = missing, true = fifth state )
 Returns : boolean
 Args    : none

=cut

	sub get_gapmode { $gapmode{ $_[0]->get_id } }

=item get_matchchar()

Returns matrix match character.

 Type    : Accessor
 Title   : get_matchchar
 Usage   : my $char = $matrix->get_matchchar;
 Function: Returns matrix match character (default is '.')
 Returns : SCALAR
 Args    : none

=cut

	sub get_matchchar { $matchchar{ $_[0]->get_id } || '.' }

=item get_nchar()

Calculates number of characters.

 Type    : Accessor
 Title   : get_nchar
 Usage   : my $nchar = $matrix->get_nchar;
 Function: Calculates number of characters (columns) in matrix (if the matrix
           is non-rectangular, returns the length of the longest row).
 Returns : INT
 Args    : none

=cut

	sub get_nchar {
		my $self  = shift;
		my $nchar = 0;
		for my $row ( @{ $self->get_entities } ) {
			my $offset = $row->get_position - 1;
			my $rowlength = scalar @{ $row->get_entities };
			$rowlength += $offset;
			$nchar = $rowlength if $rowlength > $nchar;
		}
		return $nchar;
	}

=item get_ntax()

Calculates number of taxa (rows) in matrix.

 Type    : Accessor
 Title   : get_ntax
 Usage   : my $ntax = $matrix->get_ntax;
 Function: Calculates number of taxa (rows) in matrix
 Returns : INT
 Args    : none

=cut

	sub get_ntax { scalar @{ shift->get_entities } }

=item get_polymorphism()

Returns matrix 'polymorphism' interpretation.

 Type    : Accessor
 Title   : get_polymorphism
 Usage   : do_something() if $matrix->get_polymorphism;
 Function: Returns matrix 'polymorphism' interpretation
           ( false = uncertainty, true = polymorphism )
 Returns : boolean
 Args    : none

=cut

	sub get_polymorphism { $polymorphism{ $_[0]->get_id } }

=item get_raw()

Retrieves a 'raw' (two-dimensional array) representation of the matrix's contents.

 Type    : Accessor
 Title   : get_raw
 Usage   : my $rawmatrix = $matrix->get_raw;
 Function: Retrieves a 'raw' (two-dimensional array) representation
           of the matrix's contents.
 Returns : A two-dimensional array; first dimension contains matrix rows,
           second dimension contains taxon name and characters.
 Args    : NONE

=cut

	sub get_raw {
		my $self = shift;
		my @raw;
		for my $row ( @{ $self->get_entities } ) {
			my @row;
			push @row, $row->get_name;
			my @char = $row->get_char;
			push @row, @char;
			push @raw, \@row;
		}
		return \@raw;
	}

=item get_respectcase()

Returns matrix case sensitivity interpretation.

 Type    : Accessor
 Title   : get_respectcase
 Usage   : do_something() if $matrix->get_respectcase;
 Function: Returns matrix case sensitivity interpretation
           ( false = disregarded, true = "respectcase" )
 Returns : boolean
 Args    : none

=cut

	sub get_respectcase { $case_sensitivity{ $_[0]->get_id } }

=back

=head2 CALCULATIONS

=over

=item calc_prop_invar()

Calculates proportion of invariant sites.

 Type    : Calculation
 Title   : calc_prop_invar
 Usage   : my $pinvar = $matrix->calc_prop_invar;
 Function: Calculates proportion of invariant sites.
 Returns : Scalar: a number
 Args    : Optional:
           # if true, counts missing (usually the '?' symbol) as a state
	   # in the final tallies. Otherwise, missing states are ignored
           -missing => 1
           # if true, counts gaps (usually the '-' symbol) as a state
	   # in the final tallies. Otherwise, gap states are ignored
	   -gap => 1

=cut

	sub calc_prop_invar {
		my $self  = shift;
		my $raw   = $self->get_raw;
		my $ntax  = $self->get_ntax;
		my $nchar = $self->get_nchar || 1;
		my %args  = looks_like_hash @_;
		my @symbols_to_ignore;
		for my $sym ( qw(missing gap) ) {
			if ( not exists $args{"-${sym}"} ) {
				my $method = "get_${sym}";
				push @symbols_to_ignore, $self->$method;
			}
		}
		my $invar_count;
		for my $i ( 1 .. $nchar ) {
			my %seen;
			for my $j ( 0 .. ($ntax-1) ) {
				$seen{$raw->[$j]->[$i]}++;
			}
			delete @seen{@symbols_to_ignore};
			my @symbols_in_column = keys %seen;
			$invar_count++ if scalar(@symbols_in_column) <= 1;
		}
		return $invar_count / $nchar;
	}

=item calc_state_counts()

Calculates occurrences of states.

 Type    : Calculation
 Title   : calc_state_counts
 Usage   : my %counts = %{ $matrix->calc_state_counts };
 Function: Calculates occurrences of states.
 Returns : Hashref: keys are states, values are counts
 Args    : Optional - one or more states to focus on

=cut

	sub calc_state_counts {
		my $self = shift;
		my %totals;
		for my $row ( @{ $self->get_entities } ) {
			my $counts = $row->calc_state_counts(@_);
			for my $state ( keys %{ $counts } ) {
				$totals{$state} += $counts->{$state};
			}
		}
		return \%totals;
	}


=item calc_state_frequencies()

Calculates the frequencies of the states observed in the matrix.

 Type    : Calculation
 Title   : calc_state_frequencies
 Usage   : my %freq = %{ $object->calc_state_frequencies() };
 Function: Calculates state frequencies
 Returns : A hash, keys are state symbols, values are frequencies
 Args    : Optional:
           # if true, counts missing (usually the '?' symbol) as a state
	   # in the final tallies. Otherwise, missing states are ignored
           -missing => 1
           # if true, counts gaps (usually the '-' symbol) as a state
	   # in the final tallies. Otherwise, gap states are ignored
	   -gap => 1
 Comments: Throws exception if matrix holds continuous values

=cut

	sub calc_state_frequencies {
		my $self = shift;		
		my %result;
		for my $row ( @{ $self->get_entities } ) {
			my $freqs = $row->calc_state_frequencies(@_);
			for my $state ( keys %{ $freqs } ) {
				$result{$state} += $freqs->{$state};
			}
		}
		my $total = 0;
		$total += $_ for values %result;
		if ( $total > 0 ) {
			for my $state ( keys %result ) {
				$result{$state} /= $total;
			}
		}
		return \%result;
	}

=item calc_distinct_site_patterns()

Identifies the distinct distributions of states for all characters and
counts their occurrences. Returns an array-of-arrays, where the first cell
of each inner array holds the occurrence count, the second cell holds the
pattern, i.e. an array of states. For example, for a matrix like this:

 taxon1 GTGTGTGTGTGTGTGTGTGTGTG
 taxon2 AGAGAGAGAGAGAGAGAGAGAGA
 taxon3 TCTCTCTCTCTCTCTCTCTCTCT
 taxon4 TCTCTCTCTCTCTCTCTCTCTCT
 taxon5 AAAAAAAAAAAAAAAAAAAAAAA
 taxon6 CGCGCGCGCGCGCGCGCGCGCGC
 taxon7 AAAAAAAAAAAAAAAAAAAAAAA

The following data structure will be returned:

 [
	[ 12, [ 'G', 'A', 'T', 'T', 'A', 'C', 'A' ] ],
	[ 11, [ 'T', 'G', 'C', 'C', 'A', 'G', 'A' ] ]
 ]

The patterns are sorted from most to least frequently occurring, the states
for each pattern are in the order of the rows in the matrix. (In other words,
the original matrix can more or less be reconstructed by inverting the patterns,
and multiplying them by their occurrence, although the order of the columns
will be lost.)

 Type    : Calculation
 Title   : calc_distinct_site_patterns
 Usage   : my $patterns = $object->calc_distinct_site_patterns;
 Function: Calculates distinct site patterns.
 Returns : A multidimensional array, see above.
 Args    : NONE
 Comments:

=cut
	
	sub calc_distinct_site_patterns {
		my $self  = shift;
		my $raw   = $self->get_raw;
		my $nchar = $self->get_nchar;
		my $ntax  = $self->get_ntax;
		my %pattern;
		for my $i ( 1 .. $nchar ) {
			my @column;
			for my $j ( 0 .. ( $ntax - 1 ) ) {
				push @column, $raw->[$j]->[$i];
			}
			my $col_pattern = join ' ', @column;
			$pattern{$col_pattern}++;
		}
		my @pattern_array;
		for my $key ( keys %pattern ) {
			my @column = split / /, $key;
			push @pattern_array, [ $pattern{$key}, \@column ];
		}
		my @sorted = sort { $b->[0] <=> $a->[1] } @pattern_array;
		\@sorted;
	}

=item calc_gc_content()

Calculates the G+C content as a fraction on the total

 Type    : Calculation
 Title   : calc_gc_content
 Usage   : my $fraction = $obj->calc_gc_content;
 Function: Calculates G+C content
 Returns : A number between 0 and 1 (inclusive)
 Args    : Optional:
           # if true, counts missing (usually the '?' symbol) as a state
	   # in the final tallies. Otherwise, missing states are ignored
           -missing => 1
           # if true, counts gaps (usually the '-' symbol) as a state
	   # in the final tallies. Otherwise, gap states are ignored
	   -gap => 1
 Comments: Throws 'BadArgs' exception if matrix holds anything other than DNA
           or RNA. The calculation also takes the IUPAC symbol S (which is C|G)
	   into account, but no other symbols (such as V, for A|C|G);

=cut

	sub calc_gc_content {
		my $self = shift;
		my $type = $self->get_type;
		if ( $type !~ /^(?:d|r)na/i ) {
			throw 'BadArgs' => "Matrix doesn't contain nucleotides";
		}
		my $freq = $self->calc_state_frequencies;
		my $total = 0;
		for ( qw(c C g G s S) ) {
			$total += $freq->{$_} if exists $freq->{$_};
		}
		return $total;
	}

=back

=head2 METHODS

=over

=item keep_chars()

Creates a cloned matrix that only keeps the characters at 
the supplied (zero-based) indices.

 Type    : Utility method
 Title   : keep_chars
 Usage   : my $clone = $object->keep_chars([6,3,4,1]);
 Function: Creates spliced clone.
 Returns : A spliced clone of the invocant.
 Args    : Required, an array ref of integers
 Comments: The columns are retained in the order in 
           which they were supplied.

=cut

	sub keep_chars {
		my ( $self, $indices_array_ref ) = @_;
		my @indices = @{ $indices_array_ref };
		my $clone = $self->clone;
		for my $seq ( @{ $clone->get_entities } ) {
			$seq->keep_entities(\@indices);
			my @anno = @{ $seq->get_annotations };	
			if ( @anno ) {
				my @re_anno = @anno[@indices];
				$seq->set_annotations(@re_anno);
			}			
		}
		my @labels = @{ $clone->get_charlabels };
		if ( @labels ) {
			my @re_labels = @labels[@indices];
			$clone->set_charlabels(\@re_labels);
		}		
		return $clone;
	}

=item prune_chars()

Creates a cloned matrix that omits the characters at 
the supplied (zero-based) indices.

 Type    : Utility method
 Title   : prune_chars
 Usage   : my $clone = $object->prune_chars([6,3,4,1]);
 Function: Creates spliced clone.
 Returns : A spliced clone of the invocant.
 Args    : Required, an array ref of integers
 Comments: The columns are retained in the order in 
           which they were supplied.

=cut

	sub prune_chars {
		my ( $self, $indices ) = @_;
		my $nchar = $self->get_nchar;
		my %indices = map { $_=>1 } @{ $indices };
		my @keep;
		for my $i ( 0 .. ( $nchar - 1 ) ) {
			push @keep, $i if not exists $indices{$i};
		}
		return $self->keep_chars(\@keep);
	}

=item bootstrap()

Creates bootstrapped clone.

 Type    : Utility method
 Title   : bootstrap
 Usage   : my $bootstrap = $object->bootstrap;
 Function: Creates bootstrapped clone.
 Returns : A bootstrapped clone of the invocant.
 Args    : Optional, a subroutine reference that returns a random
           integer between 0 (inclusive) and the argument provided
           to it (exclusive). The default implementation is to use
           sub { int( rand( shift ) ) }, a user might override this
           by providing an implementation with a better random number
           generator.
 Comments: The bootstrapping algorithm uses perl's random number
           generator to create a new series of indices (without
           replacement) of the same length as the original matrix.
           These indices are first sorted, then applied to the 
           cloned sequences. Annotations (if present) stay connected
           to the resampled cells.

=cut

	sub bootstrap {
		my $self = shift;
		my $gen = shift || sub { int(rand(shift)) };
		my $nchar = $self->get_nchar;
		my @indices;
		push @indices, $gen->($nchar) for ( 1 .. $nchar );
		@indices = sort { $a <=> $b } @indices;
		return $self->keep_chars(\@indices);
	}

=item jackknife()

Creates jackknifed clone.

 Type    : Utility method
 Title   : jackknife
 Usage   : my $bootstrap = $object->jackknife(0.5);
 Function: Creates jackknifed clone.
 Returns : A jackknifed clone of the invocant.
 Args    : * Required, a number between 0 and 1, representing the
             fraction of characters to jackknife. 
           * Optional, a subroutine reference that returns a random
             integer between 0 (inclusive) and the argument provided
             to it (exclusive). The default implementation is to use
             sub { int( rand( shift ) ) }, a user might override this
             by providing an implementation with a better random number
             generator.
 Comments: The jackknife algorithm uses perl's random number
           generator to create a new series of indices of cells to keep.
           These indices are first sorted, then applied to the 
           cloned sequences. Annotations (if present) stay connected
           to the resampled cells.

=cut

	sub jackknife {
		my ( $self, $prop ) = @_;
		if ( not looks_like_number $prop or $prop >= 1 or $prop < 0 ) {
			throw 'BadNumber' => "Jackknifing proportion must be a number between 0 and 1";
		}
		my $gen = $_[2] || sub { int(rand(shift)) };
		my $nchar = $self->get_nchar;
		my ( %indices, @indices );
		while ( scalar keys %indices < ( $nchar - int( $nchar * $prop ) ) ) {
			$indices{ $gen->($nchar) } = 1;
		}
		@indices = sort { $a <=> $b } keys %indices;
		return $self->keep_chars(\@indices);
	}

=item clone()

Clones invocant.

 Type    : Utility method
 Title   : clone
 Usage   : my $clone = $object->clone;
 Function: Creates a copy of the invocant object.
 Returns : A copy of the invocant.
 Args    : NONE

=cut

	sub clone {
		my $self = shift;
		$logger->info("cloning $self");
		my %subs = @_;
				
		# we'll clone datum objects, so no raw copying
		$subs{'set_raw'} = sub {};
		
		# we'll use the set/get_special_symbols method
		$subs{'set_missing'}   = sub {};
		$subs{'set_gap'}       = sub {};
		$subs{'set_matchchar'} = sub {};
		
		return $self->SUPER::clone(%subs);
	
	} 

=item insert()

Insert argument in invocant.

 Type    : Listable method
 Title   : insert
 Usage   : $matrix->insert($datum);
 Function: Inserts $datum in $matrix.
 Returns : Modified object
 Args    : A datum object
 Comments: This method re-implements the method by the same
           name in Bio::Phylo::Listable

=cut

	sub insert {
		my ( $self, $obj ) = @_;
		my $obj_container;
		eval { $obj_container = $obj->_container };
		if ( $@ || $obj_container != $self->_type ) {
			throw 'ObjectMismatch' => 'object not a datum object!';
		}
		$logger->info("inserting '$obj' in '$self'");
		if ( !$self->get_type_object->is_same( $obj->get_type_object ) ) {
			throw 'ObjectMismatch' => 'object is of wrong data type';
		}
		my $taxon1 = $obj->get_taxon;
		for my $ents ( @{ $self->get_entities } ) {
			if ( $obj->get_id == $ents->get_id ) {
				throw 'ObjectMismatch' => 'row already inserted';
			}
			if ($taxon1) {
				my $taxon2 = $ents->get_taxon;
				if ( $taxon2 && $taxon1->get_id == $taxon2->get_id ) {
					$logger->warn('datum linking to same taxon already existed, concatenating instead');
					$ents->concat($obj);
					return $self;
				}
			}
		}
		$self->SUPER::insert( $obj );
		return $self;
	}

=item validate()

Validates the object's contents.

 Type    : Method
 Title   : validate
 Usage   : $obj->validate
 Function: Validates the object's contents
 Returns : True or throws Bio::Phylo::Util::Exceptions::InvalidData
 Args    : None
 Comments: This method implements the interface method by the same
           name in Bio::Phylo::Matrices::TypeSafeData

=cut

	sub validate {
		my $self = shift;
		for my $row ( @{ $self->get_entities } ) {
			$row->validate;
		}
	}

=item compress_lookup()

Removes unused states from lookup table

 Type    : Method
 Title   : validate
 Usage   : $obj->compress_lookup
 Function: Removes unused states from lookup table
 Returns : $self
 Args    : None

=cut

	sub compress_lookup {
		my $self = shift;
		my $to = $self->get_type_object;
		my $lookup = $to->get_lookup;
		my %seen;
		for my $row ( @{ $self->get_entities } ) {
			my @char = $row->get_char;
			$seen{$_}++ for (@char);
		}
		for my $state ( keys %{ $lookup } ) {
			if ( not exists $seen{$state} ) {
				delete $lookup->{$state};
			}
		}
		$to->set_lookup($lookup);
		return $self;
	}

=item check_taxa()

Validates taxa associations.

 Type    : Method
 Title   : check_taxa
 Usage   : $obj->check_taxa
 Function: Validates relation between matrix and taxa block 
 Returns : Modified object
 Args    : None
 Comments: This method implements the interface method by the same
           name in Bio::Phylo::Taxa::TaxaLinker

=cut

	sub check_taxa {
		my $self = shift;

		# is linked to taxa
		if ( my $taxa = $self->get_taxa ) {
			my %taxa =
			  map { $_->get_internal_name => $_ } @{ $taxa->get_entities };
		  ROW_CHECK: for my $row ( @{ $self->get_entities } ) {
				if ( my $taxon = $row->get_taxon ) {
					next ROW_CHECK if exists $taxa{ $taxon->get_name };
				}
				my $name = $row->get_name;
				if ( exists $taxa{$name} ) {
					$row->set_taxon( $taxa{$name} );
				}
				else {
					my $taxon = $factory->create_taxon( -name => $name );
					$taxa{$name} = $taxon;
					$taxa->insert($taxon);
					$row->set_taxon($taxon);
				}
			}
		}

		# not linked
		else {
			for my $row ( @{ $self->get_entities } ) {
				$row->set_taxon();
			}
		}
		return $self;
	}

=item make_taxa()

Creates a taxa block from the objects contents if none exists yet.

 Type    : Method
 Title   : make_taxa
 Usage   : my $taxa = $obj->make_taxa
 Function: Creates a taxa block from the objects contents if none exists yet.
 Returns : $taxa
 Args    : NONE

=cut

	sub make_taxa {
		my $self = shift;
		if ( my $taxa = $self->get_taxa ) {
			return $taxa;
		}
		else {
			my %taxa;
			my $taxa = $factory->create_taxa;
			for my $row ( @{ $self->get_entities } ) {
				my $name = $row->get_internal_name;
				if ( not $taxa{$name} ) {
					$taxa{$name} = $factory->create_taxon( '-name' => $name );
				}
			}
			$taxa->insert( map { $taxa{$_} } sort { $a cmp $b } keys %taxa );
			$self->set_taxa( $taxa );
			return $taxa;
		}
	}

=back

=head2 SERIALIZERS

=over

=item to_xml()

Serializes matrix to nexml format.

 Type    : Format convertor
 Title   : to_xml
 Usage   : my $data_block = $matrix->to_xml;
 Function: Converts matrix object into a nexml element structure.
 Returns : Nexml block (SCALAR).
 Args    : Optional:
 		   -compact => 1 (for compact representation of matrix)

=cut

	sub to_xml {
		my $self = shift;		
		$logger->debug("writing $self to xml");
		my ( %args, $ids_for_states );
		if ( @_ ) {
			%args = @_;
		}
		
		# creating opening tag
		my $type = $self->get_type;
		my $verbosity = $args{'-compact'} ? 'Seqs' : 'Cells';
		my $xsi_type = 'nex:' . ucfirst($type) . $verbosity;
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $xml = $self->get_xml_tag;		
		$logger->debug("created opening tag $xml");
		
		# normalizing symbol table
		my $normalized = $self->_normalize_symbols;
		$logger->debug("normalized symbols");
		
		# the format block
		$xml .= '<format>';
		$logger->debug($xml);
		my $to = $self->get_type_object;
		$ids_for_states = $to->get_ids_for_states(1);
		
		# write state definitions 
		# this calls Datatype::to_xml method
		#  
		$xml .= $to->to_xml($normalized,$self->get_polymorphism);
		$logger->debug($xml);
		
		# write column definitions
		if ( %{ $ids_for_states } ) {
			$xml .= $self->_write_char_labels( $to->get_xml_id );
		}
		else {
			$xml .= $self->_write_char_labels();
		}
		$xml .= "\n</format>";
		
		# the matrix block
		$xml .= "\n<matrix>";
		my @char_ids;
		for ( 0 .. $self->get_nchar ) {
			push @char_ids, 'c' . ($_+1);
		}
		
		# write rows
		my $special = $self->get_type_object->get_ids_for_special_symbols(1);
		for my $row ( @{ $self->get_entities } ) {
			$xml .= "\n" . $row->to_xml(
				'-states'  => $ids_for_states,
				'-chars'   => \@char_ids,
				'-symbols' => $normalized,
				'-special' => $special,
				%args,
			);
		}
		$xml .= "\n</matrix>";
		$xml .= "\n" . sprintf( '</%s>', $self->get_tag );
		return $xml;
	}
        
        # what this does:
        # numerical states are their own keys; also want numerical 
        # representations for non-numerical states..."normalization"
        # provides this mapping....

	sub _normalize_symbols {
		my $self = shift;
		$logger->debug("normalizing symbols");
		if ( $self->get_type =~ /^standard$/i ) {
			my $to = $self->get_type_object;
			my $lookup = $self->get_lookup;
			my @states = keys %{ $lookup };
			if ( my @letters = sort { $a cmp $b } grep { /[a-z]/i } @states ) {
				my @numbers  = sort { $a <=> $b } grep { /^\d+$/ } @states;
				my $i = $numbers[-1];
				my %map = map { $_ => ++$i } @letters;
				return \%map;
			}
			else {
				return {};
			}
		}
		else {
			return {};
		}
	}
	
	sub _write_char_labels {
		my ( $self, $states_id ) = @_;
		my $xml = '';
		my $labels = $self->get_charlabels;
		for my $i ( 1 .. $self->get_nchar ) {
			$xml .= sprintf('<char id="c%s"',$i);  
			if ( $labels->[ $i - 1 ] ) {
				$xml .= sprintf(' label="%s"', $labels->[ $i - 1 ]);
			}
			if ( $states_id ) {
				$xml .= sprintf(' states="%s"', $states_id);
			}
			$xml .= '/>';
		}	
		return $xml;	
	}

=item to_nexus()

Serializes matrix to nexus format.

 Type    : Format convertor
 Title   : to_nexus
 Usage   : my $data_block = $matrix->to_nexus;
 Function: Converts matrix object into a nexus data block.
 Returns : Nexus data block (SCALAR).
 Args    : The following options are available:
 
            # if set, writes TITLE & LINK tokens
            '-links' => 1
            
            # if set, writes block as a "data" block (deprecated, but used by mrbayes),
            # otherwise writes "characters" block (default)
            -data_block => 1
            
            # if set, writes "RESPECTCASE" token
            -respectcase => 1
            
            # if set, writes "GAPMODE=(NEWSTATE or MISSING)" token
            -gapmode => 1
            
            # if set, writes "MSTAXA=(POLYMORPH or UNCERTAIN)" token
            -polymorphism => 1
            
            # if set, writes character labels
            -charlabels => 1
            
            # if set, writes state labels
            -statelabels => 1
            
            # if set, writes mesquite-style charstatelabels
            -charstatelabels => 1
            
            # by default, names for sequences are derived from $datum->get_name, if 
            # 'internal' is specified, uses $datum->get_internal_name, if 'taxon'
            # uses $datum->get_taxon->get_name, if 'taxon_internal' uses 
            # $datum->get_taxon->get_internal_name, if $key, uses $datum->get_generic($key)
            -seqnames => one of (internal|taxon|taxon_internal|$key)

=cut

	sub to_nexus {
		my $self   = shift;
		$logger->info("writing to nexus: $self");
		my %args   = @_;
		my $nchar  = $self->get_nchar;
		my $string = sprintf "BEGIN %s;\n",
		  $args{'-data_block'} ? 'DATA' : 'CHARACTERS';
		$string .=
		    "[! Characters block written by "
		  . ref($self) . " "
		  . $self->VERSION . " on "
		  . localtime() . " ]\n";

		# write links
		if ( $args{'-links'} ) {
			$string .= sprintf "\tTITLE %s;\n", $self->get_internal_name;
			$string .= sprintf "\tLINK TAXA=%s;\n",
			  $self->get_taxa->get_internal_name
			  if $self->get_taxa;
		}

	 	# dimensions token line - data block defines NTAX, characters block doesn't
		if ( $args{'-data_block'} ) {
			$string .= "\tDIMENSIONS NTAX=" . $self->get_ntax() . ' ';
			$string .= 'NCHAR=' . $nchar . ";\n";
		}
		else {
			$string .= "\tDIMENSIONS NCHAR=" . $nchar . ";\n";
		}

		# format token line
		$string .= "\tFORMAT DATATYPE=" . $self->get_type();
		$string .= ( $self->get_respectcase ? " RESPECTCASE" : "" )
		  if $args{'-respectcase'};    # mrbayes no like
		$string .= " MATCHCHAR=" . $self->get_matchchar if $self->get_matchchar;
		$string .= " MISSING=" . $self->get_missing();
		$string .= " GAP=" . $self->get_gap()           if $self->get_gap();
		$string .= ";\n";

		# options token line (mrbayes no like)
		if ( $args{'-gapmode'} or $args{'-polymorphism'} ) {
			$string .= "\tOPTIONS ";
			$string .=
			  "GAPMODE=" . ( $self->get_gapmode ? "NEWSTATE " : "MISSING " )
			  if $args{'-gapmode'};
			$string .= "MSTAXA="
			  . ( $self->get_polymorphism ? "POLYMORPH " : "UNCERTAIN " )
			  if $args{'-polymorphism'};
			$string .= ";\n";
		}

		# charlabels token line
		if ( $args{'-charlabels'} ) {
			my $charlabels;
			if ( my @labels = @{ $self->get_charlabels } ) {
				my $i = 1;
				for my $label (@labels) {
					$charlabels .= $label =~ /\s/ ? "\n\t\t [$i] '$label'" : "\n\t\t [$i] $label";
					$i++;
				}
				$string .= "\tCHARLABELS$charlabels\n\t;\n";
			}
		}
		
		# statelabels token line
		if ( $args{'-statelabels'} ) {
		    my $statelabels;
		    if ( my @labels = @{ $self->get_statelabels } ) {
		        my $i = 1;
		        for my $labelset ( @labels ) {
		            $statelabels .= "\n\t\t $i";
		            for my $label ( @{ $labelset } ) {
		                $statelabels .= $label =~ /\s/ ? "\n\t\t\t'$label'" : "\n\t\t\t$label";
		                $i++;
		            }
		            $statelabels .= ',';
		        }
		        $string .= "\tSTATELABELS$statelabels\n\t;\n";
		    }
		}
		
		# charstatelabels token line
		if ( $args{'-charstatelabels'} ) {
		    my @charlabels = @{ $self->get_charlabels };
		    my @statelabels = @{ $self->get_statelabels };
		    if ( @charlabels and @statelabels ) {
		        my $charstatelabels;
		        my $nlabels = $self->get_nchar - 1;
		        for my $i ( 0 .. $nlabels ) {
		            $charstatelabels .= "\n\t\t" . ( $i + 1 );
		            if ( my $label = $charlabels[$i] ) {
		                $charstatelabels .= $label =~ /\s/ ? " '$label' /" : " $label /";
		            }
		            else {
		                $charstatelabels .= " ' ' /";
		            }
		            if ( my $labelset = $statelabels[$i] ) {
		                for my $label ( @{ $labelset } ) {
		                    $charstatelabels .= $label =~ /\s/ ? " '$label'" : " $label";
		                }
		            }
		            else {
		                $charstatelabels .= " ' '";
		            }
		            $charstatelabels .= $i == $nlabels ? "\n\t;" : ',';
		        }
		        $string .= "\tCHARSTATELABELS$charstatelabels\n\t;\n";
		    }
		}

		# ...and write matrix!
		$string .= "\tMATRIX\n";
		my $length = 0;
		foreach my $datum ( @{ $self->get_entities } ) {
			$length = length( $datum->get_nexus_name )
			  if length( $datum->get_nexus_name ) > $length;
		}
		$length += 4;
		my $sp = ' ';
		foreach my $datum ( @{ $self->get_entities } ) {
			$string .= "\t\t";

			# construct name
			my $name;
			if ( not $args{'-seqnames'} ) {
				$name = $datum->get_nexus_name;
			}
			elsif ( $args{'-seqnames'} =~ /^internal$/i ) {
				$name = $datum->get_nexus_name;
			}
			elsif ( $args{'-seqnames'} =~ /^taxon/i and $datum->get_taxon ) {
				if ( $args{'-seqnames'} =~ /^taxon_internal$/i ) {
					$name = $datum->get_taxon->get_nexus_name;
				}
				elsif ( $args{'-seqnames'} =~ /^taxon$/i ) {
					$name = $datum->get_taxon->get_nexus_name;
				}
			}
			else {
				$name = $datum->get_generic( $args{'-seqnames'} );
			}
			$name = $datum->get_nexus_name if not $name;
			$string .= $name . ( $sp x ( $length - length($name) ) );
			my $char =  $datum->get_char;
			$string .= $char . "\n";
		}
		$string .= "\t;\nEND;\n";
		return $string;
	}

=item to_dom()

Analog to to_xml.

 Type    : Serializer
 Title   : to_dom
 Usage   : $matrix->to_dom
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an Element object
 Args    : Optional:
           -compact => 1 : renders characters as sequences,
                           not individual cells

=cut

	sub to_dom {	
		my $self = shift;
		my $dom = $_[0];
		my @args = @_;
		# handle dom factory object...
		if ( looks_like_instance($dom, 'SCALAR') && $dom->_type == _DOMCREATOR_ ) {
		    splice(@args, 0, 1);
		}
		else {
		    $dom = $Bio::Phylo::NeXML::DOM::DOM;
		    unless ($dom) {
				throw 'BadArgs' => 'DOM factory object not provided';
		    }
		}
		#### make sure argument handling works here...
		my ( %args, $ids_for_states );
		%args = @args if @args;
	
		my $type = $self->get_type;
		my $verbosity = $args{'-compact'} ? 'Seqs' : 'Cells';
		my $xsi_type = 'nex:' . ucfirst($type) . $verbosity;
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $elt = $self->get_dom_elt($dom);
		my $normalized = $self->_normalize_symbols;
		
		# the format block
	 	my $format_elt = $dom->create_element('-tag'=>'format');
		my $to = $self->get_type_object;
		$ids_for_states = $to->get_ids_for_states(1);
		
		# write state definitions
	
		$format_elt->set_child( $to->to_dom( $dom, $normalized, $self->get_polymorphism ) );
		
		# write column definitions
		$format_elt->set_child($_) for $self->_package_char_labels( $dom, %{ $ids_for_states } ? $to->get_xml_id : undef );
	
		$elt->set_child($format_elt);
	
		# the matrix block
	
		my $mx_elt = $dom->create_element('-tag'=>'matrix');
		my @char_ids;
		for ( 0 .. $self->get_nchar ) {
		    push @char_ids, 'c' . ($_+1);
		}
		
		# write rows
		my $special = $self->get_type_object->get_ids_for_special_symbols(1);
		for my $row ( @{ $self->get_entities } ) {
		    # $row->to_dom is calling ...::Datum::to_dom...
		    $mx_elt->set_child( 
				$row->to_dom( $dom,
				    '-states'  => $ids_for_states,
				    '-chars'   => \@char_ids,
				    '-symbols' => $normalized,
				    '-special' => $special,
				    %args,
				)
			);
		}
		$elt->set_child($mx_elt);
		return $elt;
	}

	# returns an array of elements
	sub _package_char_labels {
		my ( $self, $dom, $states_id ) = @_;
		my @elts;
		my $labels = $self->get_charlabels;
		for my $i ( 1 .. $self->get_nchar ) {
		    my $char_id = 'c' . $i;
		    my $label   = $labels->[ $i - 1 ];
		    my $elt = $dom->create_element('-tag'=>'char');
		    $elt->set_attributes( 'id' => $char_id );
		    $elt->set_attributes( 'label' => $label ) if $label;
		    $elt->set_attributes( 'states' => $states_id ) if $states_id;
		    push @elts, $elt;
		}	
		return @elts;
	}	
	
	sub _tag       { 'characters' }
	sub _type      { $CONSTANT_TYPE }
	sub _container { $CONSTANT_CONTAINER }

	sub _cleanup {
		my $self = shift;
		$logger->info("cleaning up '$self'");
		my $id = $self->get_id;
		for (@inside_out_arrays) {
			delete $_->{$id} if defined $id and exists $_->{$id};
		}
	}

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Taxa::TaxaLinker>

This object inherits from L<Bio::Phylo::Taxa::TaxaLinker>, so the
methods defined therein are also applicable to L<Bio::Phylo::Matrices::Matrix>
objects.

=item L<Bio::Phylo::Matrices::TypeSafeData>

This object inherits from L<Bio::Phylo::Matrices::TypeSafeData>, so the
methods defined therein are also applicable to L<Bio::Phylo::Matrices::Matrix>
objects.

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

}
1;

__DATA__

my %CONSERVATION_GROUPS = (
        'strong' => [ qw(
		STA
		NEQK
		NHQK
		NDEQ
		QHRK
		MILV
		MILF
		HY
		FYW
	)],
	'weak' => [ qw(
                CSA
		ATV
		SAG
		STNK
		STPA
		SGND
		SNDEQK
		NDEQHK
		NEQHRK
		FVLIM
		HFY
	)],
);

sub description {
	my ( $self, $desc ) = @_;
	if ( defined $desc ) {
		$self->set_desc( $desc );
	}
	return $self->get_desc;
}

sub num_sequences {
	my ( $self, $num ) = @_;
	# setter?
	return scalar @{ $self->get_entities };
}

sub datatype {
	my ( $self, $type ) = @_;
	# setter?
	return uc $self->get_type;
}

sub score {
	my ( $self, $score ) = @_;
	if ( defined $score ) {
		$self->set_score( $score );
	}
	return $self->get_score;
}

sub add_seq {
    my ( $self, $seq, $order ) = @_;
    $self->insert( $seq );
}

sub remove_seq {
    my ( $self, $seq ) = @_;
    $self->delete( $seq );
}

sub purge {
 $logger->warn 
}

sub sort_alphabetically {
    my $self = shift;
    my @sorted = map  { $_->[0] }
                 sort { $a->[1] cmp $b->[1] }
                 map  { [ $_, $_->get_name ] }
                 @{ $self->get_entities };
    $self->clear;
    $self->insert(@sorted);
    return @sorted;
}

sub each_seq {
    my $self = shift;
    return @{ $self->get_entities };
}

sub each_alphabetically {
    my $self = shift;
    return map  { $_->[0] }
           sort { $a->[1] cmp $b->[1] }
           map  { [ $_, $_->get_name ] } @{ $self->get_entities };
}

sub each_seq_with_id {
    my ( $self, $name ) = @_;
    return @{ 
        $self->get_by_regular_expression(
            '-value' => 'get_name',
            '-match' => qr/^\Q$name\E$/
        )
    }
}

sub get_seq_by_pos {
    my ( $self, $pos ) = @_;
    return $self->get_by_index( $pos - 1 );
}

sub select {
    my ( $self, $start, $end ) = @_;
    my $clone = $self->clone;
    my @contents = @{ $clone->get_entities };
    my @deleteme;
    for my $i ( 0 .. $#contents ) {
        if ( $i < $start - 1 or $i > $end - 1 ) {
            push @deleteme, $contents[$i];
        }
    }
    $clone->delete( $_ ) for @deleteme;
    return $clone;
}

sub select_noncont {
    my ( $self, @indices ) = @_;
    my $clone = $self->clone;
    my @contents = @{ $clone->get_entities };
    my ( @deleteme, %keep );
    %keep = map { ( $_ - 1 ) => 1 } @indices;
    for my $i ( 0 .. $#contents ) {
        if ( not exists $keep{$i} ) {
            push @deleteme, $contents[$i];
        }
    }
    $clone->delete( $_ ) for @deleteme;
    return $clone;
}

sub slice {
    my ( $self, $start, $end, $include_gapped ) = @_;
    my $clone = $self->clone;
    my $gap = $self->get_gap;
    SEQ: for my $seq ( @{ $clone->get_entities } ) {
        my @char = $self->get_char;
        my @slice = splice @char, ( $start - 1 ), ( $end - $start - 1 );
        if ( not $include_gapped ) {
            if ( not grep { $_ !~ /^\Q$gap\E$/ } @slice ) {
                next SEQ;
            }
        }
        $seq->set_char(@slice);
    }
}

sub map_chars {
    my ( $self, $from, $to ) = @_;
    for my $seq ( @{ $self->get_entities } ) {
        my @char = $seq->get_char;
        for my $c ( @char ) {
            $c =~ s/$from/$to/;
        }
        $seq->set_char( @char );
    }
}

sub uppercase {
    my $self = shift;
    for my $seq ( @{ $self->get_entities } ) {
        my @char = $seq->get_char;
        my @uc = map { uc $_ } @char;
        $seq->set_char(@uc);
    }
}

# from simplealign
sub match_line {
	my ($self,$matchlinechar, $strong, $weak) = @_;
	my %matchchars = ('match'    => $matchlinechar || '*',
							  'weak'     => $weak          || '.',
							  'strong'   => $strong        || ':',
							  'mismatch' => ' ',
						  );

	my @seqchars;
	my $alphabet;
	foreach my $seq ( $self->each_seq ) {
		push @seqchars, [ split(//, uc ($seq->seq)) ];
		$alphabet = $seq->alphabet unless defined $alphabet;
	}
	my $refseq = shift @seqchars;
	# let's just march down the columns
	my $matchline;
 POS:
	foreach my $pos ( 0..$self->length ) {
		my $refchar = $refseq->[$pos];
		my $char = $matchchars{'mismatch'};
		unless( defined $refchar ) {
			last if $pos == $self->length; # short circuit on last residue
			# this in place to handle jason's soon-to-be-committed
			# intron mapping code
			goto bottom;
		}
		my %col = ($refchar => 1);
		my $dash = ($refchar eq '-' || $refchar eq '.' || $refchar eq ' ');
		foreach my $seq ( @seqchars ) {
			next if $pos >= scalar @$seq;
			$dash = 1 if( $seq->[$pos] eq '-' || $seq->[$pos] eq '.' ||
							  $seq->[$pos] eq ' ' );
			$col{$seq->[$pos]}++ if defined $seq->[$pos];
		}
		my @colresidues = sort keys %col;

		# if all the values are the same
		if( $dash ) { $char =  $matchchars{'mismatch'} }
		elsif( @colresidues == 1 ) { $char = $matchchars{'match'} }
		elsif( $alphabet eq 'protein' ) { # only try to do weak/strong
			# matches for protein seqs
	    TYPE:
			foreach my $type ( qw(strong weak) ) {
				# iterate through categories
				my %groups;
				# iterate through each of the aa in the col
				# look to see which groups it is in
				foreach my $c ( @colresidues ) {
					foreach my $f ( grep { index($_,$c) >= 0 } @{$CONSERVATION_GROUPS{$type}} ) {
						push @{$groups{$f}},$c;
					}
				}
			 GRP:
				foreach my $cols ( values %groups ) {
					@$cols = sort @$cols;
					# now we are just testing to see if two arrays
					# are identical w/o changing either one
					# have to be same len
					next if( scalar @$cols != scalar @colresidues );
					# walk down the length and check each slot
					for($_=0;$_ < (scalar @$cols);$_++ ) {
						next GRP if( $cols->[$_] ne $colresidues[$_] );
					}
					$char = $matchchars{$type};
					last TYPE;
				}
			}
		}
	 bottom:
		$matchline .= $char;
	}
	return $matchline;
}

sub match {
    my ( $self, $match ) = @_;
    if ( defined $match ) {
        $self->set_matchchar($match);
    }
    else {
        $self->set_matchchar('.');
    }
    $match = $self->get_matchchar;
    my $lookup = $self->get_type_object->get_lookup->{$match} = [ $match ];    
    my @seqs = @{ $self->get_entities };
    my @firstseq = $seqs[0]->get_char;
    for my $i ( 1 .. $#seqs ) {
        my @char = $seqs[$i]->get_char;
        for my $j ( 0 .. $#char ) {
            if ( $char[$j] eq $firstseq[$j] ) {
                $char[$j] = $match;
            }
        }
        $seqs[$i]->set_char(@char);
    }
    1;
}

sub unmatch {
    my ( $self, $match ) = @_;
    if ( defined $match ) {
        $self->set_matchchar($match);
    }
    else {
        $self->set_matchchar('.');
    }
    $match = $self->get_matchchar;
    my @seqs = @{ $self->get_entities };
    my @firstseq = $seqs[0]->get_char;
    for my $i ( 1 .. $#seqs ) {
        my @char = $seqs[$i]->get_char;
        for my $j ( 0 .. $#char ) {
            if ( $char[$j] eq $match ) {
                $char[$j] = $firstseq[$j];
            }
        }
        $seqs[$i]->set_char(@char);
    }
    1;
}

sub id {
    my ( $self, $name ) = @_;
    if ( defined $name ) {
        $self->set_name( $name );
    }
    return $self->get_name;
}

sub missing_char {
    my ( $self, $missing ) = @_;
    if ( defined $missing ) {
        $self->set_missing( $missing );
    }
    return $self->get_missing;
}

sub match_char {
    my ( $self, $match ) = @_;
    if ( defined $match ) {
        $self->set_matchchar( $match );
    }
    return $self->get_matchchar;
}

sub gap_char {
    my ( $self, $gap ) = @_;
    if ( defined $gap ) {
        $self->set_gap( $gap );
    }
    return $self->get_gap;
}

sub symbol_chars {
    my ( $self, $includeextra ) = @_;
	my %seen;
	for my $row ( @{ $self->get_entities } ) {
		my @char = $row->get_char;
		$seen{$_} = 1 for @char;
	}
    return keys %seen if $includeextra;
    my $special_values = $self->get_special_symbols;
    my %special_keys   = map { $_ => 1 } values %{ $special_values };
    return grep { ! $special_keys{$_} } keys %seen;
}

sub consensus_string {
	my $self = shift;
	my $to = $self->get_type_object;
	my $ntax = $self->get_ntax;
	my $nchar = $self->get_nchar;
	my @consensus;
	for my $i ( 0 .. $ntax - 1 ) {
		my ( @column, %column );
		for my $j ( 0 .. $nchar - 1 ) {
			$column{ $self->get_by_index($i)->get_by_index($j) } = 1;
		}
		@column = keys %column;
		push @consensus, $to->get_symbol_for_states(@column);
	}
	return join '', @consensus;
}

sub consensus_iupac {
 $logger->warn 
}

sub is_flush { 1 }

sub length { shift->get_nchar }

sub maxname_length { $logger->warn }

sub no_residues { $logger->warn }

sub no_sequences {
    my $self = shift;
    return scalar @{ $self->get_entities };
}

sub percentage_identity { $logger->warn }

# from simplealign
sub average_percentage_identity{
   my ($self,@args) = @_;

   my @alphabet = ('A','B','C','D','E','F','G','H','I','J','K','L','M',
                   'N','O','P','Q','R','S','T','U','V','W','X','Y','Z');

   my ($len, $total, $subtotal, $divisor, $subdivisor, @seqs, @countHashes);

   if (! $self->is_flush()) {
       throw 'Generic' => "All sequences in the alignment must be the same length";
   }

   @seqs = $self->each_seq();
   $len = $self->length();

   # load the each hash with correct keys for existence checks

   for( my $index=0; $index < $len; $index++) {
       foreach my $letter (@alphabet) {
       		$countHashes[$index] = {} if not $countHashes[$index];
	   $countHashes[$index]->{$letter} = 0;
       }
   }
   foreach my $seq (@seqs)  {
       my @seqChars = split //, $seq->seq();
       for( my $column=0; $column < @seqChars; $column++ ) {
	   my $char = uc($seqChars[$column]);
	   if (exists $countHashes[$column]->{$char}) {
	       $countHashes[$column]->{$char}++;
	   }
       }
   }

   $total = 0;
   $divisor = 0;
   for(my $column =0; $column < $len; $column++) {
       my %hash = %{$countHashes[$column]};
       $subdivisor = 0;
       foreach my $res (keys %hash) {
	   $total += $hash{$res}*($hash{$res} - 1);
	   $subdivisor += $hash{$res};
       }
       $divisor += $subdivisor * ($subdivisor - 1);
   }
   return $divisor > 0 ? ($total / $divisor )*100.0 : 0;
}

# from simplealign
sub overall_percentage_identity{
   my ($self, $length_measure) = @_;

   my @alphabet = ('A','B','C','D','E','F','G','H','I','J','K','L','M',
                   'N','O','P','Q','R','S','T','U','V','W','X','Y','Z');

   my ($len, $total, @seqs, @countHashes);

   my %enum = map {$_ => 1} qw (align short long);

   throw 'Generic' => "Unknown argument [$length_measure]" 
       if $length_measure and not $enum{$length_measure};
   $length_measure ||= 'align';

   if (! $self->is_flush()) {
       throw 'Generic' => "All sequences in the alignment must be the same length";
   }

   @seqs = $self->each_seq();
   $len = $self->length();

   # load the each hash with correct keys for existence checks
   for( my $index=0; $index < $len; $index++) {
       foreach my $letter (@alphabet) {
       		$countHashes[$index] = {} if not $countHashes[$index];
	   $countHashes[$index]->{$letter} = 0;
       }
   }
   foreach my $seq (@seqs)  {
       my @seqChars = split //, $seq->seq();
       for( my $column=0; $column < @seqChars; $column++ ) {
	   my $char = uc($seqChars[$column]);
	   if (exists $countHashes[$column]->{$char}) {
	       $countHashes[$column]->{$char}++;
	   }
       }
   }

   $total = 0;
   for(my $column =0; $column < $len; $column++) {
       my %hash = %{$countHashes[$column]};
       foreach ( values %hash ) {
	   next if( $_ == 0 );
	   $total++ if( $_ == scalar @seqs );
	   last;
       }
   }

   if ($length_measure eq 'short') {
       ## find the shortest length
       $len = 0;
       foreach my $seq ($self->each_seq) {
           my $count = $seq->seq =~ tr/[A-Za-z]//;
           if ($len) {
               $len = $count if $count < $len;
           } else {
               $len = $count;
           }
       }
   }
   elsif ($length_measure eq 'long') {
       ## find the longest length
       $len = 0;
       foreach my $seq ($self->each_seq) {
           my $count = $seq->seq =~ tr/[A-Za-z]//;
           if ($len) {
               $len = $count if $count > $len;
           } else {
               $len = $count;
           }
       }
   }

   return ($total / $len ) * 100.0;
}

sub column_from_residue_number {
    my ( $self, $seqname, $resnumber ) = @_;
    my $col;
    if ( my $seq = $self->get_by_name($seqname) ) {
        my $gap  = $seq->get_gap;
        my @char = $seq->get_char;
        for my $i ( 0 .. $#char ) {
            $col++ if $char[$i] ne $gap;
            if ( $col + 1 == $resnumber ) {
                return $i + 1;
            }
        }
    }
}

sub displayname {
    my ( $self, $name, $disname ) = @_;
	my $seq;
	$self->visit( sub{ $seq = $_[0] if $_[0]->get_nse eq $name } );
    $self->throw("No sequence with name [$name]") unless $seq;
	my $disnames = $self->get_generic( 'displaynames' ) || {};
    if ( $disname and  $name ) {
    	$disnames->{$name} = $disname;
		return $disname;
    }
    elsif( defined $disnames->{$name} ) {
		return  $disnames->{$name};
    } 
    else {
		return $name;
    }
}

# from SimpleAlign
sub maxdisplayname_length {
    my $self = shift;
    my $maxname = (-1);
    my ($seq,$len);
    foreach $seq ( $self->each_seq() ) {
		$len = CORE::length $self->displayname($seq->get_nse());	
		if( $len > $maxname ) {
		    $maxname = $len;
		}
    }
    return $maxname;
}

# from SimpleAlign
sub set_displayname_flat {
    my $self = shift;
    my ($nse,$seq);

    foreach $seq ( $self->each_seq() ) {
	$nse = $seq->get_nse();
	$self->displayname($nse,$seq->id());
    }
    return 1;
}

sub set_displayname_count { $logger->warn }

sub set_displayname_normal { $logger->warn }

sub accession {
	my ( $self, $acc ) = @_;
	if ( defined $acc ) {
		$self->set_generic( 'accession' => $acc );
	}
	return $self->get_generic( 'accession' );
}

sub source {
	my ( $self, $source ) = @_;
	if ( defined $source ) {
		$self->set_generic( 'source' => $source );
	}
	return $self->get_generic( 'source' );
}

sub annotation {
	my ( $self, $anno ) = @_;
	if ( defined $anno ) {
		$self->set_generic( 'annotation' => $anno );
	}
	return $self->get_generic( 'annotation' );
}

sub consensus_meta {
	my ( $self, $meta ) = @_;
	if ( defined $meta ) {
		$self->set_generic( 'consensus_meta' => $meta );
	}
	return $self->get_generic( 'consensus_meta' );
}

# XXX this might be removed, and instead inherit from SimpleAlign
sub max_metaname_length {
    my $self = shift;
    my $maxname = (-1);
    my ($seq,$len);
    
    # check seq meta first
    for $seq ( $self->each_seq() ) {
        next if !$seq->isa('Bio::Seq::MetaI' || !$seq->meta_names);
        for my $mtag ($seq->meta_names) {
            $len = CORE::length $mtag;
            if( $len > $maxname ) {
                $maxname = $len;
            }
        }
    }
    
    # alignment meta
    for my $meta ($self->consensus_meta) {
        next unless $meta;
        for my $name ($meta->meta_names) {
            $len = CORE::length $name;
            if( $len > $maxname ) {
                $maxname = $len;
            }
        }
    }

    return $maxname;
}


sub get_SeqFeatures { return }


