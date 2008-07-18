# $Id: Taxa.pm 4786 2007-11-28 07:31:19Z rvosa $
package Bio::Phylo::Taxa;
use strict;
use Bio::Phylo::Listable;
use Bio::Phylo::Util::CONSTANT qw(_NONE_ _TAXA_ _FOREST_ _MATRIX_ looks_like_object);
use Bio::Phylo::Mediators::TaxaMediator;
use Bio::Phylo::Factory;
use vars qw(@ISA);

=begin comment

This class has no internal state, no cleanup is necessary.

=end comment

=cut

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::Listable);
{

	my $logger    = __PACKAGE__->get_logger;
	my $mediator  = 'Bio::Phylo::Mediators::TaxaMediator';
	my $factory   = Bio::Phylo::Factory->new;
	my $CONTAINER = _NONE_;
	my $TYPE      = _TAXA_;
	my $MATRIX    = _MATRIX_;
	my $FOREST    = _FOREST_;

=head1 NAME

Bio::Phylo::Taxa - An object-oriented module for managing taxa.

=head1 SYNOPSIS

 use Bio::Phylo::Taxa;
 use Bio::Phylo::Taxa::Taxon;

 # A mesquite-style default
 # taxa block for 10 taxa.
 my $taxa  = Bio::Phylo::Taxa->new;
 for my $i ( 1 .. 10 ) {
     my $taxon = Bio::Phylo::Taxa::Taxon->new(
         '-name' => 'taxon_' . $i,
     );
     $taxa->insert( $taxon );
 }

=head1 DESCRIPTION

The Bio::Phylo::Taxa object models a set of operational taxonomic units. The
object subclasses the Bio::Phylo::Listable object, and so the filtering
methods of that class are available.

A taxa object can link to multiple forest and matrix objects.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Taxa constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $taxa = Bio::Phylo::Taxa->new;
 Function: Instantiates a Bio::Phylo::Taxa object.
 Returns : A Bio::Phylo::Taxa object.
 Args    : none.

=cut

    sub new {
        # could be child class
        my $class = shift;
        
        # notify user
        $logger->info("constructor called for '$class'");
        
        # recurse up inheritance tree, get ID
        my $self = $class->SUPER::new( '-tag' => 'otus', @_ );
        
        # local fields would be set here
        
        return $self;
    }

=back

=head2 MUTATORS

=over

=item set_forest()

Sets associated Bio::Phylo::Forest object.

 Type    : Mutator
 Title   : set_forest
 Usage   : $taxa->set_forest( $forest );
 Function: Associates forest with the 
           invocant taxa object (i.e. 
           creates reference).
 Returns : Modified object.
 Args    : A Bio::Phylo::Forest object 
 Comments: A taxa object can link to multiple 
           forest and matrix objects.

=cut

    sub set_forest {
        my ( $self, $forest ) = @_;
        $logger->debug( "setting forest $forest" );
        if ( looks_like_object $forest, $FOREST ) {
        	$forest->set_taxa( $self );
        }    
        return $self;
    }

=item set_matrix()

Sets associated Bio::Phylo::Matrices::Matrix object.

 Type    : Mutator
 Title   : set_matrix
 Usage   : $taxa->set_matrix($matrix);
 Function: Associates matrix with the 
           invocant taxa object (i.e. 
           creates reference).
 Returns : Modified object.
 Args    : A Bio::Phylo::Matrices::Matrix object
 Comments: A taxa object can link to multiple 
           forest and matrix objects. 

=cut

    sub set_matrix {
        my ( $self, $matrix ) = @_;
        $logger->debug( "setting matrix $matrix" );
        if ( looks_like_object $matrix, $MATRIX ) {
        	$matrix->set_taxa( $self );
        }      
        return $self;
    }

=item unset_forest()

Removes association with argument Bio::Phylo::Forest object.

 Type    : Mutator
 Title   : unset_forest
 Usage   : $taxa->unset_forest($forest);
 Function: Disassociates forest from the 
           invocant taxa object (i.e. 
           removes reference).
 Returns : Modified object.
 Args    : A Bio::Phylo::Forest object

=cut

    sub unset_forest {
        my ( $self, $forest ) = @_;
        $logger->debug( "unsetting forest $forest" );
        if ( looks_like_object $forest, $FOREST ) {
        	$forest->unset_taxa();
        }      
        return $self;
    }

=item unset_matrix()

Removes association with Bio::Phylo::Matrices::Matrix object.

 Type    : Mutator
 Title   : unset_matrix
 Usage   : $taxa->unset_matrix($matrix);
 Function: Disassociates matrix from the 
           invocant taxa object (i.e. 
           removes reference).
 Returns : Modified object.
 Args    : A Bio::Phylo::Matrices::Matrix object

=cut

    sub unset_matrix {
        my ( $self, $matrix ) = @_;
        $logger->debug( "unsetting matrix $matrix" );
        if ( looks_like_object $matrix, $MATRIX ) {
        	$matrix->unset_taxa();
        }     
        return $self;
    }


=back

=head2 ACCESSORS

=over

=item get_forests()

Gets all associated Bio::Phylo::Forest objects.

 Type    : Accessor
 Title   : get_forests
 Usage   : @forests = @{ $taxa->get_forests };
 Function: Retrieves forests associated 
           with the current taxa object.
 Returns : An ARRAY reference of 
           Bio::Phylo::Forest objects.
 Args    : None.

=cut

    sub get_forests {
        my $self = shift;
        return $mediator->get_link( 
            '-source' => $self, 
            '-type'   => $FOREST,
        );
    }

=item get_matrices()

Gets all associated Bio::Phylo::Matrices::Matrix objects.

 Type    : Accessor
 Title   : get_matrices
 Usage   : @matrices = @{ $taxa->get_matrices };
 Function: Retrieves matrices associated 
           with the current taxa object.
 Returns : An ARRAY reference of 
           Bio::Phylo::Matrices::Matrix objects.
 Args    : None.

=cut

    sub get_matrices {
        my $self = shift;
        return $mediator->get_link( 
            '-source' => $self, 
            '-type'   => $MATRIX,
        );
    }

=item get_ntax()

Gets number of contained Bio::Phylo::Taxa::Taxon objects.

 Type    : Accessor
 Title   : get_ntax
 Usage   : my $ntax = $taxa->get_ntax;
 Function: Retrieves the number of taxa for the invocant.
 Returns : INT
 Args    : None.
 Comments:

=cut

    sub get_ntax {
        my $self = shift;
        return scalar @{ $self->get_entities };
    }

=back

=head2 METHODS

=over

=item merge_by_name()

Merges argument Bio::Phylo::Taxa object with invocant.

 Type    : Method
 Title   : merge_by_name
 Usage   : $taxa->merge_by_name($other_taxa);
 Function: Merges two taxa objects such that 
           internally different taxon objects 
           with the same name become a single
           object with the combined references 
           to datum objects and node objects 
           contained by the two.           
 Returns : A merged Bio::Phylo::Taxa object.
 Args    : A Bio::Phylo::Taxa object.

=cut

    sub merge_by_name {
        my $merged = $factory->create_taxa;
        for my $taxa ( @_ ) {
            my %object_by_name = map { $_->get_name => $_ } @{ $merged->get_entities };
            foreach my $taxon ( @{ $taxa->get_entities } ) {
                my $name   = $taxon->get_name;
                my $target = $factory->create_taxon( '-name' => $name );
                if ( exists $object_by_name{$name} ) {
                    $target = $object_by_name{$name};
                }                
                foreach my $datum ( @{ $taxon->get_data } ) {
                    $datum->set_taxon( $target );
                }
                foreach my $node ( @{ $taxon->get_nodes } ) {
                    $node->set_taxon( $target );
                }
                if ( not exists $object_by_name{$name} ) {
                    $merged->insert($target);
                    $object_by_name{ $target->get_name } = $target;
                }
            }
        }
        return $merged;
    }

=item to_nexus()

Serializes invocant to nexus format.

 Type    : Format convertor
 Title   : to_nexus
 Usage   : my $block = $taxa->to_nexus;
 Function: Converts $taxa into a nexus taxa block.
 Returns : Nexus taxa block (SCALAR).
 Args    : -links => 1 (optional, adds 'TITLE' token)
 Comments:

=cut    

	sub to_nexus {
		my $self = shift;
		my %args = @_;
		my $nexus = "BEGIN TAXA;\n";
		$nexus .=   "[! Taxa block written by " . ref($self) . " " . $self->VERSION . " on " . localtime() . " ]\n";
		if ( $args{'-links'} ) {
			$nexus .= "\tTITLE " . $self->get_internal_name . ";\n";
		} 
		$nexus .= "\tDIMENSIONS NTAX=" . $self->get_ntax . ";\n";
		$nexus .= "\tTAXLABELS\n";
		$nexus .= "\t\t" . $_->get_internal_name . "\n" for @{ $self->get_entities };
		$nexus .= "\t;\nEND;\n";
	}

=begin comment

 Type    : Internal method
 Title   : _container
 Usage   : $taxa->_container;
 Function:
 Returns : CONSTANT
 Args    :

=end comment

=cut

    sub _container { $CONTAINER }

=begin comment

 Type    : Internal method
 Title   : _type
 Usage   : $taxa->_type;
 Function:
 Returns : SCALAR
 Args    :

=end comment

=cut

    sub _type { $TYPE }

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Listable>

The L<Bio::Phylo::Taxa> object inherits from the L<Bio::Phylo::Listable>
object. Look there for more methods applicable to the taxa object.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id: Taxa.pm 4786 2007-11-28 07:31:19Z rvosa $

=cut

}
1;
