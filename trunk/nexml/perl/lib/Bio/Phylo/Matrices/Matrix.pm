# $Id: Matrix.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Matrices::Matrix;
use vars '@ISA';
use strict;
use Bio::Phylo::Factory;
use Bio::Phylo::Taxa::TaxaLinker;
use Bio::Phylo::IO qw(unparse);
use Bio::Phylo::Util::CONSTANT qw(:objecttypes);
use Bio::Phylo::Util::Exceptions qw(throw);
use Bio::Phylo::Matrices::TypeSafeData;
use UNIVERSAL qw(isa);
@ISA = qw(
  Bio::Phylo::Matrices::TypeSafeData
  Bio::Phylo::Taxa::TaxaLinker
);
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

Bio::Phylo::Matrices::Matrix - Character state matrix.

=head1 SYNOPSIS

 use Bio::Phylo::Matrices::Matrix;
 use Bio::Phylo::Taxa;
 use Bio::Phylo::Taxa::Taxon;

 # instantiate taxa object
 my $taxa = Bio::Phylo::Taxa->new();
 for ( 'Homo sapiens', 'Pan paniscus', 'Pan troglodytes' ) {
     $taxa->insert( Bio::Phylo::Taxa::Taxon->new( '-name' => $_ ) );
 }

 # instantiate matrix object, 'standard' data type. All categorical
 # data types follow semantics like this, though with different
 # symbols in lookup table and matrix
 my $standard_matrix = Bio::Phylo::Matrices::Matrix->new(
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

		# go up inheritance tree, eventually get an ID
		my $self = $class->SUPER::new( '-tag' => 'characters', @_ );
		return $self;
	}

=back

=head2 MUTATORS

