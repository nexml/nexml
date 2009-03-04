# $Id$
package Bio::Phylo::Matrices::TypeSafeData;
use Bio::Phylo::Listable;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw(_MATRIX_ looks_like_hash looks_like_object);
use Bio::Phylo::Matrices::Datatype;
use UNIVERSAL 'isa';
use strict;
use vars '@ISA';
@ISA = qw(Bio::Phylo::Listable);


{
    my $logger = __PACKAGE__->get_logger;
    my %type;
    my $MATRIX_CONSTANT = _MATRIX_;
    
=head1 NAME

Bio::Phylo::Matrices::TypeSafeData - Superclass for objects that contain
character data

=head1 SYNOPSIS

 # No direct usage

=head1 DESCRIPTION

This is a superclass for objects holding character data. Objects that inherit
from this class (typically matrices and datum objects) yield functionality to
handle datatype objects and use them to validate data such as DNA sequences,
continuous data etc.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

TypeSafeData constructor.

 Type    : Constructor
 Title   : new
 Usage   : No direct usage, is called by child class;
 Function: Instantiates a Bio::Phylo::Matrices::TypeSafeData
 Returns : a Bio::Phylo::Matrices::TypeSafeData child class
 Args    : -type        => (data type - required)
           Optional:
           -missing     => (the symbol for missing data)
           -gap         => (the symbol for gaps)
           -lookup      => (a character state lookup hash)
           -type_object => (a datatype object)

=cut    

    sub new {
        # is child class
        my $class = shift;
        
        # process args
        my %args = looks_like_hash @_;
        
        # notify user
        if ( not $args{'-type'} and not $args{'-type_object'} ) {
        	$logger->info("No data type provided, will use 'standard'");
        	unshift @_, '-type', 'standard';
        } 
        # notify user
        $logger->debug("constructor called for '$class'");

        # go up inheritance tree, eventually get an ID
        return $class->SUPER::new( @_ );
    }

=back

=head2 MUTATORS

=over

=item set_type()

Set data type.

 Type    : Mutator
 Title   : set_type
 Usage   : $obj->set_type($type);
 Function: Sets the object's datatype.
 Returns : Modified object.
 Args    : Argument must be a string, one of
           continuous, custom, dna, mixed,
           protein, restriction, rna, standard

=cut

    sub set_type {
        my $self = shift;
        my $arg  = shift;
        my ( $type, @args );
        if ( isa( $arg, 'ARRAY' ) ) {
        	@args = @{ $arg };
        	$type = shift @args;
        }
        else {
        	@args = @_;
        	$type = $arg;
        }
        $logger->info("setting type '$type'");
        my $obj = Bio::Phylo::Matrices::Datatype->new( $type, @args );
        $self->set_type_object( $obj );
        eval { looks_like_object $self, $MATRIX_CONSTANT };
        if ( not $@ ) {
        	for my $row ( @{ $self->get_entities } ) {
        		$row->set_type_object( $obj );
        	}
        }
        else {
        	undef($@);
        }
        return $self;
    }

=item set_missing()

Set missing data symbol.

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
        if ( $self->can('get_matchchar') and $missing eq $self->get_matchchar ) {
        	throw 'BadArgs' => "Missing character '$missing' already in use as match character";
        }
        $logger->info("setting missing '$missing'");
        $self->get_type_object->set_missing( $missing );
        $self->validate;
        return $self;
    }

=item set_gap()

Set gap data symbol.

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
        if ( $self->can('get_matchchar') and $gap eq $self->get_matchchar ) {
            throw 'BadArgs' => "Gap character '$gap' already in use as match character";
        }        
        $logger->info("setting gap '$gap'");
        $self->get_type_object->set_gap( $gap );
        $self->validate;
        return $self;
    }

=item set_lookup()

Set ambiguity lookup table.

 Type    : Mutator
 Title   : set_lookup
 Usage   : $obj->set_gap($hashref);
 Function: Sets the symbol for gaps
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
        $logger->info("setting character state lookup hash");
        $self->get_type_object->set_lookup( $lookup );
        $self->validate;
        return $self;
    }

