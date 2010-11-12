# $Id$
package Bio::Phylo;
use strict;

# we don't use 'our', for 5.005 compatibility
use vars qw($VERSION $COMPAT $logger);

# Because we use a roll-your-own looks_like_number from
# Bio::Phylo::Util::CONSTANT, here we don't have to worry
# about older core S::U versions that don't have it...
use Scalar::Util qw(weaken blessed);

#... instead, Bio::Phylo::Util::CONSTANT can worry about it
# in one location, perhaps using the S::U version, or a drop-in
use Bio::Phylo::Util::CONSTANT qw(
    looks_like_number
    looks_like_hash
    looks_like_instance
);
use Bio::Phylo::Util::IDPool;             # creates unique object IDs
use Bio::Phylo::Util::Exceptions 'throw'; # defines exception classes and throws
use Bio::Phylo::Util::Logger;             # for logging, like log4perl/log4j 

BEGIN {
    $logger = Bio::Phylo::Util::Logger->new;
}

# mediates one-to-many relationships between taxon and nodes, 
# taxon and sequences, taxa and forests, taxa and matrices.
# Read up on the Mediator design pattern to learn how this works.
require Bio::Phylo::Mediators::TaxaMediator;  

# Include the revision number from subversion in $VERSION
my $rev = '$Id$';
$rev =~ s/^[^\d]+(\d+)\b.*$/$1/;
$VERSION = "0.29";
$VERSION .= "_$rev";
{
    my $taxamediator = 'Bio::Phylo::Mediators::TaxaMediator';
    sub import {
        my $class = shift;
        if (@_) {
            my %opt = looks_like_hash @_;
	    while ( my ( $key, $value ) = each %opt ) {
		if ( $key =~ qr/^VERBOSE$/i ) {
		    $logger->VERBOSE( '-level' => $value, -class => $class );
		}
		elsif ( $key =~ qr/^COMPAT$/i ) {
		    $COMPAT = ucfirst( lc($value) );
		}
		else {
		    throw 'BadArgs' => "'$key' is not a valid argument for import";
		}
	    }
        }
        return 1;
    }

    # the following hashes are used to hold state of inside-out objects. For 
    # example, $obj->set_name("name") is implemented as $name{ $obj->get_id } 
    # = $name. To avoid memory leaks (and subtle bugs, should a new object by 
    # the same id appear (though that shouldn't happen)), the hash slots 
    # occupied by $obj->get_id need to be reclaimed in the destructor. This 
    # is done by recursively calling the $obj->_cleanup methods in all of $obj's 
    # superclasses. To make that method easier to write, we create an  array 
    # with the local inside-out hashes here, so that we can just iterate over 
    # them anonymously during destruction cleanup. Other classes do something 
    # like this as well.
    my @fields = \( 
	    my ( 
	    	%name, 
	    	%desc, 
	    	%score, 
	    	%generic, 
	    	%cache, 
	    	%container, # XXX weak reference
	    	%objects # XXX strong reference
    	) 
    );

=head1 NAME

Bio::Phylo - Phylogenetic analysis using perl

=head1 SYNOPSIS

 # Actually, you would almost never use this module directly. This is 
 # the base class for other modules.
 use Bio::Phylo;
 
 # sets global verbosity to 'error'
 Bio::Phylo->VERBOSE( -level => Bio::Phylo::Util::Logger::ERROR );
 
 # sets verbosity for forest ojects to 'debug'
 Bio::Phylo->VERBOSE( 
 	-level => Bio::Phylo::Util::Logger::DEBUG, 
 	-class => 'Bio::Phylo::Forest' 
 );
 
 # prints version, including SVN revision number
 print Bio::Phylo->VERSION;
 
 # prints suggested citation
 print Bio::Phylo->CITATION;

=head1 DESCRIPTION

This is the base class for the Bio::Phylo package for phylogenetic analysis using 
object-oriented perl5. In this file, methods are defined that are performed by other 
objects in the Bio::Phylo release that inherit from this base class (which you normally
wouldn't use directly).

For general information on how to use Bio::Phylo, consult the manual
(L<Bio::Phylo::Manual>).

If you come here because you are trying to debug a problem you run into in
using Bio::Phylo, you may be interested in the "exceptions" system as discussed
in L<Bio::Phylo::Util::Exceptions>. In addition, you may find the logging system
in L<Bio::Phylo::Util::Logger> of use to localize problems.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

The Bio::Phylo root constructor is rarely used directly. Rather, many other 
objects in Bio::Phylo internally go up the inheritance tree to this constructor. 
The arguments shown here can therefore also be passed to any of the child 
classes' constructors, which will pass them on up the inheritance tree. Generally, 
constructors in Bio::Phylo subclasses can process as arguments all methods that 
have set_* in their names. The arguments are named for the methods, but "set_" 
has been replaced with a dash "-", e.g. the method "set_name" becomes the 
argument "-name" in the constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $phylo = Bio::Phylo->new;
 Function: Instantiates Bio::Phylo object
 Returns : a Bio::Phylo object 
 Args    : Optional, any number of setters. For example,
 		   Bio::Phylo->new( -name => $name )
 		   will call set_name( $name ) internally

=cut

    sub new {

        # $class could be a child class, called from $class->SUPER::new(@_)
        # or an object, e.g. $node->new(%args) in which case we create a new
        # object that's bless into the same class as the invocant. No, that's
        # not the same thing as a clone.
        my $class = shift;
        if ( my $reference = ref $class ) {
        	$class = $reference;
        }

        # notify user
        $logger->info("constructor called for '$class'");

        # happens only and exactly once because this
	    # root class is visited from every constructor
        my $self = Bio::Phylo::Util::IDPool->_initialize();

        # bless in child class, not __PACKAGE__
        bless $self, $class;   
        
        # register for get_obj_by_id
        $objects{$$self} = $self;
        weaken( $objects{$$self} );

        # processing arguments
        if ( @_ and @_ = looks_like_hash @_ ) {

	    # notify user
	    $logger->debug("going to process constructor args");

	    # process all arguments
	    ARG: while (@_) {
		my $key   = shift @_;
		my $value = shift @_;
		
		# this is a bioperl arg, meant to set
		# verbosity at a per class basis. In
		# bioperl, the $verbose argument is
		# subsequently carried around in that
		# class, here we delegate that to the
		# logger, which has roughly the same 
		# effect.
		if ( $key eq '-verbose' ) {
			$logger->VERBOSE( 
				'-level' => $value,
				'-class' => $class,
			);
			next ARG;
		}

		# notify user
		$logger->debug("processing arg '$key'");

		# don't access data structures directly, call mutators
		# in child classes or __PACKAGE__
		my $mutator = $key;
		$mutator =~ s/^-/set_/;

		# backward compat fixes:
		$mutator =~ s/^set_pos$/set_position/;
		$mutator =~ s/^set_matrix$/set_raw/;
		eval {
			$self->$mutator($value);
		};
		if ( $@ ) {
		    if ( blessed $@ and $@->can('rethrow') ) {
			$@->rethrow;
		    }
		    elsif ( not ref($@) and $@ =~ /^Can't locate object method / ) {
			throw 'BadArgs' => "The named argument '${key}' cannot be passed to the constructor";
		    }
		    else {
			throw 'Generic' => $@;
		    }
		}
	    }
        }

        # register with mediator
		# TODO this is irrelevant for some child classes,
		# so should be re-factored into somewhere nearer the
		# tips of the inheritance tree.
        $taxamediator->register($self);
        return $self;
    }

=back

=head2 MUTATORS

=over

=item set_name()

Sets invocant name.

 Type    : Mutator
 Title   : set_name
 Usage   : $obj->set_name($name);
 Function: Assigns an object's name.
 Returns : Modified object.
 Args    : Argument must be a string. Ensure that this string is safe to use for
           whatever output format you want to use (this differs between xml and
           nexus, for example).

=cut

    sub set_name {
        my ( $self, $name ) = @_;
		if ( $name ) {
	        # strip spaces
	        $name =~ s/^\s*(.*?)\s*$/$1/;
	
	        # check for bad characters
	        if ( $name =~ m/(?:;|,|:|\(|\)|&|<|>\s)/ ) {
	        	$logger->info("the name '$name' might be invalid for some data formats");
	
#	            # had bad characters, but in quotes
#	            if ( $name =~ m/^(['"])/ && $name =~ m/$1$/ ) {
#	                $logger->info("$name had bad characters, but was quoted");
#	            }
#	
#	            # had unquoted bad characters
#	            else {
#	            	throw 'BadString' => "$self '$name' has unquoted bad characters";
#	            }
	        }
	
	        # notify user
	        $logger->info("setting name '$name'");
		}
        $name{$$self} = $name;
        return $self;
    }

=item set_desc()

Sets invocant description.

 Type    : Mutator
 Title   : set_desc
 Usage   : $obj->set_desc($desc);
 Function: Assigns an object's description.
 Returns : Modified object.
 Args    : Argument must be a string.

=cut

    sub set_desc {
        my ( $self, $desc ) = @_;

        # notify user
        $logger->info("setting description '$desc'");
        $desc{$$self} = $desc;
        return $self;
    }

=item set_score()

Sets invocant score.

 Type    : Mutator
 Title   : set_score
 Usage   : $obj->set_score($score);
 Function: Assigns an object's numerical score.
 Returns : Modified object.
 Args    : Argument must be any of
           perl's number formats, or undefined
           to reset score.

=cut

    sub set_score {
        my ( $self, $score ) = @_;

        # $score must be a number (or undefined)
        if ( defined $score ) {
            if ( !looks_like_number($score) ) {
            	throw 'BadNumber' => "score \"$score\" is a bad number";
            }

            # notify user
            $logger->info("setting score '$score'");
        }
        else {
            $logger->info("unsetting score");
        }

        # this resets the score if $score was undefined
        $score{$$self} = $score;
        return $self;
    }

=item set_generic()

Sets generic key/value pair(s).

 Type    : Mutator
 Title   : set_generic
 Usage   : $obj->set_generic( %generic );
 Function: Assigns generic key/value pairs to the invocant.
 Returns : Modified object.
 Args    : Valid arguments constitute:

           * key/value pairs, for example:
             $obj->set_generic( '-lnl' => 0.87565 );

           * or a hash ref, for example:
             $obj->set_generic( { '-lnl' => 0.87565 } );

           * or nothing, to reset the stored hash, e.g.
                $obj->set_generic( );

=cut

    sub set_generic {
        my $self = shift;

        # retrieve id just once, don't call $self->get_id in loops, inefficient
        my $id = $$self;

        # this initializes the hash if it didn't exist yet, or resets it if no args
        if ( !defined $generic{$id} || !@_ ) {
            $generic{$id} = {};
        }

        # have args
        if (@_) {
            my %args;

            # have a single arg, a hash ref
            if ( scalar @_ == 1 && looks_like_instance( $_[0], 'HASH' ) ) {
                %args = %{ $_[0] };
            }

            # multiple args, hopefully even size key/value pairs
            else {
                %args = looks_like_hash @_;
            }

	    # notify user
	    $logger->info("setting generic key/value pairs %{args}");

	    # fill up the hash
	    foreach my $key ( keys %args ) {
		$generic{$id}->{$key} = $args{$key};
	    }
        }
        return $self;
    }

=back

=head2 ACCESSORS

=over

=item get_name()

Gets invocant's name.

 Type    : Accessor
 Title   : get_name
 Usage   : my $name = $obj->get_name;
 Function: Returns the object's name.
 Returns : A string
 Args    : None

=cut

    sub get_name {
        my $self = shift;
        return $name{$$self};
    }

=item get_nexus_name()

Gets invocant's name, modified to be safely used in nexus files

 Type    : Accessor
 Title   : get_nexus_name
 Usage   : my $name = $obj->get_nexus_name;
 Function: Returns the object's name.
 Returns : A string
 Args    : None

=cut

    sub get_nexus_name {
        my $self = shift;
        my $name = $self->get_internal_name;
        $name =~ s/\s+/_/g;
        return $name;    
    }

=item get_internal_name()

Gets invocant's 'fallback' name (possibly autogenerated).

 Type    : Accessor
 Title   : get_internal_name
 Usage   : my $name = $obj->get_internal_name;
 Function: Returns the object's name (if none was set, the name
           is a combination of the $obj's class and its UID).
 Returns : A string
 Args    : None

=cut

    sub get_internal_name {
        my $self = shift;
        if ( my $name = $self->get_name ) {
            return $name;
        }
        else {
            my $internal_name = ref $self;
            $internal_name =~ s/.*:://;
            $internal_name .= $self->get_id;
            return $internal_name;
        }
    }

=item get_desc()

Gets invocant description.

 Type    : Accessor
 Title   : get_desc
 Usage   : my $desc = $obj->get_desc;
 Function: Returns the object's description (if any).
 Returns : A string
 Args    : None

=cut

    sub get_desc {
        my $self = shift;
        $logger->debug("getting description");
        return $desc{$$self};
    }

=item get_score()

Gets invocant's score.

 Type    : Accessor
 Title   : get_score
 Usage   : my $score = $obj->get_score;
 Function: Returns the object's numerical score (if any).
 Returns : A number
 Args    : None

=cut

    sub get_score {
        my $self = shift;
        $logger->debug("getting score");
        return $score{$$self};
    }

=item get_generic()

Gets generic hashref or hash value(s).

 Type    : Accessor
 Title   : get_generic
 Usage   : my $value = $obj->get_generic($key);
           or
           my %hash = %{ $obj->get_generic() };
 Function: Returns the object's generic data. If an
           argument is used, it is considered a key
           for which the associated value is returned.
           Without arguments, a reference to the whole
           hash is returned.
 Returns : A string or hash reference.
 Args    : None

=cut

    sub get_generic {
        my ( $self, $key ) = @_;

        # retrieve just once
        my $id = $$self;

        # might not even have a generic hash yet, make one on-the-fly
        if ( not defined $generic{$id} ) {
            $generic{$id} = {};
        }

        # have an argument
        if ( defined $key ) {

            # notify user
            $logger->debug("getting value for key '$key'");
            return $generic{$id}->{$key};
        }

        # no argument, wants whole hash
        else {

            # notify user
            $logger->debug("retrieving generic hash");
            return $generic{$id};
        }
    }

=item get_id()

Gets invocant's UID.

 Type    : Accessor
 Title   : get_id
 Usage   : my $id = $obj->get_id;
 Function: Returns the object's unique ID
 Returns : INT
 Args    : None

=cut

    sub get_id {
	my $self = shift;
	return $$self;
    }

=item get_logger()

Gets a logger object.

 Type    : Accessor
 Title   : get_logger
 Usage   : my $logger = $obj->get_logger;
 Function: Returns a Bio::Phylo::Util::Logger object
 Returns : Bio::Phylo::Util::Logger
 Args    : None

=cut

	sub get_logger { $logger }

=back

=head2 PACKAGE METHODS

=over

=item get()

Attempts to execute argument string as method on invocant.

 Type    : Accessor
 Title   : get
 Usage   : my $treename = $tree->get('get_name');
 Function: Alternative syntax for safely accessing
           any of the object data; useful for
           interpolating runtime $vars.
 Returns : (context dependent)
 Args    : a SCALAR variable, e.g. $var = 'get_name';

=cut

    sub get {
        my ( $self, $var ) = @_;
        if ( $self->can($var) ) {

            # notify user
            $logger->debug("retrieving return value for method '$var'");
            return $self->$var;
        }
        else {
            my $ref = ref $self;
            throw 'UnknownMethod' => "sorry, a '$ref' can't '$var'";
        }
    }

=item get_obj_by_id()

Attempts to fetch an in-memory object by its UID

 Type    : Accessor
 Title   : get_obj_by_id
 Usage   : my $obj = Bio::Phylo->get_obj_by_id($uid);
 Function: Fetches an object from the IDPool cache
 Returns : A Bio::Phylo object 
 Args    : A unique id

=cut

    sub get_obj_by_id {
	my ( $class, $id ) = @_;
	return $objects{$id};
    }

=item to_json()

Serializes object to JSON string

 Type    : Serializer
 Title   : to_json()
 Usage   : print $obj->to_json();
 Function: Serializes object to JSON string
 Returns : String 
 Args    : None
 Comments:

=cut

    sub to_json { 
		my $self = shift;
    	eval { require XML::XML2JSON };
    	if ( $@ ) {
    		throw 'ExtensionError' => "Can't load XML::XML2JSON - $@";    		
    	}
		return XML::XML2JSON->new->convert($self->to_xml);
    }

=item to_string()

Serializes object to general purpose string

 Type    : Serializer
 Title   : to_string()
 Usage   : print $obj->to_string();
 Function: Serializes object to general purpose string
 Returns : String 
 Args    : None
 Comments: This is YAML

=cut

	sub to_string {
		my $self = shift;
		my $class         = ref $self;
		my $id            = $self->get_id;
		my $internal_name = $self->get_internal_name;
		my $name          = $self->get_name;
		my $score         = $self->get_score;
		my $desc          = $self->get_desc;
return <<"SERIALIZED_OBJECT";
class: $class
id: $id
internal_name: $internal_name
name: $name
score: $score
desc: $desc
SERIALIZED_OBJECT
	}

=item clone()

Clones invocant.

 Type    : Utility method
 Title   : clone
 Usage   : my $clone = $object->clone;
 Function: Creates a copy of the invocant object.
 Returns : A copy of the invocant.
 Args    : None.
 Comments: Cloning is currently experimental, use with caution.

=cut

    # TODO this needs overrides in a number of subclasses,
    # in particular in Bio::Phylo::Taxa and Bio::Phylo::Taxa::Taxon
    # classes because of the asymmetry between set_forest/get_forests,
    # set_node/get_nodes etc.
    sub clone {
		my ( $self, %subs ) = @_;

		# may not work yet! warn user
		$logger->info("cloning is experimental, use with caution");

		# get inheritance tree
		my ( $class, $isa, $seen ) = ( ref($self), [], {} );
		_recurse_isa( $class, $isa, $seen );

		# walk symbol table, get symmetrical set_foo/get_foo pairs
		my %methods;
		for my $package ( $class, @{$isa} ) {
	    	my %symtable;
		    eval "\%symtable = \%${package}::"; 
		  SETTER: for my $setter ( keys %symtable ) {
				next SETTER if $setter !~ m/^set_/;
				my $getter = $setter;
				$getter =~ s/^s/g/;
				next SETTER if not exists $symtable{$getter};

				# have a symmetrical set_foo/get_foo pair, check
				# if they're code (not variables, for example)
				my $get_ref = $class->can($getter);
				my $set_ref = $class->can($setter);
				if ( looks_like_instance( $get_ref, 'CODE' ) and looks_like_instance( $set_ref, 'CODE' ) ) {
				    $methods{$getter} = $setter;
				}
	    	}
		}

		# instantiate the clone
		my @new;
		if ( $subs{'new'} ) {
			@new = @{ $subs{'new'} };
			delete $subs{'new'};			
		}
		my $clone = $class->new(@new);
		
		# populate the clone		
		for my $getter ( keys %methods ) {	    	
	    	my $setter = $methods{$getter};
	    	if ( exists $subs{$setter} ) {
	    		$logger->info("method $setter for $clone overridden");
	    		if ( looks_like_instance( $subs{$setter}, 'CODE' ) ) {
	    			$subs{$setter}->( $self, $clone );
	    		}
	    		delete $subs{$setter};
	    	}
	    	else {
				eval {
					$logger->info("copying $getter => $setter");
					my $value = $self->$getter;
					if ( defined $value ) {
						$clone->$setter($value);
					}
				};
				if ($@) {
					$logger->warn("failed copy of $getter => $setter: \n$@");
				}
	    	}
		}

		# execute additional code refs
		$_->( $self, $clone ) for ( grep { looks_like_instance( $_, 'CODE' ) } values %subs );
		return $clone;
    }

=item VERBOSE()

Getter and setter for the verbosity level. Refer to L<Bio::Phylo::Util::Logger> for more
info on available verbosity levels.

 Type    : Accessor
 Title   : VERBOSE()
 Usage   : Bio::Phylo->VERBOSE( -level => $level )
 Function: Sets/gets verbose level
 Returns : Verbose level
 Args    : -level => $level
 Comments:

=cut

    # Verbosity is mostly handled by the logger, actually. This method
    # is just here for backward compatibility (and ease of use). 
    # TODO have a facility to turn log levels (warn/error/fatal) into 
    # throws
    sub VERBOSE {
        my $class = shift;
        if (@_) {
            my %opt = looks_like_hash @_;
            $logger->VERBOSE(%opt);

            # notify user
            $logger->info("Changed verbosity level to '$opt{-level}'");
        }
        return $Bio::Phylo::Util::Logger::VERBOSE;
    }

=item CITATION()

Returns suggested citation.

 Type    : Accessor
 Title   : CITATION
 Usage   : $phylo->CITATION;
 Function: Returns suggested citation.
 Returns : Returns suggested citation.
 Args    : None
 Comments:

=cut

    # TODO one day this will actually point to something
    sub CITATION {
        my $self    = shift;
        my $name    = __PACKAGE__;
        my $version = __PACKAGE__->VERSION;
        my $string  = qq{Rutger A. Vos, 2005-2010. $name: };
        $string .= qq{Phylogenetic analysis using Perl, version $version};
        return $string;
    }

=item VERSION()

Gets version number (including revision number).

 Type    : Accessor
 Title   : VERSION
 Usage   : $phylo->VERSION;
 Function: Returns version number
           (including SVN revision number).
 Alias   :
 Returns : SCALAR
 Args    : NONE
 Comments:

=cut

    sub VERSION { $VERSION }

=begin comment

Invocant destructor.

 Type    : Destructor
 Title   : DESTROY
 Usage   : $phylo->DESTROY
 Function: Destroys Phylo object
 Alias   :
 Returns : TRUE
 Args    : none
 Comments: You don't really need this,
           it is called automatically when
           the object goes out of scope.

=end comment

=cut

{
    no warnings 'recursion';
    sub DESTROY {
        my $self = shift;

		# delete from get_obj_by_id
		my $id;
		if ( defined( $id = $self->get_id ) ) {
			delete $objects{$id};
		}

        # notify user
        $logger->debug("destructor called for '$self'"); # XXX

        # build full @ISA from child to here
        my ( $class, $isa, $seen ) = ( ref($self), [], {} );
        _recurse_isa( $class, $isa, $seen );

        # call *all* _cleanup methods, wouldn't work if simply SUPER::_cleanup
        # given multiple inheritance
        $logger->debug("going to clean up '$self'"); # XXX
        {
            no strict 'refs'; 
            for my $SUPER ( @{$isa} ) {
            	if ( $SUPER->can('_cleanup') ) {
	                my $cleanup = "${SUPER}::_cleanup";
	                $self->$cleanup;
            	}
            }
            use strict;
        }
        $logger->debug("done cleaning up '$self'"); # XXX

        # cleanup from mediator
        $taxamediator->unregister($self);

        # done cleaning up, id can be reclaimed
        Bio::Phylo::Util::IDPool->_reclaim($self);
    }
}

    # starting from $class, push all superclasses (+$class) into @$isa, 
    # %$seen is just a helper to avoid getting stuck in cycles
    sub _recurse_isa {
        my ( $class, $isa, $seen ) = @_;
        if ( not $seen->{$class} ) {
            $seen->{$class} = 1;
            push @{$isa}, $class;
            my @isa;
            {
                no strict 'refs'; 
                @isa = @{"${class}::ISA"};
                use strict;
            }
            _recurse_isa( $_, $isa, $seen ) for @isa;
        }
    }

    # child classes probably should have a method like this,
    # if their objects hold internal state anyway (b/c they'll
    # be inside-out objects).
    sub _cleanup {
        my $self = shift;
        $logger->debug("cleaning up '$self'"); # XXX
        my $id = $self->get_id;

        # cleanup local fields
        if ( defined $id ) {
	        for my $field (@fields) {
	            delete $field->{$id};
	        }
        }
    }

=begin comment

 Type    : Internal method
 Title   : _get_container
 Usage   : $phylo->_get_container;
 Function: Retrieves the object that contains the invocant (e.g. for a node,
           returns the tree it is in).
 Returns : Bio::Phylo::* object
 Args    : None

=end comment

=cut

    # this is the converse of $listable->get_entities, i.e.
    # every entity in a listable object holds a reference
    # to its container. We actually use this surprisingly 
    # rarely, and because I read somewhere (heh) it's bad
    # to have the objects of a has-a relationship fiddle with
    # their container we hide this method from abuse. Then
    # again, sometimes it's handy ;-)
    sub _get_container {
        my $self = shift;
        return $container{ $$self };
    }

=begin comment

 Type    : Internal method
 Title   : _set_container
 Usage   : $phylo->_set_container($obj);
 Function: Creates a reference from the invocant to the object that contains
           it (e.g. for a node, creates a reference to the tree it is in).
 Returns : Bio::Phylo::* object
 Args    : A Bio::Phylo::Listable object

=end comment

=cut

    sub _set_container {
        my ( $self, $container ) = @_;
        my $id = $$self;
        if ( blessed $container ) {
            if ( $container->can('can_contain') ) {
                if ( $container->can_contain( $self ) ) {
                    if ( $container->contains($self) ) {
                        $container{$id} = $container;
                        weaken( $container{$id} );
                        return $self;
                    }
                    else {
                    	throw 'ObjectMismatch' => "'$self' not in '$container'";
                    }
                }
                else {
                	throw 'ObjectMismatch' => "'$container' cannot contain '$self'";
                }
            }
            else {
            	throw 'ObjectMismatch' => "Invalid objects";
            }
        }
        else {
        	throw 'BadArgs' => "Argument not an object";
        }
    }

=back

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=head1 REVISION

 $Id$

=cut

}

1;