=over

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
		if ( isa( $statelabels, 'ARRAY' ) ) {
			for my $col ( @{$statelabels} ) {
				if ( not isa( $col, 'ARRAY') ) {
					throw 'BadArgs' => "statelabels must be a two dimensional array ref";
				}
			}
		}

		# it's defined but not an array ref
		elsif ( defined $statelabels && ! isa( $statelabels, 'ARRAY' ) ) {
			throw 'BadArgs' => "statelabels must be a two dimensional array ref";
		}

		# it's either a valid array ref, or nothing, i.e. a reset
		$statelabels{$$self} = $statelabels || [];
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
		if ( isa( $charlabels, 'ARRAY' ) ) {
			for my $label ( @{$charlabels} ) {
				if ( ref $label ) {
					throw 'BadArgs' => "charlabels must be an array ref of scalars";
				}
			}
		}

		# it's defined but not an array ref
		elsif ( defined $charlabels && ! isa( $charlabels, 'ARRAY' ) ) {
			throw 'BadArgs' => "charlabels must be an array ref of scalars";
		}

		# it's either a valid array ref, or nothing, i.e. a reset
		$charlabels{$$self} = defined $charlabels ? $charlabels : [];
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
		$gapmode{$$self} = !!$gapmode;
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
			$matchchar{$$self} = $match;
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
		$polymorphism{$$self} = !!$poly;
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
			if ( isa( $raw, 'ARRAY' ) ) {
				my @rows;
				for my $row ( @{$raw} ) {
					if ( defined $row ) {
						if ( isa( $row, 'ARRAY' ) ) {
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
		$case_sensitivity{$$self} = !!$case_sensitivity;
		return $self;
	}

=back

=head2 ACCESSORS

=over

=item get_statelabels()

Retrieves state labels.

 Type    : Accessor
 Title   : get_statelabels
 Usage   : my @statelabels = @{ $matrix->get_statelabels };
 Function: Retrieves state labels.
 Returns : ARRAY
 Args    : None.

=cut

	sub get_statelabels { $statelabels{ ${ $_[0] } } || [] }

=item get_charlabels()

Retrieves character labels.

 Type    : Accessor
 Title   : get_charlabels
 Usage   : my @charlabels = @{ $matrix->get_charlabels };
 Function: Retrieves character labels.
 Returns : ARRAY
 Args    : None.

=cut

	sub get_charlabels { $charlabels{ ${ $_[0] } } || [] }

=item get_gapmode()

Returns matrix gapmode.

 Type    : Accessor
 Title   : get_gapmode
 Usage   : do_something() if $matrix->get_gapmode;
 Function: Returns matrix gapmode ( false = missing, true = fifth state )
 Returns : boolean
 Args    : none

=cut

	sub get_gapmode { $gapmode{ ${ $_[0] } } }

=item get_matchchar()

Returns matrix match character.

 Type    : Accessor
 Title   : get_matchchar
 Usage   : my $char = $matrix->get_matchchar;
 Function: Returns matrix match character (default is '.')
 Returns : SCALAR
 Args    : none

=cut

	sub get_matchchar { $matchchar{ ${ $_[0] } } || '.' }

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
#		my $i     = 1;
		for my $row ( @{ $self->get_entities } ) {
			my $rowlength = scalar( @{ $row->get_entities } ) + $row->get_position - 1;
# 			$logger->debug(
# 				sprintf( "counted %s chars in row %s", $rowlength, $i++ ) );
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

	sub get_polymorphism { $polymorphism{ ${ $_[0] } } }

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

	sub get_respectcase { $case_sensitivity{ ${ $_[0] } } }

=back

=head2 METHODS

=over

=item bootstrap()

Creates bootstrapped clone.

 Type    : Utility method
 Title   : bootstrap
 Usage   : my $bootstrap = $object->bootstrap;
 Function: Creates bootstrapped clone.
 Returns : A bootstrapped clone of the invocant.
 Args    : NONE
 Comments: The bootstrapping algorithm uses perl's random number
           generator to create a new series of indices (without
           replacement) of the same length as the original matrix.
           These indices are first sorted, then applied to the 
           cloned sequences. Annotations (if present) stay connected
           to the resampled cells.

=cut

	sub bootstrap {
		my $self = shift;		
		my $clone = $self->clone;
		my $nchar = $clone->get_nchar;
		my @indices;
		push @indices, int(rand($nchar)) for ( 1 .. $nchar );
		@indices = sort { $a <=> $b } @indices;
		for my $row ( @{ $clone->get_entities } ) {
			my @anno = @{ $row->get_annotations };	
			my @char = @{ $row->get_entities };
			my @resampled = @char[@indices];
			$row->set_char(@resampled);
			if ( @anno ) {
				my @re_anno = @anno[@indices];
				$row->set_annotations(@re_anno);
			}
		}
		my @labels = @{ $clone->get_charlabels };
		if ( @labels ) {
			my @re_labels = @labels[@indices];
			$clone->set_charlabels(\@re_labels);
		}
		return $clone;
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
		
		return $self->SUPER::clone(%subs);
	
	} 

=item to_xml()

Serializes matrix to nexml format.

 Type    : Format convertor
 Title   : to_xml
 Usage   : my $data_block = $matrix->to_xml;
 Function: Converts matrix object into a nexml element structure.
 Returns : Nexml block (SCALAR).
 Args    : NONE

=cut

	sub to_xml {
		my $self = shift;
		my $type = $self->get_type;
		my $xsi_type = 'nex:' . ucfirst($type) . 'Cells';
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $xml = $self->get_xml_tag;
		
		# the format block
		$xml .= "\n<format>";
		my $to = $self->get_type_object;
		my $ids_for_states = $to->get_ids_for_states(1);
		
		# write state definitions
		$xml .= $to->to_xml;
		
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
		for my $row ( @{ $self->get_entities } ) {
			$xml .= "\n" . $row->to_xml(
				'-states' => $ids_for_states,
				'-chars'  => \@char_ids,
			);
		}
		$xml .= "\n</matrix>";
		$xml .= "\n" . sprintf( '</%s>', $self->get_tag );
		return $xml;
	}
	
	sub _write_char_labels {
		my ( $self, $states_id ) = @_;
		my $xml = '';
		my $labels = $self->get_charlabels;
		for my $i ( 1 .. $self->get_nchar ) {
			my $char_id = 'c' . $i;
			my $label   = $labels->[ $i - 1 ];
			
			# have state definitions (categorical data)
			if ( $states_id ) {
				if ( $label ) {
					$xml .= "\n" . sprintf('<char id="%s" label="%s" states="%s"/>', $char_id, $label, $states_id);
				}
				else {
					$xml .= "\n" . sprintf('<char id="%s" states="%s"/>', $char_id, $states_id);
				}
			}
			
			# must be continuous characters (because no state definitions)
			else {
				if ( $label ) {
					$xml .= "\n" . sprintf('<char id="%s" label="%s"/>', $char_id, $label);
				}
				else {
					$xml .= "\n" . sprintf('<char id="%s"/>', $char_id);
				}
			}
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
 	       
		   # by default, names for sequences are derived from $datum->get_name, if 
		   # 'internal' is specified, uses $datum->get_internal_name, if 'taxon'
		   # uses $datum->get_taxon->get_name, if 'taxon_internal' uses 
		   # $datum->get_taxon->get_internal_name, if $key, uses $datum->get_generic($key)
		   -tipnames => one of (internal|taxon|taxon_internal|$key)

=cut

	sub to_nexus {
		my $self   = shift;
		$logger->info("writing to nexus: $self");
		my %args   = @_;
		my $nchar  = $self->get_nchar;
		my $string = sprintf "BEGIN %s;\n",
		  $args{'-data_block'} ? 'DATA' : 'CHARACTERS';
		$string .=
		    "[!\n\tCharacters block written by "
		  . ref($self) . " "
		  . $self->VERSION . "\n\ton "
		  . localtime() . " from object "
		  . $self->get_internal_name . " (id: "
		  . $self->get_id . ") \n]\n";

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
				for my $label (@labels) {
					$charlabels .= $label =~ /\s/ ? " '$label'" : " $label";
				}
				$string .= "\tCHARLABELS$charlabels;\n";
			}
		}

		# ...and write matrix!
		$string .= "\tMATRIX\n";
		my $length = 0;
		foreach my $datum ( @{ $self->get_entities } ) {
			$length = length( $datum->get_name )
			  if length( $datum->get_name ) > $length;
		}
		$length += 4;
		my $sp = ' ';
		foreach my $datum ( @{ $self->get_entities } ) {
			$string .= "\t\t";

			# construct name
			my $name;
			if ( not $args{'-seqnames'} ) {
				$name = $datum->get_name;
			}
			elsif ( $args{'-seqnames'} =~ /^internal$/i ) {
				$name = $datum->get_internal_name;
			}
			elsif ( $args{'-seqnames'} =~ /^taxon/i and $datum->get_taxon ) {
				if ( $args{'-seqnames'} =~ /^taxon_internal$/i ) {
					$name = $datum->get_taxon->get_internal_name;
				}
				elsif ( $args{'-seqnames'} =~ /^taxon$/i ) {
					$name = $datum->get_taxon->get_name;
				}
			}
			else {
				$name = $datum->get_generic( $args{'-seqnames'} );
			}
			$name = $datum->get_internal_name if not $name;
			$string .= $name . ( $sp x ( $length - length($name) ) );
			my @characters;
			for my $i ( 0 .. ( $nchar - 1 ) ) {
				push @characters, $datum->get_by_index($i);
			}
			$string .= $self->get_type_object->join( \@characters ) . "\n";
		}
		$string .= "\t;\nEND;\n";
		return $string;
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
	
	sub _type      { $CONSTANT_TYPE }
	sub _container { $CONSTANT_CONTAINER }

	sub _cleanup {
		my $self = shift;
		$logger->info("cleaning up '$self'");
		my $id = $$self;
		for (@inside_out_arrays) {
			delete $_->{$id} if defined $id and exists $_->{$id};
		}
	}

=back

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

=head1 REVISION

 $Id: Matrix.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

}
1;
