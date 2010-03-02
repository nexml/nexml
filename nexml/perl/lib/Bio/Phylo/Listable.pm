# $Id$
package Bio::Phylo::Listable;
use strict;
use vars qw(@ISA);
use Scalar::Util qw(blessed weaken);
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::XMLWritable ();
use Bio::Phylo::Util::CONSTANT qw(:all);
use Bio::Phylo::Factory ();
use UNIVERSAL qw(isa can);

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::Util::XMLWritable);
{

	my $logger = __PACKAGE__->get_logger;
	my $fac    = Bio::Phylo::Factory->new;
	
	my ( $DATUM,  $NODE,  $MATRIX,  $TREE  ) = 
	   ( _DATUM_, _NODE_, _MATRIX_, _TREE_ );

	# $fields array necessary for object destruction
	my @fields = \( 
		my ( 
			%entities, # XXX strong reference
			%index,
			%listeners,
			%sets,
		) 
	);

=head1 NAME

Bio::Phylo::Listable - List of things, super class for many objects

=head1 SYNOPSIS

 No direct usage, parent class. Methods documented here 
 are available for all objects that inherit from it.

=head1 DESCRIPTION

A listable object is an object that contains multiple smaller objects of the
same type. For example: a tree contains nodes, so it's a listable object.

This class contains methods that are useful for all listable objects: Matrices
(i.e. sets of matrix objects), individual Matrix objects, Datum objects (i.e.
character state sequences), Taxa, Forest, Tree and Node objects.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Listable object constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $obj = Bio::Phylo::Listable->new;
 Function: Instantiates a Bio::Phylo::Listable object
 Returns : A Bio::Phylo::Listable object.
 Args    : none

=cut

#	sub new {
#
#		# could be child class
#		my $class = shift;
#
#		# notify user
#		$logger->info("constructor called for '$class'");
#
#		# recurse up inheritance tree, get ID
#		my $self = $class->SUPER::new(@_);
#
#		# local fields would be set here
#
#		return $self;
#	}

=back

=head2 ARRAY METHODS

=over

=item insert()

Pushes an object into its container.

 Type    : Object method
 Title   : insert
 Usage   : $obj->insert($other_obj);
 Function: Pushes an object into its container.
 Returns : A Bio::Phylo::Listable object.
 Args    : A Bio::Phylo::* object.

=cut

	sub insert {
		my ( $self, @obj ) = @_;
		if ( @obj and $self->can_contain( @obj ) ) {
			$logger->info("inserting '@obj' in '$self'");
			push @{ $entities{$$self} }, @obj;
			for my $obj ( @obj ) {
				if ( ref $obj and can( $obj, '_set_container' ) ) {
					$obj->_set_container( $self );
				}
			}
			$self->notify_listeners( 'insert', @obj );
			return $self;
		}
		else {
			throw 'ObjectMismatch' => "Failed insertion: [@obj] in [$self]";
		}
	}

=item insert_at_index()

Inserts argument object in invocant container at argument index.

 Type    : Object method
 Title   : insert_at_index
 Usage   : $obj->insert_at_index($other_obj, $i);
 Function: Inserts $other_obj at index $i in container $obj
 Returns : A Bio::Phylo::Listable object.
 Args    : A Bio::Phylo::* object.

=cut    

	sub insert_at_index {
		my ( $self, $obj, $index ) = @_;
		$logger->debug("inserting '$obj' in '$self' at index $index");
		if ( defined $obj and $self->can_contain($obj) ) {
			$entities{$$self}->[$index] = $obj;
			if ( can( $obj, '_set_container' ) ) {
				$obj->_set_container($self);
			}
			$self->notify_listeners( 'insert_at_index', $obj );			
			return $self;
		}
		else {
			throw 'ObjectMismatch' => 'Failed insertion!';
		}
	}

=item delete()

Deletes argument from invocant object.

 Type    : Object method
 Title   : delete
 Usage   : $obj->delete($other_obj);
 Function: Deletes an object from its container.
 Returns : A Bio::Phylo::Listable object.
 Args    : A Bio::Phylo::* object.
 Note    : Be careful with this method: deleting 
           a node from a tree like this will 
           result in undefined references in its 
           neighbouring nodes. Its children will 
           have their parent reference become 
           undef (instead of pointing to their 
           grandparent, as collapsing a node would 
           do). The same is true for taxon objects 
           that reference datum objects: if the 
           datum object is deleted from a matrix 
           (say), the taxon will now hold undefined 
           references.

=cut

	sub delete {
		my ( $self, $obj ) = @_;
		my $id = $$self;
		if ( $self->can_contain($obj) ) {
			my $occurence_counter = 0;
			if ( my $i = $index{$id} ) {
				for my $j ( 0 .. $i ) {
					if ( $entities{$id}->[$j] == $obj ) {
						$occurence_counter++;
					}
				}
			}
			my @modified = grep { ${ $_ } != $$obj } @{ $entities{$id} };
			$entities{$id} = \@modified;
			$index{$id} -= $occurence_counter;
		}
		else {
			throw 'ObjectMismatch' => "Invocant object cannot contain argument object";
		}
		$self->notify_listeners( 'delete', $obj );		
		return $self;
	}

=item clear()

Empties container object.

 Type    : Object method
 Title   : clear
 Usage   : $obj->clear();
 Function: Clears the container.
 Returns : A Bio::Phylo::Listable object.
 Args    : Note.
 Note    : 

=cut

	sub clear {
		my $self = shift;
		$entities{$$self} = [];
		$self->notify_listeners( 'clear' );
		return $self;
	}

=item cross_reference()

The cross_reference method links node and datum objects to the taxa they apply
to. After crossreferencing a matrix with a taxa object, every datum object has
a reference to a taxon object stored in its C<$datum-E<gt>get_taxon> field, and
every taxon object has a list of references to datum objects stored in its
C<$taxon-E<gt>get_data> field.

 Type    : Generic method
 Title   : cross_reference
 Usage   : $obj->cross_reference($taxa);
 Function: Crossreferences the entities 
           in the invocant with names 
           in $taxa
 Returns : string
 Args    : A Bio::Phylo::Taxa object
 Comments:

=cut

	sub cross_reference {
		my ( $self, $taxa ) = @_;
		my ( $selfref, $taxref ) = ( ref $self, ref $taxa );
		if ( $taxa->can('get_entities') ) {
			my $ents = $self->get_entities;
			if ( $ents && @{$ents} ) {
				foreach ( @{$ents} ) {
					if ( $_->can('get_name') && $_->can('set_taxon') ) {
						my $tax = $taxa->get_entities;
						if ( $tax && @{$tax} ) {
							foreach my $taxon ( @{$tax} ) {
								if ( not $taxon->get_name or not $_->get_name )
								{
									next;
								}
								if ( $taxon->get_name eq $_->get_name ) {
									$_->set_taxon($taxon);
									if ( $_->_type == $DATUM ) {
										$taxon->set_data($_);
									}
									if ( $_->_type == $NODE ) {
										$taxon->set_nodes($_);
									}
								}
							}
						}
					}
					else {
						throw 'ObjectMismatch' => "$selfref can't link to $taxref";
					}
				}
			}
			if ( $self->_type == $TREE ) {
				$self->_get_container->set_taxa($taxa);
			}
			elsif ( $self->_type == $MATRIX ) {
				$self->set_taxa($taxa);
			}
			return $self;
		}
		else {
			throw 'ObjectMismatch' => "$taxref does not contain taxa";
		}
	}

=item get_entities()

Returns a reference to an array of objects contained by the listable object.

 Type    : Generic query
 Title   : get_entities
 Usage   : my @entities = @{ $obj->get_entities };
 Function: Retrieves all entities in the invocant.
 Returns : A reference to a list of Bio::Phylo::* 
           objects.
 Args    : none.

=cut

	sub get_entities { 
		my $self = shift;
		return $entities{$$self} || [];
	}

=item get_index_of()

Returns the index of the argument in the list,
or undef if the list doesn't contain the argument

 Type    : Generic query
 Title   : get_index_of
 Usage   : my $i = $listable->get_index_of($obj)
 Function: Returns the index of the argument in the list,
           or undef if the list doesn't contain the argument
 Returns : An index or undef
 Args    : A contained object

=cut

	sub get_index_of {
		my ( $self, $obj ) = @_;
		my $id = $obj->get_id;
		my $i = 0;
		for my $ent ( @{ $self->get_entities } ) {
			return $i if $ent->get_id == $id;
			$i++;
		}
		return undef;
	}

=item contains()

Tests whether the invocant object contains the argument object.

 Type    : Test
 Title   : contains
 Usage   : if ( $obj->contains( $other_obj ) ) {
               # do something
           }
 Function: Tests whether the invocant object 
           contains the argument object
 Returns : BOOLEAN
 Args    : A Bio::Phylo::* object

=cut

	sub contains {
		my ( $self, $obj ) = @_;
		if ( blessed $obj ) {
			my $id = $obj->get_id;
			for my $ent ( @{ $self->get_entities } ) {
				next     if not $ent;
				return 1 if $ent->get_id == $id;
			}
			return 0;
		}
		else {
			#throw 'BadArgs' => "\"$obj\" is not a blessed object!";
			for my $ent ( @{ $self->get_entities } ) {
				next if not $ent;
				return 1 if $ent eq $obj
			} 
		}
	}

=back

=head2 ITERATOR METHODS

=over

=item first()

Jumps to the first element contained by the listable object.

 Type    : Iterator
 Title   : first
 Usage   : my $first_obj = $obj->first;
 Function: Retrieves the first 
           entity in the invocant.
 Returns : A Bio::Phylo::* object
 Args    : none.

=cut

	sub first {
		my $self = shift;
		my $id   = $$self;
		$index{$id} = 0;
		return $entities{$id}->[0];
	}

=item last()

Jumps to the last element contained by the listable object.

 Type    : Iterator
 Title   : last
 Usage   : my $last_obj = $obj->last;
 Function: Retrieves the last 
           entity in the invocant.
 Returns : A Bio::Phylo::* object
 Args    : none.

=cut

	sub last {
		my $self = shift;
		my $id   = $$self;
		$index{$id} = $#{ $entities{$id} };
		return $entities{$id}->[-1];
	}

=item current()

Returns the current focal element of the listable object.

 Type    : Iterator
 Title   : current
 Usage   : my $current_obj = $obj->current;
 Function: Retrieves the current focal 
           entity in the invocant.
 Returns : A Bio::Phylo::* object
 Args    : none.

=cut

	sub current {
		my $self = shift;
		my $id   = $$self;
		if ( !defined $index{$id} ) {
			$index{$id} = 0;
		}
		return $entities{$id}->[ $index{$id} ];
	}

=item next()

Returns the next focal element of the listable object.

 Type    : Iterator
 Title   : next
 Usage   : my $next_obj = $obj->next;
 Function: Retrieves the next focal 
           entity in the invocant.
 Returns : A Bio::Phylo::* object
 Args    : none.

=cut

	sub next {
		my $self = shift;
		my $id   = $$self;
		if ( !defined $index{$id} ) {
			$index{$id} = 0;
			return $entities{$id}->[ $index{$id} ];
		}
		elsif ( ( $index{$id} + 1 ) <= $#{ $entities{$id} } ) {
			$index{$id}++;
			return $entities{$id}->[ $index{$id} ];
		}
		else {
			return;
		}
	}

=item previous()

Returns the previous element of the listable object.

 Type    : Iterator
 Title   : previous
 Usage   : my $previous_obj = $obj->previous;
 Function: Retrieves the previous 
           focal entity in the invocant.
 Returns : A Bio::Phylo::* object
 Args    : none.

=cut

	sub previous {
		my $self = shift;
		my $id   = $$self;

		# either undef or 0
		if ( !$index{$id} ) {
			return;
		}
		elsif ( 1 <= $index{$id} ) {
			$index{$id}--;
			return $entities{$id}->[ $index{$id} ];
		}
		else {
			return;
		}
	}

=item current_index()

Returns the current internal index of the invocant.

 Type    : Generic query
 Title   : current_index
 Usage   : my $last_index = $obj->current_index;
 Function: Returns the current internal 
           index of the invocant.
 Returns : An integer
 Args    : none.

=cut

	sub current_index { $index{ ${ $_[0] } } || 0 }

=item last_index()

Returns the highest valid index of the invocant.

 Type    : Generic query
 Title   : last_index
 Usage   : my $last_index = $obj->last_index;
 Function: Returns the highest valid 
           index of the invocant.
 Returns : An integer
 Args    : none.

=cut

	sub last_index { $#{ $entities{ ${ $_[0] } } } }

=item get_by_index()

Gets element defined by argument index from invocant container.

 Type    : Query
 Title   : get_by_index
 Usage   : my $contained_obj = $obj->get_by_index($i);
 Function: Retrieves the i'th entity 
           from a listable object.
 Returns : An entity stored by a listable 
           object (or array ref for slices).
 Args    : An index or range. This works 
           the way you dereference any perl
           array including through slices, 
           i.e. $obj->get_by_index(0 .. 10)>
           $obj->get_by_index(0, -1) 
           and so on.
 Comments: Throws if out-of-bounds

=cut

	sub get_by_index {
		my $self  = shift;
		my $id    = $$self;
		my @range = @_;
		if ( scalar @range > 1 ) {
			my @returnvalue;
			eval { @returnvalue = @{ $entities{$id} }[@range] };
			if ($@) {
				throw 'OutOfBounds' => 'index out of bounds';
			}
			return \@returnvalue;
		}
		else {
			my $returnvalue;
			eval { $returnvalue = $entities{$id}->[ $range[0] ] };
			if ($@) {
				throw 'OutOfBounds' => 'index out of bounds';
			}
			return $returnvalue;
		}
	}

=back

=head2 VISITOR METHODS

=over

=item get_by_value()

Gets elements that meet numerical rule from invocant container.

 Type    : Visitor predicate
 Title   : get_by_value
 Usage   : my @objects = @{ $obj->get_by_value(
              -value => $method,
              -ge    => $number
           ) };
 Function: Iterates through all objects 
           contained by $obj and returns 
           those for which the output of 
           $method (e.g. get_tree_length) 
           is less than (-lt), less than 
           or equal to (-le), equal to 
           (-eq), greater than or equal to 
           (-ge), or greater than (-gt) $number.
 Returns : A reference to an array of objects
 Args    : -value => any of the numerical 
                     obj data (e.g. tree length)
           -lt    => less than
           -le    => less than or equals
           -eq    => equals
           -ge    => greater than or equals
           -gt    => greater than

=cut

	sub get_by_value {
		my $self = shift;
		my %o    = looks_like_hash @_;
		my @results;
		for my $e ( @{ $self->get_entities } ) {
			if ( $o{ -eq } ) {
				if (   $e->get( $o{-value} )
					&& $e->get( $o{-value} ) == $o{ -eq } )
				{
					push @results, $e;
				}
			}
			if ( $o{ -le } ) {
				if (   $e->get( $o{-value} )
					&& $e->get( $o{-value} ) <= $o{ -le } )
				{
					push @results, $e;
				}
			}
			if ( $o{ -lt } ) {
				if (   $e->get( $o{-value} )
					&& $e->get( $o{-value} ) < $o{ -lt } )
				{
					push @results, $e;
				}
			}
			if ( $o{ -ge } ) {
				if (   $e->get( $o{-value} )
					&& $e->get( $o{-value} ) >= $o{ -ge } )
				{
					push @results, $e;
				}
			}
			if ( $o{ -gt } ) {
				if (   $e->get( $o{-value} )
					&& $e->get( $o{-value} ) > $o{ -gt } )
				{
					push @results, $e;
				}
			}
		}
		return \@results;
	}

=item get_by_name()

Gets first element that has argument name

 Type    : Visitor predicate
 Title   : get_by_name
 Usage   : my $found = $obj->get_by_name('foo');
 Function: Retrieves the first contained object
           in the current Bio::Phylo::Listable 
           object whose name is 'foo'
 Returns : A Bio::Phylo::* object.
 Args    : A name (string)

=cut

	sub get_by_name {
		my ( $self, $name ) = @_;
		if ( not defined $name or ref $name ) {
			throw 'BadString' => "Can't search on name '$name'";
		}
		for my $obj ( @{ $self->get_entities } ) {
			my $obj_name = $obj->get_name;
			if ( $obj_name and $name eq $obj_name ) {
				return $obj;
			}
#			return $obj if $name eq $obj->get_name;
		}
		return;
	}

=item get_by_regular_expression()

Gets elements that match regular expression from invocant container.

 Type    : Visitor predicate
 Title   : get_by_regular_expression
 Usage   : my @objects = @{ 
               $obj->get_by_regular_expression(
                    -value => $method,
                    -match => $re
            ) };
 Function: Retrieves the data in the 
           current Bio::Phylo::Listable 
           object whose $method output 
           matches $re
 Returns : A list of Bio::Phylo::* objects.
 Args    : -value => any of the string 
                     datum props (e.g. 'get_type')
           -match => a compiled regular 
                     expression (e.g. qr/^[D|R]NA$/)

=cut

	sub get_by_regular_expression {
		my $self = shift;
		my %o    = looks_like_hash @_;
		my @matches;
		for my $e ( @{ $self->get_entities } ) {
			if ( $o{-match} && isa( $o{-match}, 'Regexp' ) ) {
				if (   $e->get( $o{-value} )
					&& $e->get( $o{-value} ) =~ $o{-match} )
				{
					push @matches, $e;
				}
			}
			else {
				throw 'BadArgs' => 'need a regular expression to evaluate';
			}
		}
		return \@matches;
	}

=item visit()

Iterates over objects contained by invocant, executes argument
code reference on each.

 Type    : Visitor predicate
 Title   : visit
 Usage   : $obj->visit( 
               sub{ print $_[0]->get_name, "\n" } 
           );
 Function: Implements visitor pattern 
           using code reference.
 Returns : The invocant, possibly modified.
 Args    : a CODE reference.

=cut

	sub visit {
		my ( $self, $code ) = @_;
		if ( isa( $code, 'CODE' ) ) {
			for ( @{ $self->get_entities } ) {
				$code->($_);
			}
		}
		else {
			throw 'BadArgs' => "\"$code\" is not a CODE reference!";
		}
		return $self;
	}

=back

=head2 TESTS

=over

=item can_contain()

Tests if argument can be inserted in invocant.

 Type    : Test
 Title   : can_contain
 Usage   : &do_something if $listable->can_contain( $obj );
 Function: Tests if $obj can be inserted in $listable
 Returns : BOOL
 Args    : An $obj to test

=cut

	sub can_contain {
		my ( $self, @obj ) = @_;
		$logger->info("checking if '$self' can contain '@obj'");
		for my $obj ( @obj ) {
			my ( $self_type, $obj_container );
			eval {
				$self_type     = $self->_type;
				$obj_container = $obj->_container;
			};			
			if ( $@ or $self_type != $obj_container ) {
				if ( not $@ ) {
					$logger->info(" $self $self_type != $obj $obj_container");
				}
				else {
					$logger->info($@);
				}
				return 0;
			}
		}
		return 1;
	}

=back

=head2 UTILITY METHODS

=over

=item set_listener()

Attaches a listener (code ref) which is executed when contents change.

 Type    : Utility method
 Title   : set_listener
 Usage   : $object->set_listener( sub { my $object = shift; } );
 Function: Attaches a listener (code ref) which is executed when contents change.
 Returns : Invocant.
 Args    : A code reference.
 Comments: When executed, the code reference will receive $object
           (the invocant) as its first argument.

=cut

	sub set_listener {
		my ( $self, $listener ) = @_;
		my $id = $$self;
		if ( not $listeners{$id} ) {
			$listeners{$id} = [];
		}
		if ( isa( $listener, 'CODE' ) ) {
			push @{ $listeners{$id} }, $listener;
		}
		else {
			throw 'BadArgs' => "$listener not a CODE reference";
		}	
	}

=item notify_listeners()

Notifies listeners of changed contents.

 Type    : Utility method
 Title   : notify_listeners
 Usage   : $object->notify_listeners;
 Function: Notifies listeners of changed contents.
 Returns : Invocant.
 Args    : NONE.
 Comments:

=cut

	sub notify_listeners {
		my ( $self, @args ) = @_;
		my $id = $$self;
		if ( $listeners{$id} ) {
			for my $l ( @{ $listeners{$id} } ) {
				$l->( $self, @args );
			}
		}
		return $self;
	}

=item clone()

Clones invocant.

 Type    : Utility method
 Title   : clone
 Usage   : my $clone = $object->clone;
 Function: Creates a copy of the invocant object.
 Returns : A copy of the invocant.
 Args    : NONE.
 Comments: Cloning is currently experimental, use with caution.

=cut

	sub clone {
		my $self = shift;
		$logger->info("cloning $self");
		my %subs = @_;
		
		# some extra logic to copy characters from source to target
		if ( not exists $subs{'insert'} ) {
			$subs{'insert'} = sub {
				my ( $obj, $clone ) = @_;
				for my $ent ( @{ $obj->get_entities } ) {
					my $copy = $ent;
					if ( UNIVERSAL::can( $ent, 'clone' ) ) {
						$copy = $ent->clone;
						$logger->debug(
							"inserting a clone $copy of entity" .
							" $ent in a clone $clone of object $obj"
						);
					}
					else {
						$logger->debug(
							"inserting a raw value '$copy' in" .
							" a clone $clone of object $obj"
						);				
					}
					$clone->insert($copy);
				}
			};	
		}
		
		return $self->SUPER::clone(%subs);
	
	}

=back

=head2 SETS MANAGEMENT

Many Bio::Phylo objects are segmented, i.e. they contain one or more subparts 
of the same type. For example, a matrix contains multiple rows; each row 
contains multiple cells; a tree contains nodes, and so on. (Segmented objects
all inherit from Bio::Phylo::Listable, i.e. the class whose documentation you're
reading here.) In many cases it is useful to be able to define subsets of the 
contents of segmented objects, for example sets of taxon objects inside a taxa 
block. The Bio::Phylo::Listable object allows this through a number of methods 
(add_set, remove_set, add_to_set, remove_from_set etc.). Those methods delegate 
the actual management of the set contents to the L<Bio::Phylo::Set> object. 
Consult the documentation for L<Bio::Phylo::Set> for a code sample.

=over

=item add_set()

 Type    : Mutator
 Title   : add_set
 Usage   : $obj->add_set($set)
 Function: Associates a Bio::Phylo::Set object with the invocant
 Returns : Invocant
 Args    : A Bio::Phylo::Set object

=cut

	# here we create a listener that updates the set
	# object when the associated container changes
	my $create_set_listeners = sub {
		my ( $self, $set ) = @_;
		my $listener = sub {
			my ( $listable, $method, $obj ) = @_;
			if ( $method eq 'delete' ) {
				$listable->remove_from_set( $obj, $set );
			}
			elsif ( $method eq 'clear' ) {
				$set->clear;
			}						
		};
		return $listener;
	};

    sub add_set {
        my ( $self, $set ) = @_;;
        my $listener = $create_set_listeners->($self,$set);
        $self->set_listener($listener);
        my $id = $self->get_id;
        $sets{$id} = {} if not $sets{$id};
        my $setid = $set->get_id;
        $sets{$id}->{$setid} = $set;
        return $self;
    }

=item remove_set()

 Type    : Mutator
 Title   : remove_set
 Usage   : $obj->remove_set($set)
 Function: Removes association between a Bio::Phylo::Set object and the invocant
 Returns : Invocant
 Args    : A Bio::Phylo::Set object

=cut    

    sub remove_set {
        my ( $self, $set ) = @_;
        my $id = $self->get_id;        
        $sets{$id} = {} if not $sets{$id};
        my $setid = $set->get_id;
        delete $sets{$id}->{$setid};
        return $self;        
    }

=item get_sets()

 Type    : Accessor
 Title   : get_sets
 Usage   : my @sets = @{ $obj->get_sets() };
 Function: Retrieves all associated Bio::Phylo::Set objects
 Returns : Invocant
 Args    : None

=cut 

    sub get_sets {
        my $self = shift;
        my $id = $self->get_id;        
        $sets{$id} = {} if not $sets{$id};
        return [ values %{ $sets{$id} } ];
    }

=item is_in_set()

 Type    : Test
 Title   : is_in_set
 Usage   : @do_something if $listable->is_in_set($obj,$set);
 Function: Returns whether or not the first argument is listed in the second argument
 Returns : Boolean
 Args    : $obj - an object that may, or may not be in $set
           $set - the Bio::Phylo::Set object to query
 Notes   : This method makes two assumptions:
           i) the $set object is associated with the invocant,
              i.e. add_set($set) has been called previously
           ii) the $obj object is part of the invocant
           If either assumption is violated a warning message
           is printed.