=item set_type_object()

Set data type object.

 Type    : Mutator
 Title   : set_type_object
 Usage   : $obj->set_gap($obj);
 Function: Sets the datatype object
 Returns : Modified object.
 Args    : Argument must be a subclass
           of Bio::Phylo::Matrices::Datatype

=cut

    sub set_type_object {
        my ( $self, $obj ) = @_;
        $logger->info("setting character type object");
        $type{$$self} = $obj;
        eval {
            $self->validate
        };
        if ( $@ ) {
            undef($@);
            if ( my @char = $self->get_char ) {
            	$self->clear;
            	$logger->warn("Data contents of $self were invalidated by new type object.");
            }
        }
        return $self;
    }

=back

=head2 ACCESSORS

=over

=item get_type()

Get data type.

 Type    : Accessor
 Title   : get_type
 Usage   : my $type = $obj->get_type;
 Function: Returns the object's datatype
 Returns : A string
 Args    : None

=cut

    sub get_type { shift->get_type_object->get_type }

=item get_missing()

Get missing data symbol.

 Type    : Accessor
 Title   : get_missing
 Usage   : my $missing = $obj->get_missing;
 Function: Returns the object's missing data symbol
 Returns : A string
 Args    : None

=cut

    sub get_missing { shift->get_type_object->get_missing }

=item get_gap()

Get gap symbol.

 Type    : Accessor
 Title   : get_gap
 Usage   : my $gap = $obj->get_gap;
 Function: Returns the object's gap symbol
 Returns : A string
 Args    : None

=cut

    sub get_gap { shift->get_type_object->get_gap }

=item get_lookup()

Get ambiguity lookup table.

 Type    : Accessor
 Title   : get_lookup
 Usage   : my $lookup = $obj->get_lookup;
 Function: Returns the object's lookup hash
 Returns : A hash reference
 Args    : None

=cut

    sub get_lookup { shift->get_type_object->get_lookup }

=item get_type_object()

Get data type object.

 Type    : Accessor
 Title   : get_type_object
 Usage   : my $obj = $obj->get_type_object;
 Function: Returns the object's linked datatype object
 Returns : A subclass of Bio::Phylo::Matrices::Datatype
 Args    : None

=cut

    sub get_type_object { $type{ ${ $_[0] } } }

=back

=head2 UTILITY METHODS

=over

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
		
		# we'll create type object during construction
		$subs{'set_type'}    = 0;
		$subs{'set_missing'} = 0;
		$subs{'set_gap'}     = 0;
		$subs{'set_lookup'}  = 0;
		
		# we'll override this, the type object is created from scratch
		$subs{'set_type_object'} = 0;
		
		# this will create type object during construction
		$subs{'new'} = [ 
			'-type'    => $self->get_type,
			'-missing' => $self->get_missing,
			'-gap'     => $self->get_gap,
			'-lookup'  => $self->get_lookup,
		];		
		
		return $self->SUPER::clone(%subs);
	
	} 

=back

=head2 INTERFACE METHODS

=over

=item validate()

Validates the object's contents

 Type    : Interface method
 Title   : validate
 Usage   : $obj->validate
 Function: Validates the object's contents
 Returns : True or throws Bio::Phylo::Util::Exceptions::InvalidData
 Args    : None
 Comments: This is an interface method, i.e. this class doesn't
           implement the method, child classes have to

=cut

    sub validate {
    	throw 'NotImplemented' => 'Not implemented!';
    }
    
    sub _cleanup {
        my $self = shift;
        if ( $self and defined( my $id = $$self ) ) {
	        $logger->debug("cleaning up '$self'");
	        delete $type{ $self->get_id };
        }
    }

}

# podinherit_insert_token

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Listable>

This object inherits from L<Bio::Phylo::Listable>, so the methods defined 
therein are also applicable to L<Bio::Phylo::Matrices::TypeSafeData> objects.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

1;
