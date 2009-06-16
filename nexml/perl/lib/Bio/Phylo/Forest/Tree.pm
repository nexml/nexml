# $Id$
package Bio::Phylo::Forest::Tree;
use strict;
use Bio::Phylo::Listable;
use Bio::Phylo::Forest::Node;
use Bio::Phylo::IO qw(unparse);
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Util::CONSTANT qw(_TREE_ _FOREST_ _DOMCREATOR_ looks_like_number looks_like_hash);
use Bio::Phylo::Factory;
use Scalar::Util qw(blessed);
use vars qw(@ISA);

# classic @ISA manipulation, not using 'base'
@ISA = qw(Bio::Phylo::Listable);

eval { require Bio::Tree::TreeI };
if ( not $@ ) {
	push @ISA, 'Bio::Tree::TreeI';
}
else {
	undef($@);
}

my $LOADED_WRAPPERS = 0;

{
	#my $mediator = Bio::Phylo::Mediators::NodeMediator->new;
	my $logger = __PACKAGE__->get_logger;
	my ( $TYPE_CONSTANT, $CONTAINER_CONSTANT ) = ( _TREE_, _FOREST_ );
	my @fields = \( my ( %default, %rooted ) );
	my $fac = Bio::Phylo::Factory->new;
	my %default_constructor_args = (
        '-tag'      => 'tree', 
        '-listener' => sub {
            my ( $self, $method, @args ) = @_;                
            for my $node ( @args ) {
                if ( $method eq 'insert' ) {
                    $node->set_tree( $self );
                }
                elsif ( $method eq 'delete' ) {
                    $node->set_tree();
                }
            }
        },	
	);

=head1 NAME

Bio::Phylo::Forest::Tree - Phylogenetic tree

=head1 SYNOPSIS

 # some way to get a tree
 use Bio::Phylo::IO;
 my $string = '((A,B),C);';
 my $forest = Bio::Phylo::IO->parse(
    -format => 'newick',
    -string => $string
 );
 my $tree = $forest->first;

 # do something:
 print $tree->calc_imbalance;

 # prints "1"

=head1 DESCRIPTION

The object models a phylogenetic tree, a container of
L<Bio::Phylo::Forest::Node> objects. The tree object
inherits from L<Bio::Phylo::Listable>, so look there
for more methods.

=head1 METHODS

=head2 CONSTRUCTORS

=over

=item new()

Tree constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $tree = Bio::Phylo::Forest::Tree->new;
 Function: Instantiates a Bio::Phylo::Forest::Tree object.
 Returns : A Bio::Phylo::Forest::Tree object.
 Args    : No required arguments.

=cut

	sub new {

		# could be child class
		my $class = shift;

		# notify user
		$logger->info("constructor called for '$class'");

		if ( not $LOADED_WRAPPERS ) {
			eval do { local $/; <DATA> };
			$LOADED_WRAPPERS++;
		}	

		# go up inheritance tree, eventually get an ID
		my $self = $class->SUPER::new( %default_constructor_args, @_ );			
		return $self;
	}

=item new_from_bioperl()

Tree constructor from Bio::Tree::TreeI argument.

 Type    : Constructor
 Title   : new_from_bioperl
 Usage   : my $tree = 
           Bio::Phylo::Forest::Tree->new_from_bioperl(
               $bptree           
           );
 Function: Instantiates a 
           Bio::Phylo::Forest::Tree object.
 Returns : A Bio::Phylo::Forest::Tree object.
 Args    : A tree that implements Bio::Tree::TreeI

=cut

	sub new_from_bioperl {
		my ( $class, $bptree ) = @_;
		my $self;
		if ( blessed $bptree && $bptree->isa('Bio::Tree::TreeI') ) {
			$self = $fac->create_tree;
			bless $self, $class;
			$self = $self->_recurse( $bptree->get_root_node );
			
			# copy name
			my $name = $bptree->id;
			$self->set_name( $name ) if defined $name;
			
			# copy score
			my $score = $bptree->score;
			$self->set_score( $score ) if defined $score;
		}
		else {
			throw 'ObjectMismatch' => 'Not a bioperl tree!';
		}
		return $self;
	}

=begin comment

 Type    : Internal method
 Title   : _recurse
 Usage   : $tree->_recurse( $bpnode );
 Function: Traverses a bioperl tree, instantiates a Bio::Phylo::Forest::Node
           object for every Bio::Tree::NodeI object it encounters, copying
           the parent, sibling and child relationships.
 Returns : None (modifies invocant).
 Args    : A Bio::Tree::NodeI object.

=end comment

=cut    

	sub _recurse {
		my ( $self, $bpnode, $parent ) = @_;
		my $node = Bio::Phylo::Forest::Node->new_from_bioperl($bpnode);
		if ($parent) {
			$parent->set_child($node);
		}
		$self->insert($node);
		foreach my $bpchild ( $bpnode->each_Descendent ) {
			$self->_recurse( $bpchild, $node );
		}
		return $self;
	}

=begin comment

 Type    : Internal method
 Title   : _analyze
 Usage   : $tree->_analyze;
 Function: Traverses the tree, creates references to first_daughter,
           last_daughter, next_sister and previous_sister.
 Returns : A Bio::Phylo::Forest::Tree object.
 Args    : none.
 Comments: This method only looks at the parent, so theoretically
           one could mess around with the
           Bio::Phylo::Forest::Node::set_parent(Bio::Phylo::Forest::Node) method and
           subsequently call Bio::Phylo::Forest::Tree::_analyze to overwrite old
           (and wrong) child and sister references with new (and correct) ones.

=end comment

=cut

	sub _analyze {
		my $tree  = $_[0];
		my $nodes = $tree->get_entities;
		foreach ( @{$nodes} ) {
			$_->set_next_sister();
			$_->set_previous_sister();
			$_->set_first_daughter();
			$_->set_last_daughter();
		}
		my ( $i, $j, $first, $next );

		# mmmm... O(N^2)
	  NODE: for $i ( 0 .. $#{$nodes} ) {
			$first = $nodes->[$i];
			for $j ( ( $i + 1 ) .. $#{$nodes} ) {
				$next = $nodes->[$j];
				my ( $firstp, $nextp ) =
				  ( $first->get_parent, $next->get_parent );
				if ( $firstp && $nextp && $firstp == $nextp ) {
					if ( !$first->get_next_sister ) {
						$first->set_next_sister($next);
					}
					if ( !$next->get_previous_sister ) {
						$next->set_previous_sister($first);
					}
					next NODE;
				}
			}
		}

		# O(N)
		foreach ( @{$nodes} ) {
			my $p = $_->get_parent;
			if ($p) {
				if ( !$_->get_next_sister ) {
					$p->set_last_daughter($_);
					next;
				}
				if ( !$_->get_previous_sister ) {
					$p->set_first_daughter($_);
				}
			}
		}
		return $tree;
	}

=back 

=head2 MUTATORS

=over

=item set_as_unrooted()

Sets tree to be interpreted as unrooted.

 Type    : Mutator
 Title   : set_as_unrooted
 Usage   : $tree->set_as_unrooted;
 Function: Sets tree to be interpreted as unrooted.
 Returns : $tree
 Args    : NONE
 Comments: This is a flag to indicate that the invocant
           is interpreted to be unrooted (regardless of
           topology). The object is otherwise unaltered,
           this method is only here to capture things such
           as the [&U] token in nexus files.

=cut

	sub set_as_unrooted {
		my $self = shift;
		$rooted{$$self} = 1;
		return $self;
	}

=item set_as_default()

Sets tree to be the default tree in a forest

 Type    : Mutator
 Title   : set_as_default
 Usage   : $tree->set_as_default;
 Function: Sets tree to be default tree in forest
 Returns : $tree
 Args    : NONE
 Comments: This is a flag to indicate that the invocant
           is the default tree in a forest, i.e. to
           capture the '*' token in nexus files.

=cut

	sub set_as_default {
		my $self = shift;
		if ( my $forest = $self->_get_container ) {
			if ( my $tree = $forest->get_default_tree ) {
				$tree->set_not_default;
			}		
		}
		$default{$$self} = 1;
		return $self;
	}

=item set_not_default()

Sets tree to NOT be the default tree in a forest

 Type    : Mutator
 Title   : set_not_default
 Usage   : $tree->set_not_default;
 Function: Sets tree to not be default tree in forest
 Returns : $tree
 Args    : NONE
 Comments: This is a flag to indicate that the invocant
           is the default tree in a forest, i.e. to
           capture the '*' token in nexus files.

=cut

	sub set_not_default {
		my $self = shift;
		$default{$$self} = 0;
		return $self;
	}

=back

=head2 QUERIES

=over

=item get_terminals()

Get terminal nodes.

 Type    : Query
 Title   : get_terminals
 Usage   : my @terminals = @{ $tree->get_terminals };
 Function: Retrieves all terminal nodes in
           the Bio::Phylo::Forest::Tree object.
 Returns : An array reference of 
           Bio::Phylo::Forest::Node objects.
 Args    : NONE
 Comments: If the tree is valid, this method 
           retrieves the same set of nodes as 
           $node->get_terminals($root). However, 
           because there is no recursion it may 
           be faster. Also, the node method by 
           the same name does not see orphans.

=cut

	sub get_terminals {
		my $self = shift;
		my @terminals;
		for ( @{ $self->get_entities } ) {
			if ( $_->is_terminal ) {
				push @terminals, $_;
			}
		}
		return \@terminals;
	}

=item get_internals()

Get internal nodes.

 Type    : Query
 Title   : get_internals
 Usage   : my @internals = @{ $tree->get_internals };
 Function: Retrieves all internal nodes 
           in the Bio::Phylo::Forest::Tree object.
 Returns : An array reference of 
           Bio::Phylo::Forest::Node objects.
 Args    : NONE
 Comments: If the tree is valid, this method 
           retrieves the same set of nodes as 
           $node->get_internals($root). However, 
           because there is no recursion it may 
           be faster. Also, the node method by 
           the same name does not see orphans.

=cut

	sub get_internals {
		my $self = shift;
		my @internals;
		foreach ( @{ $self->get_entities } ) {
			if ( $_->is_internal ) {
				push @internals, $_;
			}
		}
		return \@internals;
	}

=item get_root()

Get root node.

 Type    : Query
 Title   : get_root
 Usage   : my $root = $tree->get_root;
 Function: Retrieves the first orphan in 
           the current Bio::Phylo::Forest::Tree
           object - which should be the root.
 Returns : Bio::Phylo::Forest::Node
 Args    : NONE

=cut

	sub get_root {
		my $self = shift;
		for ( @{ $self->get_entities } ) {
			if ( !$_->get_parent ) {
				return $_;
			}
		}
		return;
	}

=item get_tallest_tip()

Retrieves the node furthest from the root. 

 Type    : Query
 Title   : get_tallest_tip
 Usage   : my $tip = $tree->get_tallest_tip;
 Function: Retrieves the node furthest from the
           root in the current Bio::Phylo::Forest::Tree
           object.
 Returns : Bio::Phylo::Forest::Node
 Args    : NONE
 Comments: If the tree has branch lengths, the tallest tip is
           based on root-to-tip path length, else it is based
           on number of nodes to root

=cut

	sub get_tallest_tip {
		my $self = shift;
		my $criterion;

		# has (at least some) branch lengths
		if ( $self->calc_tree_length ) {
			$criterion = 'calc_path_to_root',;
		}
		else {
			$criterion = 'calc_nodes_to_root';
		}

		my $tallest;
		my $height = 0;
		for my $tip ( @{ $self->get_terminals } ) {
			if ( my $path = $tip->$criterion ) {
				if ( $path > $height ) {
					$tallest = $tip;
					$height  = $path;
				}
			}
		}
		return $tallest;
	}

=item get_mrca()

Get most recent common ancestor of argument nodes.

 Type    : Query
 Title   : get_mrca
 Usage   : my $mrca = $tree->get_mrca(\@nodes);
 Function: Retrieves the most recent 
           common ancestor of \@nodes
 Returns : Bio::Phylo::Forest::Node
 Args    : A reference to an array of 
           Bio::Phylo::Forest::Node objects 
           in $tree.

=cut

	sub get_mrca {
		my ( $tree, $nodes ) = @_;
		my $mrca;
		for my $i ( 1 .. $#{$nodes} ) {
			$mrca ? $mrca = $mrca->get_mrca( $nodes->[$i] ) : $mrca =
			  $nodes->[0]->get_mrca( $nodes->[$i] );
		}
		return $mrca;
	}

=back

=head2 TESTS

=over

=item is_default()

Test if tree is default tree.

 Type    : Test
 Title   : is_default
 Usage   : if ( $tree->is_default ) {
              # do something
           }
 Function: Tests whether the invocant 
           object is the default tree in the forest.
 Returns : BOOLEAN
 Args    : NONE

=cut

	sub is_default {
		my $self = shift;
		return !!$default{$$self};
	}

=item is_rooted()

Test if tree is rooted.

 Type    : Test
 Title   : is_rooted
 Usage   : if ( $tree->is_rooted ) {
              # do something
           }
 Function: Tests whether the invocant 
           object is rooted.
 Returns : BOOLEAN
 Args    : NONE
 Comments: A tree is considered unrooted if:
           - set_as_unrooted has been set, or
           - the basal split is a polytomy

=cut

	sub is_rooted {
		my $self = shift;
		if ( defined $rooted{$$self} ) {
			return $rooted{$$self};
		}
		if ( my $root = $self->get_root ) {
			if ( my $children = $root->get_children ) {
				return scalar @{ $children } <= 2;
			}
			return 1;
		}
		return 0;
	}

=item is_binary()

Test if tree is bifurcating.

 Type    : Test
 Title   : is_binary
 Usage   : if ( $tree->is_binary ) {
              # do something
           }
 Function: Tests whether the invocant 
           object is bifurcating.
 Returns : BOOLEAN
 Args    : NONE

=cut

	sub is_binary {
		my $self = shift;
		for ( @{ $self->get_internals } ) {
			if ( $_->get_first_daughter->get_next_sister->get_id !=
				$_->get_last_daughter->get_id )
			{
				return;
			}
		}
		return 1;
	}

=item is_ultrametric()

Test if tree is ultrametric.

 Type    : Test
 Title   : is_ultrametric
 Usage   : if ( $tree->is_ultrametric(0.01) ) {
              # do something
           }
 Function: Tests whether the invocant is 
           ultrametric.
 Returns : BOOLEAN
 Args    : Optional margin between pairwise 
           comparisons (default = 0).
 Comments: The test is done by performing 
           all pairwise comparisons for
           root-to-tip path lengths. Since many 
           programs introduce rounding errors 
           in branch lengths the optional argument is
           available to test TRUE for nearly 
           ultrametric trees. For example, a value 
           of 0.01 indicates that no pairwise
           comparison may differ by more than 1%. 
           Note: behaviour is undefined for 
           negative branch lengths.

=cut

	sub is_ultrametric {
		my ( $tree, $margin ) = @_;
		if ( !$margin ) {
			$margin = 0;
		}
		my @paths;
		foreach ( @{ $tree->get_terminals } ) {
			push @paths, $_->calc_path_to_root;
		}
		for my $i ( 0 .. $#paths ) {
			for my $j ( ( $i + 1 ) .. $#paths ) {
				my $diff;
				if ( $paths[$i] < $paths[$j] ) {
					$diff = $paths[$i] / $paths[$j];
				}
				else {
					if ( $paths[$i] ) {
						$diff = $paths[$j] / $paths[$i];
					}
				}
				if ( $diff && ( 1 - $diff ) > $margin ) {
					return;
				}
			}
		}
		return 1;
	}

=item is_monophyletic()

Tests if first argument (node array ref) is monophyletic with respect
to second argument.

 Type    : Test
 Title   : is_monophyletic
 Usage   : if ( $tree->is_monophyletic(\@tips, $node) ) {
              # do something
           }
 Function: Tests whether the set of \@tips is
           monophyletic w.r.t. $outgroup.
 Returns : BOOLEAN
 Args    : A reference to a list of nodes, and a node.
 Comments: This method is essentially the
           same as 
           &Bio::Phylo::Forest::Node::is_outgroup_of.

=cut

	sub is_monophyletic {
		my $tree = shift;
		my ( $nodes, $outgroup );
		if ( @_ == 2 ) {
		    ( $nodes, $outgroup ) = @_;
		}
		elsif ( @_ == 4 ) {
		    my %args = @_;
		    $nodes = $args{'-nodes'};
		    $outgroup = $args{'-outgroup'};
		}		
		for my $i ( 0 .. $#{$nodes} ) {
			for my $j ( ( $i + 1 ) .. $#{$nodes} ) {
				my $mrca = $nodes->[$i]->get_mrca( $nodes->[$j] );
				return if $mrca->is_ancestor_of($outgroup);
			}
		}
		return 1;
	}

=item is_paraphyletic()

 Type    : Test
 Title   : is_paraphyletic
 Usage   : if ( $tree->is_paraphyletic(\@nodes,$node) ){ }
 Function: Tests whether or not a given set of nodes are paraphyletic
           (representing the full clade) given an outgroup
 Returns : [-1,0,1] , -1 if the group is not monophyletic
                       0 if the group is not paraphyletic
                       1 if the group is paraphyletic
 Args    : Array ref of node objects which are in the tree,
           Outgroup to compare the nodes to

=cut

	sub is_paraphyletic {
		my $tree = shift;
		my ( $nodes, $outgroup );
		if ( @_ == 2 ) {
		    ( $nodes, $outgroup ) = @_;
		}
		elsif ( @_ == 4 ) {
		    my %args = @_;
		    $nodes = $args{'-nodes'};
		    $outgroup = $args{'-outgroup'};
		}
		return -1 if ! $tree->is_monophyletic($nodes,$outgroup);
		my @all = ( @{ $nodes }, $outgroup );
		my $mrca = $tree->get_mrca(\@all);
		my $tips = $mrca->get_terminals;
		return scalar @{ $tips } == scalar @all ? 0 : 1;
	}

=item is_clade()

Tests if argument (node array ref) forms a clade.

 Type    : Test
 Title   : is_clade
 Usage   : if ( $tree->is_clade(\@tips) ) {
              # do something
           }
 Function: Tests whether the set of 
           \@tips forms a clade
 Returns : BOOLEAN
 Args    : A reference to an array of 
           Bio::Phylo::Forest::Node objects.
 Comments:

=cut

	sub is_clade {
		my ( $tree, $tips ) = @_;
		my $mrca;
		for my $i ( 1 .. $#{$tips} ) {
			$mrca ? $mrca = $mrca->get_mrca( $tips->[$i] ) : $mrca =
			  $tips->[0]->get_mrca( $tips->[$i] );
		}
		scalar @{ $mrca->get_terminals } == scalar @{$tips} ? return 1 : return;
	}

=item is_cladogram()

Tests if tree is a cladogram (i.e. no branch lengths)

 Type    : Test
 Title   : is_cladogram
 Usage   : if ( $tree->is_cladogram() ) {
              # do something
           }
 Function: Tests whether the tree is a 
           cladogram (i.e. no branch lengths)
 Returns : BOOLEAN
 Args    : NONE
 Comments:

=cut

	sub is_cladogram {
	    my $tree = shift;
	    for my $node ( @{ $tree->get_entities } ) {
	        return 0 if defined $node->get_branch_length;
	    }
	    return 1;
	}

=back

=head2 CALCULATIONS

=over

=item calc_tree_length()

Calculates the sum of all branch lengths.

 Type    : Calculation
 Title   : calc_tree_length
 Usage   : my $tree_length = 
           $tree->calc_tree_length;
 Function: Calculates the sum of all branch 
           lengths (i.e. the tree length).
 Returns : FLOAT
 Args    : NONE

=cut

	sub calc_tree_length {
		my $self = shift;
		my $tl   = 0;
		for ( @{ $self->get_entities } ) {
			if ( my $bl = $_->get_branch_length ) {
				$tl += $bl if defined $bl;
			}
		}
		return $tl;
	}

=item calc_tree_height()

Calculates the height of the tree.

 Type    : Calculation
 Title   : calc_tree_height
 Usage   : my $tree_height = 
           $tree->calc_tree_height;
 Function: Calculates the height 
           of the tree.
 Returns : FLOAT
 Args    : NONE
 Comments: For ultrametric trees this 
           method returns the height, but 
           this is done by averaging over 
           all root-to-tip path lengths, so 
           for additive trees the result 
           should consequently be interpreted
           differently.

=cut

	sub calc_tree_height {
		my $self = shift;
		my $th   = $self->calc_total_paths / $self->calc_number_of_terminals;
		return $th;
	}

=item calc_number_of_nodes()

Calculates the number of nodes.

 Type    : Calculation
 Title   : calc_number_of_nodes
 Usage   : my $number_of_nodes = 
           $tree->calc_number_of_nodes;
 Function: Calculates the number of 
           nodes (internals AND terminals).
 Returns : INT
 Args    : NONE

=cut

	sub calc_number_of_nodes {
		my $self     = shift;
		my $numnodes = scalar @{ $self->get_entities };
		return $numnodes;
	}

=item calc_number_of_terminals()

Calculates the number of terminal nodes.

 Type    : Calculation
 Title   : calc_number_of_terminals
 Usage   : my $number_of_terminals = 
           $tree->calc_number_of_terminals;
 Function: Calculates the number 
           of terminal nodes.
 Returns : INT
 Args    : NONE

=cut

	sub calc_number_of_terminals {
		my $self    = shift;
		my $numterm = scalar @{ $self->get_terminals };
		return $numterm;
	}

=item calc_number_of_internals()

Calculates the number of internal nodes.

 Type    : Calculation
 Title   : calc_number_of_internals
 Usage   : my $number_of_internals = 
           $tree->calc_number_of_internals;
 Function: Calculates the number 
           of internal nodes.
 Returns : INT
 Args    : NONE

=cut

	sub calc_number_of_internals {
		my $self   = shift;
		my $numint = scalar @{ $self->get_internals };
		return $numint;
	}

=item calc_total_paths()

Calculates the sum of all root-to-tip path lengths.

 Type    : Calculation
 Title   : calc_total_paths
 Usage   : my $total_paths = 
           $tree->calc_total_paths;
 Function: Calculates the sum of all 
           root-to-tip path lengths.
 Returns : FLOAT
 Args    : NONE

=cut

	sub calc_total_paths {
		my $self = shift;
		my $tp   = 0;
		foreach ( @{ $self->get_terminals } ) {
			$tp += $_->calc_path_to_root;
		}
		return $tp;
	}

=item calc_redundancy()

Calculates the amount of shared (redundant) history on the total.

 Type    : Calculation
 Title   : calc_redundancy
 Usage   : my $redundancy = 
           $tree->calc_redundancy;
 Function: Calculates the amount of shared 
           (redundant) history on the total.
 Returns : FLOAT
 Args    : NONE
 Comments: Redundancy is calculated as
 1 / ( treelength - height / ( ntax * height - height ) )

=cut

	sub calc_redundancy {
		my $self = shift;
		my $tl   = $self->calc_tree_length;
		my $th   = $self->calc_tree_height;
		my $ntax = $self->calc_number_of_terminals;
		my $red  = 1 - ( ( $tl - $th ) / ( ( $th * $ntax ) - $th ) );
		return $red;
	}

=item calc_imbalance()

Calculates Colless' coefficient of tree imbalance.

 Type    : Calculation
 Title   : calc_imbalance
 Usage   : my $imbalance = $tree->calc_imbalance;
 Function: Calculates Colless' coefficient 
           of tree imbalance.
 Returns : FLOAT
 Args    : NONE
 Comments: As described in Colless, D.H., 1982. 
           The theory and practice of phylogenetic 
           systematics. Systematic Zoology 31(1): 100-104

=cut

	sub calc_imbalance {
		my $self = shift;
		my ( $maxic, $sum, $Ic ) = ( 0, 0 );
		if ( !$self->is_binary ) {
			throw 'ObjectMismatch' => 'Colless\' imbalance only possible for binary trees';
		}
		my $numtips = $self->calc_number_of_terminals;
		$numtips -= 2;
		while ($numtips) {
			$maxic += $numtips;
			$numtips--;
		}
		for my $node ( @{ $self->get_internals } ) {
			my ( $fd, $ld, $ftips, $ltips ) =
			  ( $node->get_first_daughter, $node->get_last_daughter, 0, 0 );
			if ( $fd->is_internal ) {
				for ( @{ $fd->get_descendants } ) {
					if   ( $_->is_terminal ) { $ftips++; }
					else                     { next; }
				}
			}
			else { $ftips = 1; }
			if ( $ld->is_internal ) {
				foreach ( @{ $ld->get_descendants } ) {
					if   ( $_->is_terminal ) { $ltips++; }
					else                     { next; }
				}
			}
			else { $ltips = 1; }
			$sum += abs( $ftips - $ltips );
		}
		$Ic = $sum / $maxic;
		return $Ic;
	}

=item calc_i2()

Calculates I2 imbalance.

 Type    : Calculation
 Title   : calc_i2
 Usage   : my $ci2 = $tree->calc_i2;
 Function: Calculates I2 imbalance.
 Returns : FLOAT
 Args    : NONE
 Comments:

=cut

	sub calc_i2 {
		my $self = shift;
		my ( $maxic, $sum, $I2 ) = ( 0, 0 );
		if ( !$self->is_binary ) {
			throw 'ObjectMismatch' => 'I2 imbalance only possible for binary trees';
		}
		my $numtips = $self->calc_number_of_terminals;
		$numtips -= 2;
		while ($numtips) {
			$maxic += $numtips;
			$numtips--;
		}
		foreach my $node ( @{ $self->get_internals } ) {
			my ( $fd, $ld, $ftips, $ltips ) =
			  ( $node->get_first_daughter, $node->get_last_daughter, 0, 0 );
			if ( $fd->is_internal ) {
				foreach ( @{ $fd->get_descendants } ) {
					if ( $_->is_terminal ) {
						$ftips++;
					}
					else {
						next;
					}
				}
			}
			else {
				$ftips = 1;
			}
			if ( $ld->is_internal ) {
				foreach ( @{ $ld->get_descendants } ) {
					if ( $_->is_terminal ) {
						$ltips++;
					}
					else {
						next;
					}
				}
			}
			else {
				$ltips = 1;
			}
			next unless ( $ftips + $ltips - 2 );
			$sum += abs( $ftips - $ltips ) / abs( $ftips + $ltips - 2 );
		}
		$I2 = $sum / $maxic;
		return $I2;
	}

=item calc_gamma()

Calculates the Pybus gamma statistic.

 Type    : Calculation
 Title   : calc_gamma
 Usage   : my $gamma = $tree->calc_gamma();
 Function: Calculates the Pybus gamma statistic
 Returns : FLOAT
 Args    : NONE
 Comments: As described in Pybus, O.G. and 
           Harvey, P.H., 2000. Testing
           macro-evolutionary models using 
           incomplete molecular phylogenies. 
           Proc. R. Soc. Lond. B 267, 2267-2272

=cut

	# code due to Aki Mimoto
	sub calc_gamma {
		my $self      = shift;
		my $tl        = $self->calc_tree_length;
		my $terminals = $self->get_terminals;
		my $n         = scalar @{$terminals};
		my $height    = $self->calc_tree_height;

	  # Calculate the distance of each node to the root
	  #        my %soft_refs;
	  #        my $root = $self->get_root;
	  #        $soft_refs{$root} = 0;
	  #        my @nodes = $root;
	  #        while (@nodes) {
	  #            my $node     = shift @nodes;
	  #            my $path_len = $soft_refs{$node} += $node->get_branch_length;
	  #            my $children = $node->get_children or next;
	  #            for my $child (@$children) {
	  #                $soft_refs{$child} = $path_len;
	  #            }
	  #            push @nodes, @{$children};
	  #        }
	  # the commented out block is more efficiently implemented like so:
		my %soft_refs =
		  map { $_ => $_->calc_path_to_root } @{ $self->get_entities };

		# Then, we know how far each node is from the root. At this point, we
		# can sort through and create the @g array
		my %node_spread =
		  map { ( $_ => 1 ) } values %soft_refs;    # remove duplicates
		my @sorted_nodes = sort { $a <=> $b } keys %node_spread;
		my $prev = 0;
		my @g;
		for my $length (@sorted_nodes) {
			push @g, $length - $prev;
			$prev = $length;
		}
		my $sum = 0;
		eval "require Math::BigFloat";
		if ($@) {                                   # BigFloat is not available.
			for ( my $i = 2 ; $i < $n ; $i++ ) {
				for ( my $k = 2 ; $k <= $i ; $k++ ) {
					$sum += $k * $g[ $k - 1 ];
				}
			}
			my $numerator = ( $sum / ( $n - 2 ) ) - ( $tl / 2 );
			my $denominator = $tl * sqrt( 1 / ( 12 * ( $n - 2 ) ) );
			$self->_store_cache( $numerator / $denominator );
			return $numerator / $denominator;
		}

		# Big Float is available. We'll use it then
		$sum = Math::BigFloat->new(0);
		for ( my $i = 2 ; $i < $n ; $i++ ) {
			for ( my $k = 2 ; $k <= $i ; $k++ ) {
				$sum->badd( $k * $g[ $k - 1 ] );
			}
		}
		$sum->bdiv( $n - 2 );
		$sum->bsub( $tl / 2 );
		my $denominator = Math::BigFloat->new(1);
		$denominator->bdiv( 12 * ( $n - 2 ) );
		$denominator->bsqrt();
		$sum->bdiv( $denominator * $tl );
		return $sum;
	}

=item calc_fiala_stemminess()

Calculates stemminess measure of Fiala and Sokal (1985).

 Type    : Calculation
 Title   : calc_fiala_stemminess
 Usage   : my $fiala_stemminess = 
           $tree->calc_fiala_stemminess;
 Function: Calculates stemminess measure 
           Fiala and Sokal (1985).
 Returns : FLOAT
 Args    : NONE
 Comments: As described in Fiala, K.L. and 
           R.R. Sokal, 1985. Factors 
           determining the accuracy of 
           cladogram estimation: evaluation 
           using computer simulation. 
           Evolution, 39: 609-622

=cut

	sub calc_fiala_stemminess {
		my $self      = shift;
		my @internals = @{ $self->get_internals };
		my $total     = 0;
		my $nnodes    = ( scalar @internals - 1 );
		foreach my $node (@internals) {
			if ( $node->get_parent ) {
				my $desclengths = $node->get_branch_length;
				my @children    = @{ $node->get_descendants };
				for my $child (@children) {
					$desclengths += $child->get_branch_length;
				}
				$total += ( $node->get_branch_length / $desclengths );
			}
		}
		$total /= $nnodes;
		return $total;
	}

=item calc_rohlf_stemminess()

Calculates stemminess measure from Rohlf et al. (1990).

 Type    : Calculation
 Title   : calc_rohlf_stemminess
 Usage   : my $rohlf_stemminess = 
           $tree->calc_rohlf_stemminess;
 Function: Calculates stemminess measure 
           from Rohlf et al. (1990).
 Returns : FLOAT
 Args    : NONE
 Comments: As described in Rohlf, F.J., 
           W.S. Chang, R.R. Sokal, J. Kim, 
           1990. Accuracy of estimated 
           phylogenies: effects of tree 
           topology and evolutionary model. 
           Evolution, 44(6): 1671-1684

=cut

	sub calc_rohlf_stemminess {
		my $self = shift;
		if ( !$self->is_ultrametric(0.01) ) {
			throw 'ObjectMismatch' => 'Rohlf stemminess only possible for ultrametric trees';
		}
		my @internals            = @{ $self->get_internals };
		my $total                = 0;
		my $one_over_t_minus_two = 1 / ( scalar @internals - 1 );
		foreach my $node (@internals) {
			if ( $node->get_parent ) {
				my $Wj_i   = $node->get_branch_length;
				my $parent = $node->get_parent;
				my $hj     = $parent->calc_min_path_to_tips;
				if ( !$hj ) {
					next;
				}
				$total += ( $Wj_i / $hj );
			}
		}
		unless ($total) {
			throw 'ObjectMismatch' => 'it looks like all branches were of length zero';
		}
		my $crs = $one_over_t_minus_two * $total;
		return $crs;
	}

=item calc_resolution()

Calculates tree resolution.

 Type    : Calculation
 Title   : calc_resolution
 Usage   : my $resolution = 
           $tree->calc_resolution;
 Function: Calculates the total number 
           of internal nodes over the
           total number of internal nodes 
           on a fully bifurcating
           tree of the same size.
 Returns : FLOAT
 Args    : NONE

=cut

	sub calc_resolution {
		my $self = shift;
		my $res  = $self->calc_number_of_internals /
		  ( $self->calc_number_of_terminals - 1 );
		return $res;
	}

=item calc_branching_times()

Calculates branching times.

 Type    : Calculation
 Title   : calc_branching_times
 Usage   : my $branching_times = 
           $tree->calc_branching_times;
 Function: Returns a two-dimensional array. 
           The first dimension consists of 
           the "records", so that in the 
           second dimension $AoA[$first][0] 
           contains the internal node references, 
           and $AoA[$first][1] the branching 
           time of the internal node. The 
           records are orderered from root to 
           tips by time from the origin.
 Returns : SCALAR[][] or FALSE
 Args    : NONE

=cut

	sub calc_branching_times {
		my $self = shift;
		my @branching_times;
		if ( !$self->is_ultrametric(0.01) ) {
			throw 'ObjectMismatch' => 'tree isn\'t ultrametric, results would be meaningless';
		}
		else {
			my ( $i, @temp ) = 0;
			foreach ( @{ $self->get_internals } ) {
				$temp[$i] = [ $_, $_->calc_path_to_root ];
				$i++;
			}
			@branching_times = sort { $a->[1] <=> $b->[1] } @temp;
		}
		return \@branching_times;
	}

=item calc_ltt()

Calculates lineage-through-time data points.

 Type    : Calculation
 Title   : calc_ltt
 Usage   : my $ltt = $tree->calc_ltt;
 Function: Returns a two-dimensional array. 
           The first dimension consists of the 
           "records", so that in the second 
           dimension $AoA[$first][0] contains 
           the internal node references, and
           $AoA[$first][1] the branching time 
           of the internal node, and $AoA[$first][2] 
           the cumulative number of lineages over
           time. The records are orderered from 
           root to tips by time from the origin.
 Returns : SCALAR[][] or FALSE
 Args    : NONE

=cut

	sub calc_ltt {
		my $self = shift;
		if ( !$self->is_ultrametric(0.01) ) {
			throw 'ObjectMismatch' => 'tree isn\'t ultrametric, results are meaningless';
		}
		my $ltt      = ( $self->calc_branching_times );
		my $lineages = 1;
		for my $i ( 0 .. $#{$ltt} ) {
			$lineages += ( scalar @{ $ltt->[$i][0]->get_children } - 1 );
			$ltt->[$i][2] = $lineages;
		}
		return $ltt;
	}

=item calc_symdiff()

Calculates the symmetric difference metric between invocant and argument.

 Type    : Calculation
 Title   : calc_symdiff
 Usage   : my $symdiff = 
           $tree->calc_symdiff($other_tree);
 Function: Returns the symmetric difference 
           metric between $tree and $other_tree, 
           sensu Penny and Hendy, 1985.
 Returns : SCALAR
 Args    : A Bio::Phylo::Forest::Tree object
 Comments: Trees in comparison must span 
           the same set of terminal taxa
           or results are meaningless.

=cut

	sub calc_symdiff {
		my ( $tree, $other_tree ) = @_;
		my ( $symdiff, @clades1, @clades2 ) = (0);
		foreach my $node ( @{ $tree->get_internals } ) {
			my $tips = join ' ',
			  sort { $a cmp $b } map { $_->get_name } @{ $node->get_terminals };
			push @clades1, $tips;
		}
		foreach my $node ( @{ $other_tree->get_internals } ) {
			my $tips = join ' ',
			  sort { $a cmp $b } map { $_->get_name } @{ $node->get_terminals };
			push @clades2, $tips;
		}
	  OUTER: foreach my $outer (@clades1) {
			foreach my $inner (@clades2) {
				next OUTER if $outer eq $inner;
			}
			$symdiff++;
		}
	  OUTER: foreach my $outer (@clades2) {
			foreach my $inner (@clades1) {
				next OUTER if $outer eq $inner;
			}
			$symdiff++;
		}
		return $symdiff;
	}

=item calc_fp() 

Calculates the Fair Proportion value for each terminal.

 Type    : Calculation
 Title   : calc_fp
 Usage   : my $fp = $tree->calc_fp();
 Function: Returns the Fair Proportion 
           value for each terminal
 Returns : HASHREF
 Args    : NONE

=cut

	# code due to Aki Mimoto
	sub calc_fp {
		my $self = shift;

		# First establish how many children sit on each of the nodes
		my %weak_ref;
		my $terminals = $self->get_terminals;
		for my $terminal (@$terminals) {
			my $index = $terminal;
			do { $weak_ref{$index}++ } while ( $index = $index->get_parent );
		}

		# Then, assign each terminal a value
		my $fp = {};
		for my $terminal (@$terminals) {
			my $name = $terminal->get_name;
			my $fpi  = 0;
			do {
				$fpi +=
				  ( $terminal->get_branch_length || 0 ) / $weak_ref{$terminal};
			} while ( $terminal = $terminal->get_parent );
			$fp->{$name} = $fpi;
		}
		return $fp;
	}

=item calc_es() 

Calculates the Equal Splits value for each terminal

 Type    : Calculation
 Title   : calc_es
 Usage   : my $es = $tree->calc_es();
 Function: Returns the Equal Splits value for each terminal
 Returns : HASHREF
 Args    : NONE

=cut

	# code due to Aki Mimoto
	sub calc_es {
		my $self = shift;

		# First establish how many children sit on each of the nodes
		my $terminals = $self->get_terminals;
		my $es        = {};
		for my $terminal ( @{$terminals} ) {
			my $name    = $terminal->get_name;
			my $esi     = 0;
			my $divisor = 1;
			do {
				my $length   = $terminal->get_branch_length || 0;
				my $children = $terminal->get_children      || [];
				$divisor *= @$children || 1;
				$esi += $length / $divisor;
			} while ( $terminal = $terminal->get_parent );
			$es->{$name} = $esi;
		}
		return $es;
	}

=item calc_pe()

Calculates the Pendant Edge value for each terminal.

 Type    : Calculation
 Title   : calc_pe
 Usage   : my $es = $tree->calc_pe();
 Function: Returns the Pendant Edge value for each terminal
 Returns : HASHREF
 Args    : NONE

=cut

	# code due to Aki Mimoto
	sub calc_pe {
		my $self = shift;
		my $terminals = $self->get_terminals or return {};
		my $pe =
		  { map { $_->get_name => $_->get_branch_length } @{$terminals} };
		return $pe;
	}

=item calc_shapley()

Calculates the Shapley value for each terminal.

 Type    : Calculation
 Title   : calc_shapley
 Usage   : my $es = $tree->calc_shapley();
 Function: Returns the Shapley value for each terminal
 Returns : HASHREF
 Args    : NONE

=cut

	# code due to Aki Mimoto
	sub calc_shapley {
		my $self = shift;

		# First find out how many tips are at the ends of each edge.
		my $terminals   = $self->get_terminals or return;    # nothing to see!
		my $edge_lookup = {};
		my $index       = $terminals->[0];

		# Iterate through the edges and find out which side each terminal reside
		_calc_shapley_traverse( $index, undef, $edge_lookup, 'root' );

		# At this point, it's possible to create the calculation matrix
		my $n = @$terminals;
		my @m;
		my $edges = [ keys %$edge_lookup ];
		for my $e ( 0 .. $#$edges ) {
			my $edge = $edges->[$e];
			my $el =
			  $edge_lookup->{$edge};    # Lookup for terminals on one edge side
			my $v =
			  keys %{ $el
				  ->{terminals} };  # Number of elements on one side of the edge
			for my $l ( 0 .. $#$terminals ) {
				my $terminal = $terminals->[$l];
				my $name     = $terminal->get_name;
				if ( $el->{terminals}{$name} ) {
					$m[$l][$e] = ( $n - $v ) / ( $n * $v );
				}
				else {
					$m[$l][$e] = $v / ( $n * ( $n - $v ) );
				}
			}
		}

		# Now we can calculate through the matrix
		my $shapley = {};
		for my $l ( 0 .. $#$terminals ) {
			my $terminal = $terminals->[$l];
			my $name     = $terminal->get_name;
			for my $e ( 0 .. $#$edges ) {
				my $edge = $edge_lookup->{ $edges->[$e] };
				$shapley->{$name} += $edge->{branch_length} * $m[$l][$e];
			}
		}
		return $shapley;
	}

	sub _calc_shapley_traverse {

		# This does a depth first traversal to assign the terminals
		# to the outgoing side of each branch.
		my ( $index, $previous, $edge_lookup, $direction ) = @_;
		return unless $index;
		$previous ||= '';

		# Is this element a root?
		my $is_root = !$index->get_parent;

		# Now assemble all the terminal datapoints and use the soft reference
		# to keep track of which end the terminals are attached
		my @core_terminals;
		if ( $previous and $index->is_terminal ) {
			push @core_terminals, $index->get_name;
		}
		my $parent = $index->get_parent || '';
		my @child_terminals;
		my $child_nodes = $index->get_children || [];
		for my $child (@$child_nodes) {
			next unless $child ne $previous;
			push @child_terminals,
			  _calc_shapley_traverse( $child, $index, $edge_lookup, 'tip' );
		}
		my @parent_terminals;
		if ( $parent ne $previous ) {
			push @parent_terminals,
			  _calc_shapley_traverse( $parent, $index, $edge_lookup, 'root' );
		}

# We're going to toss the root node and we need to merge the root's child branches
		unless ($is_root) {
			$edge_lookup->{$index} = {
				branch_length => $index->get_branch_length,
				terminals     => {
					map { $_ => 1 } @core_terminals,
					$direction eq 'root' ? @parent_terminals : @child_terminals
				}
			};
		}
		return ( @core_terminals, @child_terminals, @parent_terminals );
	}

=back

=head2 VISITOR METHODS

The following methods are a - not entirely true-to-form - implementation of the Visitor
design pattern: the nodes in a tree are visited, and rather than having an object
operate on them, a set of code references is used. This can be used, for example, to
serialize a tree to a string format. To create a newick string without branch lengths
you would use something like this (there is a more powerful 'to_newick' method, so this
is just an example):

 $tree->visit_depth_first(
	'-pre_daughter'   => sub { print '('             },	
	'-post_daughter'  => sub { print ')'             },	
	'-in'             => sub { print shift->get_name },
	'-pre_sister'     => sub { print ','             },	
 );
 print ';';

=over

=item visit_depth_first()

Visits nodes depth first

 Type    : Visitor method
 Title   : visit_depth_first
 Usage   : $tree->visit_depth_first( -pre => sub{ ... }, -post => sub { ... } );
 Function: Visits nodes in a depth first traversal, executes subs
 Returns : $tree
  Args    : Optional handlers in the order in which they would be executed on an internal node:
			
			# first event handler, is executed when node is reached in recursion
			-pre            => sub { print "pre: ",            shift->get_name, "\n" },

			# is executed if node has a daughter, but before that daughter is processed
			-pre_daughter   => sub { print "pre_daughter: ",   shift->get_name, "\n" },
			
			# is executed if node has a daughter, after daughter has been processed	
			-post_daughter  => sub { print "post_daughter: ",  shift->get_name, "\n" },

			# is executed whether or not node has sisters, if it does have sisters
			# they're processed first	
			-in             => sub { print "in: ",             shift->get_name, "\n" },
			
			# is executed if node has a sister, before sister is processed
			-pre_sister     => sub { print "pre_sister: ",     shift->get_name, "\n" },	
			
			# is executed if node has a sister, after sister is processed
			-post_sister    => sub { print "post_sister: ",    shift->get_name, "\n" },							
			
			# is executed last			
			-post           => sub { print "post: ",           shift->get_name, "\n" },
			
			# specifies traversal order, default 'ltr' means first_daugher -> next_sister
			# traversal, alternate value 'rtl' means last_daughter -> previous_sister traversal
			-order          => 'ltr', # ltr = left-to-right, 'rtl' = right-to-left
 Comments: 

=cut

	sub visit_depth_first {
		my $self = shift;
		my %args = looks_like_hash @_;
		$self->get_root->visit_depth_first(%args);
		return $self;
	}

=item visit_breadth_first()

Visits nodes breadth first

 Type    : Visitor method
 Title   : visit_breadth_first
 Usage   : $tree->visit_breadth_first( -pre => sub{ ... }, -post => sub { ... } );
 Function: Visits nodes in a breadth first traversal, executes handlers
 Returns : $tree
 Args    : Optional handlers in the order in which they would be executed on an internal node:
			
			# first event handler, is executed when node is reached in recursion
			-pre            => sub { print "pre: ",            shift->get_name, "\n" },
			
			# is executed if node has a sister, before sister is processed
			-pre_sister     => sub { print "pre_sister: ",     shift->get_name, "\n" },	
			
			# is executed if node has a sister, after sister is processed
			-post_sister    => sub { print "post_sister: ",    shift->get_name, "\n" },			
			
			# is executed whether or not node has sisters, if it does have sisters
			# they're processed first	
			-in             => sub { print "in: ",             shift->get_name, "\n" },			
			
			# is executed if node has a daughter, but before that daughter is processed
			-pre_daughter   => sub { print "pre_daughter: ",   shift->get_name, "\n" },
			
			# is executed if node has a daughter, after daughter has been processed	
			-post_daughter  => sub { print "post_daughter: ",  shift->get_name, "\n" },				
			
			# is executed last			
			-post           => sub { print "post: ",           shift->get_name, "\n" },
			
			# specifies traversal order, default 'ltr' means first_daugher -> next_sister
			# traversal, alternate value 'rtl' means last_daughter -> previous_sister traversal
			-order          => 'ltr', # ltr = left-to-right, 'rtl' = right-to-left
 Comments: 

=cut

	sub visit_breadth_first {
		my $self = shift;
		my %args = looks_like_hash @_;
		$self->get_root->visit_breadth_first(%args);
		return $self;
	}

=item visit_level_order()

Visits nodes in a level order traversal.

 Type    : Visitor method
 Title   : visit_level_order
 Usage   : $tree->visit_level_order( sub{...} );
 Function: Visits nodes in a level order traversal, executes sub
 Returns : $tree
 Args    : A subroutine reference that operates on visited nodes.
 Comments:

=cut	

	sub visit_level_order {
		my ( $tree, $sub ) = @_;
		$tree->get_root->visit_level_order($sub);
		return $tree;
	}

=back

=head2 TREE MANIPULATION

=over

=item ultrametricize()

Sets all root-to-tip path lengths equal.

 Type    : Tree manipulator
 Title   : ultrametricize
 Usage   : $tree->ultrametricize;
 Function: Sets all root-to-tip path 
           lengths equal by stretching
           all terminal branches to the 
           height of the tallest node.
 Returns : The modified invocant.
 Args    : NONE
 Comments: This method is analogous to 
           the 'ultrametricize' command
           in Mesquite, i.e. no rate smoothing 
           or anything like that happens, just 
           a lengthening of terminal branches.

=cut

	sub ultrametricize {
		my $tree    = shift;
		my $tallest = 0;
		foreach ( @{ $tree->get_terminals } ) {
			my $path_to_root = $_->calc_path_to_root;
			if ( $path_to_root > $tallest ) {
				$tallest = $path_to_root;
			}
		}
		foreach ( @{ $tree->get_terminals } ) {
			my $newbl =
			  $_->get_branch_length + ( $tallest - $_->calc_path_to_root );
			$_->set_branch_length($newbl);
		}
		return $tree;
	}

=item scale()

Scales the tree to the specified height.

 Type    : Tree manipulator
 Title   : scale
 Usage   : $tree->scale($height);
 Function: Scales the tree to the 
           specified height.
 Returns : The modified invocant.
 Args    : $height = a numerical value 
           indicating root-to-tip path length.
 Comments: This method uses the 
           $tree->calc_tree_height method, and 
           so for additive trees the *average* 
           root-to-tip path length is scaled to
           $height (i.e. some nodes might be 
           taller than $height, others shorter).

=cut

	sub scale {
		my ( $tree, $target_height ) = @_;
		my $current_height = $tree->calc_tree_height;
		my $scaling_factor = $target_height / $current_height;
		foreach ( @{ $tree->get_entities } ) {
			my $bl = $_->get_branch_length;
			if ($bl) {
				my $new_branch_length = $bl * $scaling_factor;
				$_->set_branch_length($new_branch_length);
			}
		}
		return $tree;
	}

=item resolve()

Randomly breaks polytomies.

 Type    : Tree manipulator
 Title   : resolve
 Usage   : $tree->resolve;
 Function: Randomly breaks polytomies by inserting 
           additional internal nodes.
 Returns : The modified invocant.
 Args    :
 Comments:

=cut

	sub resolve {
		my $tree = shift;
		for my $node ( @{ $tree->get_internals } ) {
			my @children = @{ $node->get_children };
			if ( scalar @children > 2 ) {
				my $i = 1;
				while ( scalar @children > 2 ) {
					my $newnode = Bio::Phylo::Forest::Node->new(
						'-branch_length' => 0.00,
						'-name'          => 'r' . $i++,
					);
					$tree->insert($newnode);
					$newnode->set_parent($node);
					for ( 1 .. 2 ) {
						my $i = int( rand( scalar @children ) );
						$children[$i]->set_parent($newnode);
						splice @children, $i, 1;
					}
					push @children, $newnode;
				}
			}
		}
		return $tree;
	}

=item prune_tips()

Prunes argument nodes from invocant.

 Type    : Tree manipulator
 Title   : prune_tips
 Usage   : $tree->prune_tips(\@taxa);
 Function: Prunes specified taxa from invocant.
 Returns : A pruned Bio::Phylo::Forest::Tree object.
 Args    : A reference to an array of taxon names.
 Comments:

=cut

	sub prune_tips {
		my ( $self, $tips ) = @_;
		if ( blessed $tips ) {
			my @tmp = map { $_->get_name } @{ $tips->get_entities };
			$tips = \@tmp;
		}
		my %names_to_delete = map { $_ => 1 } @{ $tips };
		my %names_to_keep;
		for my $tip ( @{ $self->get_entities } ) {
		    my $name = $tip->get_internal_name;
		    if ( not $names_to_delete{$name} ) {
		        $names_to_keep{$name} = 1;
		    }
		}
        $self->visit_depth_first(
            '-post' => sub {
                my $node = shift;
                for my $tip ( @{ $node->get_terminals } ) {
                    if ( not $names_to_keep{ $tip->get_internal_name } ) {
                        $tip->get_parent->prune_child( $tip );
                        $self->delete( $tip );
                    }
                }
                $self->remove_unbranched_internals;
            }
        );
		$self->remove_unbranched_internals;	
		return $self;
	}

=item keep_tips()

Keeps argument nodes from invocant (i.e. prunes all others).

 Type    : Tree manipulator
 Title   : keep_tips
 Usage   : $tree->keep_tips(\@taxa);
 Function: Keeps specified taxa from invocant.
 Returns : The pruned Bio::Phylo::Forest::Tree object.
 Args    : An array ref of taxon names or a Bio::Phylo::Taxa object
 Comments:

=cut

	sub keep_tips {
		my ( $tree, $tips ) = @_;
		if ( blessed $tips ) {
			my @tmp = map { $_->get_name } @{ $tips->get_entities };
			$tips = \@tmp;
		}
		my %keep_taxa = map { $_ => 1 } @{ $tips };
		my @taxa_to_prune;
		for my $tip ( @{ $tree->get_entities } ) {
		    my $name = $tip->get_internal_name;
		    push @taxa_to_prune, $name if not exists $keep_taxa{$name};
		}
		return $tree->prune_tips( \@taxa_to_prune );
	}

=item negative_to_zero()

Converts negative branch lengths to zero.

 Type    : Tree manipulator
 Title   : negative_to_zero
 Usage   : $tree->negative_to_zero;
 Function: Converts negative branch 
           lengths to zero.
 Returns : The modified invocant.
 Args    : NONE
 Comments:

=cut

	sub negative_to_zero {
		my $tree = shift;
		foreach my $node ( @{ $tree->get_entities } ) {
			my $bl = $node->get_branch_length;
			if ( $bl && $bl < 0 ) {
				$node->set_branch_length(0);
			}
		}
		return $tree;
	}

=item exponentiate()

Raises branch lengths to argument.

 Type    : Tree manipulator
 Title   : exponentiate
 Usage   : $tree->exponentiate($power);
 Function: Raises branch lengths to $power.
 Returns : The modified invocant.
 Args    : A $power in any of perl's number formats.

=cut

	sub exponentiate {
		my ( $tree, $power ) = @_;
		if ( !looks_like_number $power ) {
			throw 'BadNumber' => "Power \"$power\" is a bad number";
		}
		else {
			foreach my $node ( @{ $tree->get_entities } ) {
				my $bl = $node->get_branch_length;
				$node->set_branch_length( $bl**$power );
			}
		}
		return $tree;
	}

=item log_transform()

Log argument base transform branch lengths.

 Type    : Tree manipulator
 Title   : log_transform
 Usage   : $tree->log_transform($base);
 Function: Log $base transforms branch lengths.
 Returns : The modified invocant.
 Args    : A $base in any of perl's number formats.

=cut

	sub log_transform {
		my ( $tree, $base ) = @_;
		if ( !looks_like_number $base ) {
			throw 'BadNumber' => "Base \"$base\" is a bad number"; 
		}
		else {
			foreach my $node ( @{ $tree->get_entities } ) {
				my $bl = $node->get_branch_length;
				my $newbl;
				eval { $newbl = ( log $bl ) / ( log $base ); };
				if ($@) {
					throw 'OutOfBounds' => "Invalid input for log transform: $@";
				}
				else {
					$node->set_branch_length($newbl);
				}
			}
		}
		return $tree;
	}

=item remove_unbranched_internals()

Collapses internal nodes with fewer than 2 children.

 Type    : Tree manipulator
 Title   : remove_unbranched_internals
 Usage   : $tree->remove_unbranched_internals;
 Function: Collapses internal nodes 
           with fewer than 2 children.
 Returns : The modified invocant.
 Args    : NONE
 Comments:

=cut

	sub remove_unbranched_internals {
		my $self = shift;
		for my $node ( @{ $self->get_internals } ) {
			my @children = @{ $node->get_children };
			if ( scalar @children == 1 ) {
				my $child = $children[0];
				$child->set_parent( $node->get_parent );
				my $child_bl = $children[0]->get_branch_length;
				my $node_bl  = $node->get_branch_length;
				if ( defined $child_bl ) {
					if ( defined $node_bl ) {
						$child->set_branch_length( $child_bl + $node_bl );
					}
					else {
						$child->set_branch_length($child_bl);
					}
				}
				else {
					$child->set_branch_length($node_bl) if defined $node_bl;
				}
				$self->delete($node);
			}
		}
		return $self;
	}

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
 Args    : Optional: a hash of code references to 
           override reflection-based getter/setter copying

           my $clone = $object->clone(  
               'set_forest' => sub {
                   my ( $self, $clone ) = @_;
                   for my $forest ( @{ $self->get_forests } ) {
                       $clone->set_forest( $forest );
                   }
               },
               'set_matrix' => sub {
                   my ( $self, $clone ) = @_;
                   for my $matrix ( @{ $self->get_matrices } ) {
                       $clone->set_matrix( $matrix );
                   }
           );

 Comments: Cloning is currently experimental, use with caution.
           It works on the assumption that the output of get_foo
           called on the invocant is to be provided as argument
           to set_foo on the clone - such as 
           $clone->set_name( $self->get_name ). Sometimes this 
           doesn't work, for example where this symmetry doesn't
           exist, or where the return value of get_foo isn't valid
           input for set_foo. If such a copy fails, a warning is 
           emitted. To make sure all relevant attributes are copied
           into the clone, additional code references can be 
           provided, as in the example above. Typically, this is
           done by overrides of this method in child classes.

=cut

	sub clone {
		my $self = shift;
		$logger->info("cloning $self");
		my %subs = @_;
		
		# override, because we'll handle insert
		$subs{'set_root'}      = sub {};
		$subs{'set_root_node'} = sub {};
				
		# we'll clone node objects, so no raw copying
		$subs{'insert'} = sub {
			my ( $self, $clone ) = @_;
			my %clone_of;
			for my $node ( @{ $self->get_entities } ) {
				my $cloned_node = $node->clone;
				$clone_of{ $node->get_id } = $cloned_node;
				$clone->insert( $cloned_node );
			}
			for my $node ( @{ $self->get_entities } ) {
				my $cloned_node = $clone_of{ $node->get_id };
				if ( my $parent = $node->get_parent ) {
					my $cloned_parent_node = $clone_of{ $parent->get_id };
					$cloned_node->set_parent( $cloned_parent_node );
				}
			}
		};
		
		return $self->SUPER::clone(%subs);
	
	} 

=back

=head2 SERIALIZERS

=over

=item to_newick()

Serializes invocant to newick string.

 Type    : Stringifier
 Title   : to_newick
 Usage   : my $string = $tree->to_newick;
 Function: Turns the invocant tree object 
           into a newick string
 Returns : SCALAR
 Args    : NONE

=cut

	sub to_newick {
		my $self   = shift;
		my %args   = @_;
		my $newick = unparse( -format => 'newick', -phylo => $self, %args );
		return $newick;
	}

=item to_xml()

Serializes invocant to xml.

 Type    : Serializer
 Title   : to_xml
 Usage   : my $xml = $obj->to_xml;
 Function: Turns the invocant object into an XML string.
 Returns : SCALAR
 Args    : NONE

=cut

	sub to_xml {
		my $self = shift;
		my $xsi_type = 'nex:IntTree';
		for my $node ( @{ $self->get_entities } ) {
			my $length = $node->get_branch_length;
			if ( defined $length and $length !~ /^[+-]?\d+$/ ) {
				$xsi_type = 'nex:FloatTree';
			}
		}
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $xml = $self->get_xml_tag;
		if ( my $root = $self->get_root ) {
			$xml .= $root->to_xml;
		}
		$xml .= sprintf( "\n</%s>", $self->get_tag );
		return $xml;		
	}

=item to_svg()

Serializes invocant to SVG.

 Type    : Serializer
 Title   : to_svg
 Usage   : my $svg = $obj->to_svg;
 Function: Turns the invocant object into an SVG string.
 Returns : SCALAR
 Args    : Same args as the Bio::Phylo::Treedrawer constructor
 Notes   : This will only work if you have the SVG module
           from CPAN installed on your system.

=cut

	sub to_svg {
	    my $self = shift;
		my $drawer = $fac->create_drawer(@_);
		$drawer->set_tree($self);
	    return $drawer->draw;
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
        if ( my $root = $self->get_root ) {
            return '{' . $self->_to_json() . ',"root":' . $root->to_json() . '}';
        }
        else {
            return $self->SUPER::to_json;
        }
    }

=item to_dom()

 Type    : Serializer
 Title   : to_dom
 Usage   : $tree->to_dom($dom)
 Function: Generates a DOM subtree from the invocant
           and its contained objects
 Returns : an Element object
 Args    : DOM factory object

=cut

    sub to_dom {
		my ($self, $dom) = @_;
		$dom ||= $Bio::Phylo::Util::DOM::DOM;
		unless (looks_like_object $dom, _DOMCREATOR_) {
		    throw 'BadArgs' => 'DOM factory object not provided';
		}
		my $xsi_type = 'nex:IntTree';
		for my $node ( @{ $self->get_entities } ) {
		    my $length = $node->get_branch_length;
		    if ( defined $length and $length !~ /^[+-]?\d+$/ ) {
				$xsi_type = 'nex:FloatTree';
		    }
		}
		$self->set_attributes( 'xsi:type' => $xsi_type );
		my $elt = $self->get_dom_elt($dom);
		if ( my $root = $self->get_root ) {
		    $elt->set_child( $_ ) for $root->to_dom($dom);
		}
		return $elt;
    }

=begin comment

 Type    : Internal method
 Title   : _cleanup
 Usage   : $trees->_cleanup;
 Function: Called during object destruction, for cleanup of instance data
 Returns : 
 Args    :

=end comment

=cut

	sub _cleanup {
		my $self = shift;
		if ( defined( my $id = $self->get_id ) ) {
			for my $field ( @fields ) {
				delete $field->{$id};
			}			
		}
	}

=begin comment

 Type    : Internal method
 Title   : _consolidate
 Usage   : $tree->_consolidate;
 Function: Does pre-order traversal, only keeps
           nodes seen during traversal in tree,
           in order of traversal
 Returns :
 Args    :

=end comment

=cut

    sub _consolidate {
        my $self = shift;
        my @nodes;
        $self->visit_depth_first( '-pre' => sub { push @nodes, shift } );
        $self->clear;
        $self->insert(@nodes);    
    }

=begin comment

 Type    : Internal method
 Title   : _container
 Usage   : $tree->_container;
 Function:
 Returns : CONSTANT
 Args    :

=end comment

=cut

	sub _container { $CONTAINER_CONSTANT }

=begin comment

 Type    : Internal method
 Title   : _type
 Usage   : $tree->_type;
 Function:
 Returns : CONSTANT
 Args    :

=end comment

=cut

	sub _type { $TYPE_CONSTANT }

=back

=cut

# podinherit_insert_token

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Listable>

The L<Bio::Phylo::Forest::Tree|Bio::Phylo::Forest::Tree> object inherits from
the L<Bio::Phylo::Listable|Bio::Phylo::Listable> object, so the methods defined
therein also apply to trees.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

}
1;

__DATA__
sub get_nodes {
	my $self = shift;
	my $order = 'depth';
	my @nodes;
	if ( @_ ) {
		my %args = @_;
		if ( $args{'-order'} and $args{'-order'} =~ m/^b/ ) {
			$order = 'breadth';
		}
	}
	if ( my $root = $self->get_root ) {	
        if ( $order eq 'depth' ) {      
            $root->visit_depth_first(
                -pre => sub { push @nodes, shift }
            );
        }
        else {
            $root->visit_level_order( sub { push @nodes, shift } ); # XXX bioperl is wrong
        }
	}
	return @nodes;
}

sub set_root {
	my ( $self, $node ) = @_;
	my @nodes = ($node);
	if ( my $desc = $node->get_descendants ) {
		push @nodes, @{ $desc };
	}
	$self->clear;
	$self->insert(@nodes);
	return $node;
}

*set_root_node = \&set_root;

*as_string = \&to_newick;

sub get_root_node{ shift->get_root }

sub number_nodes { shift->calc_number_of_nodes }

sub total_branch_length { shift->calc_tree_length }

sub height {
	my $self = shift;
	my $nodect =  $self->calc_number_of_nodes;
	return 0 if( ! $nodect ); 
	return log($nodect) / log(2);
}

sub id {
	my $self = shift;
	if ( @_ ) {
		$self->set_name(shift);
	}
	return $self->get_name;
}

sub score {
	my $self = shift;
	if ( @_ ) {
		$self->set_score(shift);
	}
	return $self->get_score;
}

sub get_leaf_nodes {
	my $self = shift;
	my $tips = $self->get_terminals;
	if ( $tips ) {
		return @{ $tips };
	}
	return;
}

sub _parse_newick {
	my $self = shift;
	my $newick = join ('', @{ $_[0] } ) . ';';
	my $forest = Bio::Phylo::IO::parse( '-format' => 'newick', '-string' => $newick );
	my $tree = $forest->first;
	my @nodes = @{ $tree->get_entities };
	for my $node ( @nodes ) {
		$self->insert($node);
		$tree->delete($node);
	}
	$tree->DESTROY;
	$forest->DESTROY;
}

sub find_node {
   my $self = shift;
   if( ! @_ ) { 
       $logger->warn("Must request a either a string or field and string when searching");
   }
   my ( $field, $value );
   if ( @_ == 1 ) {
        ( $field, $value ) = ( 'id', shift );
   }
   elsif ( @_ == 2 ) {
        ( $field, $value ) = @_;
        $field =~ s/^-//;
   }
   my @nodes;
   $self->visit(
        sub {
            my $node = shift;
            push @nodes, $node if $node->$field and $node->$field eq $value;
        }
   );
   if ( wantarray) { 
       return @nodes;
   } 
   else { 
       if( @nodes > 1 ) { 
	        $logger->warn("More than 1 node found but caller requested scalar, only returning first node");
       }
       return shift @nodes;
   }   
}

sub verbose {
    my ( $self, $level ) = @_;
    $level = 0 if $level < 0;
    $self->VERBOSE( -level => $level );
}

sub reroot {
    my ( $self, $node ) = @_;
    my $id = $node->get_id;
    my $new_root = $node->set_root_below;
    if ( $new_root ) {
        my @children = grep { $_->get_id != $id } @{ $new_root->get_children };
        $node->set_child($_) for @children;
        return 1;    
    }
    else {
        return 0;
    }
}

sub remove_Node {
    my ( $self, $node ) = @_;
    if ( not ref $node ) {
        ($node) = grep { $_->get_name eq $node } @{ $self->get_entities };
    }
    if ( $node->is_terminal ) {
        $node->get_parent->prune_child( $node );
    }
    else {
        $node->collapse;
    }
    $self->delete($node);
}

sub splice {
    my ( $self, @args ) = @_;
    if ( ref($args[0]) ) {
        $_->collapse for @args;
    }
    else {
        my %args = @args;
        my ( @keep, @remove );
        for my $key ( keys %args ) {
            if ( $key =~ /^-keep_(.+)$/ ) {
                my $field = $1;
                my %val;
                if ( ref $args{$key} ) {
                    %val = map { $_ => 1 } @{ $args{$key} };
                }
                else {
                    %val = ( $args{$key} => 1 );
                }
                push @keep, grep { $val{ $_->$field } } @{ $self->get_entities };
            }
            elsif ( $key =~ /^-remove_(.+)$/ ) {
                my $field = $1;
                my %val;
                if ( ref $args{$key} ) {
                    %val = map { $_ => 1 } @{ $args{$key} };
                }
                else {
                    %val = ( $args{$key} => 1 );
                }
                push @remove, grep { $val{ $_->$field } } @{ $self->get_entities };           
            }
        }
        my @netto;
        REMOVE: for my $remove ( @remove ) {
            for my $keep ( @keep ) {
                next REMOVE if $remove->get_id == $keep->get_id;
            }
            push @netto, $remove;
        }
        my @names = map { $_->id } @netto;
        my @keep_names = map { $_->id } @keep;
        if ( @names ) {
            $self->prune_tips(\@names);
        }
        elsif ( @keep_names ) {
            $self->keep_tips( \@keep_names );
        }
    }
}

sub move_id_to_bootstrap {
    my $self = shift;
    $self->visit( 
        sub { 
            my $node = shift; 
            $node->bootstrap( $node->id ) if defined $node->id;
            $node->id("");
        } 
    );
}