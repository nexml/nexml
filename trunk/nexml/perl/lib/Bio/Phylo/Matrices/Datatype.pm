# $Id: Datatype.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Matrices::Datatype;
use Bio::Phylo::Util::XMLWritable;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT 'looks_like_hash';
use strict;
use vars '@ISA';
@ISA = qw(Bio::Phylo::Util::XMLWritable);

{
 
 	my $logger = __PACKAGE__->get_logger;
    
    my @fields = \( my ( %lookup, %missing, %gap ) );

=head1 NAME

Bio::Phylo::Matrices::Datatype - Superclass for objects that validate
character data.

=head1 SYNOPSIS

 # No direct usage

=head1 DESCRIPTION

This is a superclass for objects that validate character data. Objects that
inherit from this class (typically those in the
Bio::Phylo::Matrices::Datatype::* namespace) can check strings and arrays of
character data for invalid symbols, and split and join strings and arrays
in a way appropriate for the type (i.e. on whitespace for continuous data,
on single characters for categorical data). The datatype objects are used by
L<Bio::Phylo::Matrices::Matrix> objects and L<Bio::Phylo::Matrices::Datum>
objects in an arrangement akin to the Delegation design pattern
(e.g. see L<http://www.c2.com/cgi/wiki?DelegationPattern>). There is really no
normal usage in which you'd have to deal with this object directly.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Datatype constructor.

 Type    : Constructor
 Title   : new
 Usage   : No direct usage, is called by TypeSafaData classes;
 Function: Instantiates a Datatype object
 Returns : a Bio::Phylo::Matrices::Datatype child class
 Args    : $type (optional, one of continuous, custom, dna,
           mixed, protein, restriction, rna, standard)

=cut

    sub new {
        my $package = shift;
        my $type = ucfirst( lc( shift ) );
        if ( not $type ) {
        	throw 'BadArgs' => "No subtype specified!";
        }
        if ( $type eq 'Nucleotide' ) {
            $logger->warn("'nucleotide' datatype requested, using 'dna'");
            $type = 'Dna';
        }
        my $typeclass = __PACKAGE__ . '::' . $type;
        my $self      = __PACKAGE__->SUPER::new( '-tag' => 'states' ); 
        eval "require $typeclass";
        if ( $@ ) {
        	throw 'BadFormat' => "'$type' is not a valid datatype";
        }
        else {
            return $typeclass->_new( $self, @_ );
        }
    }
    
    sub _new { 
        my $class = shift;
        my $self  = shift;
        my ( $lookup, $missing, $gap );
        {
            no strict 'refs';
            $lookup  = ${ $class . '::LOOKUP'  };
            $missing = ${ $class . '::MISSING' }; 
            $gap     = ${ $class . '::GAP'     };
            use strict;
        }
        bless $self, $class;
        $self->set_lookup(  $lookup  ) if defined $lookup;
        $self->set_missing( $missing ) if defined $missing;
        $self->set_gap(     $gap     ) if defined $gap;
        
		# process further args
		while ( my @args = looks_like_hash @_ ) {

			my $key   = shift @args;
			my $value = shift @args;

			# notify user
			$logger->debug("processing arg '$key'");

			# don't access data structures directly, call mutators
			# in child classes or __PACKAGE__
			my $mutator = $key;
			$mutator =~ s/^-/set_/;

			# backward compat fixes:
			$mutator =~ s/^set_pos$/set_position/;
			$mutator =~ s/^set_matrix$/set_raw/;
			
			# bad argument?
			eval {
				$self->$mutator($value);
			};
			if ( $@ and not ref $@ and $@ =~ m/^Can't locate object method/ ) {
				throw 'UnknownMethod' => "Processing argument '$key' as method '$mutator' failed: $@";
			}
			elsif ( UNIVERSAL::isa( $@, 'Bio::Phylo::Util::Exceptions') ) {
				$@->rethrow;
			}
		}         
        
        return $self;
    }

=back

=head2 MUTATORS

=over

=item set_lookup()

Sets state lookup table.

 Type    : Mutator
 Title   : set_lookup
 Usage   : $obj->set_lookup($hashref);
 Function: Sets the state lookup table.
 Returns : Modified object.
 Args    : Argument must be a hash
           reference that maps allowed
           single character symbols
           (including ambiguity symbols)
           onto the equivalent set of
           non-ambiguous symbols

=cut

    sub set_lookup {
        my ( $self, $lookup ) = @_;
        my $id = $$self;
        
        # we have a value
        if ( defined $lookup ) {
            if ( UNIVERSAL::isa( $lookup, 'HASH' ) ) {
                $lookup{$id} = $lookup;
            }
            else {
            	throw 'BadArgs' => "lookup must be a hash reference";
            }
        }
        
        # no value, so must be a reset
        else {
            $lookup{$id} = $self->get_lookup;
        }
        return $self;
    }

=item set_missing()

Sets missing data symbol.

 Type    : Mutator
 Title   : set_missing
 Usage   : $obj->set_missing('?');
 Function: Sets the symbol for missing data
 Returns : Modified object.
 Args    : Argument must be a single
           character, default is '?'

=cut

    sub set_missing {
        my ( $self, $missing ) = @_;
        my $id = $$self;
        if ( $missing ne $self->get_gap ) {
        	$missing{$id} = $missing;
        }
        else {
        	throw 'BadArgs' => "Missing character '$missing' already in use as gap character";
        }
        return $self;
    }

=item set_gap()

Sets gap symbol.

 Type    : Mutator
 Title   : set_gap
 Usage   : $obj->set_gap('-');
 Function: Sets the symbol for gaps
 Returns : Modified object.
 Args    : Argument must be a single
           character, default is '-'

=cut

    sub set_gap {
        my ( $self, $gap ) = @_;
        if ( not $gap eq $self->get_missing ) {
        	$gap{ $self->get_id } = $gap;
        }
        else {
        	throw 'BadArgs' => "Gap character '$gap' already in use as missing character";
        }
        return $self;
    }

=back

=head2 ACCESSORS

=over

=item get_type()

Gets data type as string.

 Type    : Accessor
 Title   : get_type
 Usage   : my $type = $obj->get_type;
 Function: Returns the object's datatype
 Returns : A string
 Args    : None

=cut

    sub get_type {
        my $type = ref shift;
        $type =~ s/.*:://;
        return $type;
    }

=item get_ids_for_states()

Gets state-to-id mapping

 Type    : Accessor
 Title   : get_ids_for_states
 Usage   : my %ids = %{ $obj->get_ids_for_states };
 Function: Returns the object's datatype
 Returns : A hash reference, keyed on state, with UID values
 Args    : None

=cut
    
    sub get_ids_for_states {
    	my $self = shift;
    	if ( my $lookup = $self->get_lookup ) {
    		my $i = 1;
    		my $ids_for_states = {};
    		my ( @states, @tmp_cats ); 
    		my @tmp = sort { $a->[1] <=> $b->[1] } 
    		           map { [ $_, scalar @{ $lookup->{$_} } ] } 
    		         keys %{ $lookup };
    		for my $state ( @tmp ) {
    			my $count = $state->[1];
    			my $sym   = $state->[0];
    			if ( not $tmp_cats[$count] ) {
    				$tmp_cats[$count] = [];
    			}
    			push @{ $tmp_cats[$count] }, $sym;
    		}
    		for my $cat ( @tmp_cats ) {
    			if ( $cat ) {
    				my @sorted = sort { $a cmp $b } @{ $cat };
    				push @states, @sorted;
    			}
    		}
    		for my $state ( @states ) {
    			my $id = $i++;
    			$ids_for_states->{$state} = $_[0] ? "s${id}" : $id;
    		}
    		return $ids_for_states;
    	}
    	else {
    		return {};
    	}
    }

=item get_symbol_for_states()

Gets ambiguity symbol for a set of states

 Type    : Accessor
 Title   : get_symbol_for_states
 Usage   : my $state = $obj->get_symbol_for_states('A','C');
 Function: Returns the ambiguity symbol for a set of states
 Returns : A symbol (SCALAR)
 Args    : A set of symbols
 Comments: If no symbol exists in the lookup
           table for the given set of states,
           a new - numerical - one is created

=cut

	sub get_symbol_for_states {
		my $self = shift;
		my @syms = @_;
		my $lookup = $self->get_lookup;
		if ( $lookup ) {
			my @lookup_syms = keys %{ $lookup };
			SYM: for my $sym ( @lookup_syms ) {
				my @states = @{ $lookup->{$sym} };
				if ( scalar @syms == scalar @states ) {
					my $seen_all = 0;
					for my $i ( 0 .. $#syms ) {
						my $seen = 0;
						for my $j ( 0 .. $#states ) {
							if ( $syms[$i] eq $states[$j] ) {
								$seen++;
								$seen_all++;
							}
						}
						next SYM if not $seen;
					}
					# found existing symbol
					return $sym if $seen_all == scalar @syms;
				}
			}
			# create new symbol
			my $sym;
			
			if ( $self->get_type !~ /standard/i ) {
				my $sym = 0;
				while ( exists $lookup->{$sym} ) {
					$sym++;
				}
			}
			else {
				LETTER: for my $char ( 'A' .. 'Z' ) {
					if ( not exists $lookup->{$char} ) {
						$sym = $char;
						last LETTER;
					}
				}
			}
			
			$lookup->{$sym} = \@syms;
			$self->set_lookup($lookup);
			return $sym;
		}
		else {
			$logger->info("No lookup table!");
			return;
		}
	}

=item get_lookup()

Gets state lookup table.

 Type    : Accessor
 Title   : get_lookup
 Usage   : my $lookup = $obj->get_lookup;
 Function: Returns the object's lookup hash
 Returns : A hash reference
 Args    : None

=cut

    sub get_lookup {
        my $self = shift;
        my $id = $self->get_id;
        if ( exists $lookup{$id} ) {
            return $lookup{$id};
        }
        else {
           my $class = ref $self;
           my $lookup;
           {
                no strict 'refs';
                $lookup = ${ $class . '::LOOKUP'  };
                use strict;
           }
           $self->set_lookup( $lookup );
           return $lookup;
        }
    }

=item get_missing()

Gets missing data symbol.

 Type    : Accessor
 Title   : get_missing
 Usage   : my $missing = $obj->get_missing;
 Function: Returns the object's missing data symbol
 Returns : A string
 Args    : None

=cut

    sub get_missing {
    	my $self = shift;
        my $missing = $missing{$$self};
        return defined $missing ? $missing : '?';
    }

=item get_gap()

Gets gap symbol.

 Type    : Accessor
 Title   : get_gap
 Usage   : my $gap = $obj->get_gap;
 Function: Returns the object's gap symbol
 Returns : A string
 Args    : None

=cut

    sub get_gap {
    	my $self = shift;    	
        my $gap = $gap{$$self};
        return defined $gap ? $gap : '-';
    }

=back

=head2 TESTS

=over

=item is_valid()

Validates argument.

 Type    : Test
 Title   : is_valid
 Usage   : if ( $obj->is_valid($datum) ) {
              # do something
           }
 Function: Returns true if $datum only contains valid characters
 Returns : BOOLEAN
 Args    : A Bio::Phylo::Matrices::Datum object

=cut

    sub is_valid {
        my $self = shift;        
        my @data;
        for my $arg ( @_ ) {
        	if ( UNIVERSAL::can( $arg, 'get_char') ) {
        		push @data, $arg->get_char;
        	}
        	elsif ( UNIVERSAL::isa( $arg, 'ARRAY') ) {
        		push @data, @{ $arg };
        	}
        	else {
        		if ( length($arg) > 1 ) {
        			push @data, @{ $self->split( $arg ) };
        		}
        		else {
        			push @data, $arg;
        		}
        	}
        }
        return 1 if not @data;
        my $lookup = $self->get_lookup;
        my ( $missing, $gap ) = ( $self->get_missing, $self->get_gap );
        CHAR_CHECK: for my $char ( @data ) {            
            next CHAR_CHECK if not defined $char;
            my $uc = uc $char;
            if ( exists $lookup->{$uc} || ( defined $missing && $uc eq $missing ) || ( defined $gap && $uc eq $gap ) ) {
                next CHAR_CHECK;
            }
            else {
                return 0;
            }
        }
        return 1;
    }

=item is_same()

Compares data type objects.

 Type    : Test
 Title   : is_same
 Usage   : if ( $obj->is_same($obj1) ) {
              # do something
           }
 Function: Returns true if $obj1 contains the same validation rules
 Returns : BOOLEAN
 Args    : A Bio::Phylo::Matrices::Datatype::* object

=cut

    sub is_same {
        my ( $self, $model ) = @_;
        $logger->info("Comparing datatype '$self' to '$model'");
        return 1 if $self->get_id   == $model->get_id;
        return 0 if $self->get_type ne $model->get_type;
        
        # check strings
        for my $prop ( qw(get_type get_missing get_gap) ) {
            my ( $self_prop, $model_prop ) = ( $self->$prop, $model->$prop );
            return 0 if defined $self_prop && defined $model_prop && $self_prop ne $model_prop;
        }
        my ( $s_lookup, $m_lookup ) = ( $self->get_lookup, $model->get_lookup );
    
        # one has lookup, other hasn't
        if ( $s_lookup && ! $m_lookup ) {
            return 0;
        }
    
        # both don't have lookup -> are continuous
        if ( ! $s_lookup && ! $m_lookup ) {
            return 1;
        }
    
        # get keys
        my @s_keys = keys %{ $s_lookup };
        my @m_keys = keys %{ $m_lookup };
    
        # different number of keys
        if ( scalar( @s_keys ) != scalar( @m_keys ) ) {
            return 0;
        }
        
        # compare keys
        for my $key ( @s_keys ) {
            if ( not exists $m_lookup->{$key} ) {
                return 0;
            }
            else {
                # compare values
                my ( %s_vals, %m_vals );
                my ( @s_vals, @m_vals );
                @s_vals = @{ $s_lookup->{$key} };
                @m_vals = @{ $m_lookup->{$key} };
                
                # different number of vals
                if ( scalar( @m_vals ) != scalar( @s_vals ) ) {
                    return 0;
                }
                
                # make hashes to compare on vals
                %s_vals = map { $_ => 1 } @s_vals;
                %m_vals = map { $_ => 1 } @m_vals;                      
                for my $val ( keys %s_vals ) {
                    return 0 if not exists $m_vals{$val};
                }
            }
        }
        return 1;
    }

=back

=head2 UTILITY METHODS

=over

=item split()

Splits argument string of characters following appropriate rules.

 Type    : Utility method
 Title   : split
 Usage   : $obj->split($string)
 Function: Splits $string into characters
 Returns : An array reference of characters
 Args    : A string

=cut

    sub split {
        my ( $self, $string ) = @_;
        my @array = CORE::split /\s*/, $string;
        return \@array;
    }

=item join()

Joins argument array ref of characters following appropriate rules.

 Type    : Utility method
 Title   : join
 Usage   : $obj->join($arrayref)
 Function: Joins $arrayref into a string
 Returns : A string
 Args    : An array reference

=cut

    sub join {
        my ( $self, $array ) = @_;
        return CORE::join '', @{ $array };
    }
    
    sub _cleanup {
        my $self = shift;
        $logger->debug("cleaning up '$self'");
        my $id = $self->get_id;
        for my $field ( @fields ) {
            delete $field->{$id};
        }
    }

=back

=head2 SERIALIZERS

=over

=item to_xml()

Writes data type definitions to xml

 Type    : Serializer
 Title   : to_xml
 Usage   : my $xml = $obj->to_xml
 Function: Writes data type definitions to xml
 Returns : An xml string representation of data type definition
 Args    : None

=cut

	sub to_xml {
		my $self = shift;	
		my $xml = '';
		my $normalized = {};
		$normalized = shift if @_;
		if ( my $lookup = $self->get_lookup ) {
			$xml .= "\n" . $self->get_xml_tag;
			my $id_for_state = $self->get_ids_for_states;
			my @states = sort { $id_for_state->{$a} <=> $id_for_state->{$b} } keys %{ $id_for_state };
			for my $state ( @states ) {
				my $state_id = $id_for_state->{ $state };
				$id_for_state->{ $state } = 's' . $state_id;
			}
			for my $state ( @states ) {
				my $state_id = $id_for_state->{ $state };
				my @mapping = @{ $lookup->{$state} };
				my $symbol = exists $normalized->{$state} ? $normalized->{$state} : $state;
				
				# has ambiguity mappings
				if ( scalar @mapping > 1 ) {
					$xml .= "\n" . sprintf('<state id="%s" symbol="%s">', $state_id, $symbol);
					for my $map ( @mapping ) {
						$xml .= "\n" . sprintf( '<mapping state="%s" mstaxa="uncertainty"/>', $id_for_state->{ $map } );
					}
					$xml .= "\n</state>";
				}
				
				# no ambiguity
				else {
					$xml .= "\n" . sprintf('<state id="%s" symbol="%s"/>', $state_id, $symbol);
				}
			}
			$xml .= "\n</states>";
		}	
		return $xml;	
	}

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo>

This object inherits from L<Bio::Phylo>, so the methods defined
therein are also applicable to L<Bio::Phylo::Matrices::Datatype> objects.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Datatype.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

}

1;