=cut 

    sub is_in_set {
        my ( $self, $obj, $set ) = @_;
        if ( $sets{ $self->get_id }->{ $set->get_id } ) {
            my $i = $self->get_index_of($obj);
            if ( defined $i ) {
                return $set->get_by_index($i) ? 1 : 0;
            }
            else {
                $logger->warn("Container doesn't contain that object.");
            }            
        }
        else {
            $logger->warn("That set is not associated with this container.");
        }
    }

=item add_to_set()

 Type    : Mutator
 Title   : add_to_set
 Usage   : $listable->add_to_set($obj,$set);
 Function: Adds first argument to the second argument
 Returns : Invocant
 Args    : $obj - an object to add to $set
           $set - the Bio::Phylo::Set object to add to
 Notes   : this method assumes that $obj is already 
           part of the invocant. If that assumption is
           violated a warning message is printed.

=cut 

    sub add_to_set {
        my ( $self, $obj, $set ) = @_;
        my $id = $self->get_id;
        $sets{$id} = {} if not $sets{$id};        
        my $i = $self->get_index_of($obj);
        if ( defined $i ) {
            $set->insert_at_index( 1 => $i );
            my $set_id = $set->get_id;
            if ( not exists $sets{$id}->{$set_id} ) {
            	my $listener = $create_set_listeners->($self,$set);
            	$self->set_listener($listener);
            }
            $sets{$id}->{$set_id} = $set;
        }
        else {
            $logger->warn("Container doesn't contain the object you're adding to the set.");
        }
        return $self;
    }

