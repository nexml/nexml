package Bio::Phylo::PhyloWS;
use strict;
use Bio::Phylo ();
use Bio::Phylo::Util::CONSTANT 'looks_like_hash';
use Bio::Phylo::Util::Exceptions 'throw';
use UNIVERSAL 'isa';

use vars qw(@ISA %MIMETYPE);
@ISA=qw(Bio::Phylo);

eval { require URI::URL };
if ( $@ ) {
    throw 'ExtensionError' => "Error loading the URI::URL extension: $@";
}

%MIMETYPE = (
    'nexml'  => 'application/xml',
    'yaml'   => 'application/x-yaml',    
    'rdf'    => 'application/rdf+xml',    
    'nexus'  => 'text/plain',
    'json'   => 'text/javascript',
    'newick' => 'text/plain',
);

{            
    my @fields = \( my ( %uri ) );

=head1 NAME

Bio::Phylo::PhyloWS - Base class for phylogenetic web services

=head1 SYNOPSIS

 # no direct usage, used by child classes

=head1 DESCRIPTION

This is the base class for clients and service that implement the PhyloWS
(L<http://evoinfo.nescent.org/PhyloWS>) recommendations. This base class
isn't used directly, it contains useful methods that are inherited by
its children.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

The Bio::Phylo::PhyloWS constructor is rarely used directly. Rather, many other 
objects in Bio::Phylo::PhyloWS internally go up the inheritance tree to this 
constructor. The arguments shown here can therefore also be passed to any of 
the child classes' constructors, which will pass them on up the inheritance tree. 
Generally, constructors in Bio::Phylo::PhyloWS subclasses can process as arguments 
all methods that have set_* in their names. The arguments are named for the 
methods, but "set_" has been replaced with a dash "-", e.g. the method "set_name" 
becomes the argument "-name" in the constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $phylows = Bio::Phylo::PhyloWS->new( -url => $url );
 Function: Instantiates Bio::Phylo::PhyloWS object
 Returns : a Bio::Phylo::PhyloWS object 
 Args    : Required: -url => $url
           Optional: any number of setters. For example,
 		   Bio::Phylo::PhyloWS->new( -name => $name )
 		   will call set_name( $name ) internally

=cut

	sub new {

		# could be child class
		my $class = shift;

		# go up inheritance tree, eventually get an ID
		my $self = $class->SUPER::new(@_);
		
		# mandatory!
		if ( not $self->get_url ) {
            throw 'BadArgs' => 'Need -url => $url args';
		}
        
		return $self;
	} 

=back

=head2 MUTATORS

=over

=item set_url()

Sets invocant url.

 Type    : Mutator
 Title   : set_url
 Usage   : $obj->set_url($url);
 Function: Assigns an object's url.
 Returns : Modified object.
 Args    : Argument must be a string.

=cut

	sub set_url {
	    my ( $self, $url ) = @_;
	    $uri{ $self->get_id } = $url;
	    return $self;
	}

=back

=head2 ACCESSORS

=over

=item get_url()

Gets invocant's url.

 Type    : Accessor
 Title   : get_url
 Usage   : my $url = $obj->get_url;
 Function: Returns the object's url.
 Returns : A string
 Args    : None

=cut

	my $build_query_string = sub {
	    my ( $uri, %args ) = @_;
        while ( my ( $key, $value ) = each %args ) {
            if ( $key =~ m/^-/ ) {
                $key =~ s/^-//;
                if ( $uri =~ m/\?/ ) {
                    if ( $uri !~ m/[&\?]$/ ) {
                        $uri .= '&';
                    }
                    $uri .= "${key}=${value}";
                }
                else {
                    $uri .= '?' . "${key}=${value}";
                }
            }
        }
        return $uri;
	};
	
	sub get_url {
	    my $self = shift;
	    my $uri = $uri{ $self->get_id };
	    if ( my %args = looks_like_hash @_ ) {
	        if ( $args{'-guid'} ) {
                $uri .= $args{'-guid'};
                delete $args{'-guid'};
                $uri = $build_query_string->( $uri, %args );
	        }
	        elsif ( $args{'-query'} ) {
	            $uri .= lc($args{'-section'}) . '/find?';
	            delete $args{'-section'};
	            $uri = $build_query_string->( $uri, %args );
	        }
	    }
	    return $uri;
	}
	
	sub _cleanup {
		my $self = shift;
		my $id = $self->get_id;
		for my $field (@fields) {
			delete $field->{$id};
		}
	}

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>

=head1 REVISION

 $Id: Phylo.pm 1045 2009-05-28 22:48:16Z rvos $

=cut

}
1;