=item remove_from_set()

 Type    : Mutator
 Title   : remove_from_set
 Usage   : $listable->remove_from_set($obj,$set);
 Function: Removes first argument from the second argument
 Returns : Invocant
 Args    : $obj - an object to remove from $set
           $set - the Bio::Phylo::Set object to remove from
 Notes   : this method assumes that $obj is already 
           part of the invocant. If that assumption is
           violated a warning message is printed.

=cut

    sub remove_from_set {
        my ( $self, $obj, $set ) = @_;
        my $id = $self->get_id;
        $sets{$id} = {} if not $sets{$id};        
        my $i = $self->get_index_of($obj);
        if ( defined $i ) {
            $set->insert_at_index( $i => 0 );
            $sets{$id}->{ $set->get_id } = $set;
        }   
        else {
        	$logger->warn("Container doesn't contain the object you're adding to the set.");
        }
        return $self;
    }

=begin comment

 Type    : Internal method
 Title   : _cleanup
 Usage   : $listable->_cleanup;
 Function: Called during object destruction, for cleanup of instance data
 Returns : 
 Args    :

=end comment

=cut

{
	no warnings 'recursion';
	sub _cleanup {
		my $self = shift;
		my $id = $$self;
		for my $field (@fields) {
			delete $field->{$id};
		}
	}
}

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=head2 Objects inheriting from Bio::Phylo::Listable

=over

=item L<Bio::Phylo::Forest>

Iterate over a set of trees.

=item L<Bio::Phylo::Forest::Tree>

Iterate over nodes in a tree.

=item L<Bio::Phylo::Forest::Node>

Iterate of children of a node.

=item L<Bio::Phylo::Matrices>

Iterate over a set of matrices.

=item L<Bio::Phylo::Matrices::Matrix>

Iterate over the datum objects in a matrix.

=item L<Bio::Phylo::Matrices::Datum>

Iterate over the characters in a datum.

=item L<Bio::Phylo::Taxa>

Iterate over a set of taxa.

=back

=head2 Superclasses

=over

=item L<Bio::Phylo::Util::XMLWritable>

This object inherits from L<Bio::Phylo::Util::XMLWritable>, so methods
defined there are also applicable here.

=back

=head1 REVISION

 $Id$

=cut

}
1;
