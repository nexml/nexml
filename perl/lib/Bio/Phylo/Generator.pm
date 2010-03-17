# $Id$
package Bio::Phylo::Generator;
use strict;
use Bio::Phylo::Util::CONSTANT 'looks_like_hash';
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Factory;
use Bio::Phylo::Util::Logger;

eval { require Math::Random };
if ( $@ ) {
	throw 'ExtensionError' => "Error loading the Math::Random extension: $@";
}
Math::Random->import('random_exponential');

{

	my $logger  = Bio::Phylo::Util::Logger->new;
	my $factory = Bio::Phylo::Factory->new;

=head1 NAME

Bio::Phylo::Generator - Generator of tree topologies

=head1 SYNOPSIS

 use Bio::Phylo::Factory;
 my $fac = Bio::Phylo::Factory->new;
 my $gen = $fac->create_generator;
 my $trees = $gen->gen_rand_pure_birth( 
     '-tips'  => 10, 
     '-model' => 'yule',
     '-trees' => 10,
 );

 # prints 'Bio::Phylo::Forest'
 print ref $trees;

=head1 DESCRIPTION

The generator module is used to simulate trees under the Yule, Hey, or
equiprobable model.

=head1 METHODS

=head2 CONSTRUCTOR

=over

=item new()

Generator constructor.

 Type    : Constructor
 Title   : new
 Usage   : my $gen = Bio::Phylo::Generator->new;
 Function: Initializes a Bio::Phylo::Generator object.
 Returns : A Bio::Phylo::Generator object.
 Args    : NONE

=cut

	sub new {

		# could be child class
		my $class = shift;

		# notify user
		$logger->info("constructor called for '$class'");

		# the object turns out to be stateless
		my $self = bless \$class, $class;

		return $self;
	}

=back

=head2 GENERATOR

=over

=item gen_rand_pure_birth()

This method generates a Bio::Phylo::Forest 
object populated with Yule/Hey trees.

 Type    : Generator
 Title   : gen_rand_pure_birth
 Usage   : my $trees = $gen->gen_rand_pure_birth(
               '-tips'  => 10, 
               '-model' => 'yule',
               '-trees' => 10,
           );
 Function: Generates markov tree shapes, 
           with branch lengths sampled 
           from a user defined model of 
           clade growth, for a user defined
           number of tips.
 Returns : A Bio::Phylo::Forest object.
 Args    : -tips  => number of terminal nodes,
           -model => either 'yule' or 'hey',
           -trees => number of trees to generate
	   Optional: -factory => a Bio::Phylo::Factory object

=cut

	sub gen_rand_pure_birth {
		my $random  = shift;
		my %options = looks_like_hash @_;
		my $fac = $options{'-factory'} || $factory;
		my ( $yule, $hey );
		if ( $options{'-model'} =~ m/yule/i ) {
			$yule = 1;
		}
		elsif ( $options{'-model'} =~ m/hey/i ) {
			$hey = 1;
		}
		else {
			throw 'BadFormat' => "model \"$options{'-model'}\" not implemented";
		}
		my $forest = $fac->create_forest;
		for ( 0 .. $options{'-trees'} ) {

			# instantiate new tree object
			my $tree = $fac->create_tree;

			# $i = a counter, $bl = branch length
			my ( $i, $bl ) = 1;

			# generate branch length
			if ($yule) {
				$bl = random_exponential( 1, 1 / ( $i + 1 ) );
			}
			elsif ($hey) {
				$bl = random_exponential( 1, ( 1 / ( $i * ( $i + 1 ) ) ) );
			}

			# instantiate root node
			my $root = $fac->create_node( '-name' => 'root' );
			$root->set_branch_length(0);
			$tree->insert($root);

			for ( 1 .. 2 ) {
				my $node = $fac->create_node( '-name' => "node.$i.$_" );
				$node->set_branch_length($bl);
				$tree->insert($node);
				$node->set_parent($root);
			}

			# there are now two tips from which the tree
			# can grow, we store these in the tip array,
			# from which we well randomly draw a tip
			# for the next split.
			my @tips;
			push @tips, @{ $root->get_children };

			# start growing the tree
			for my $i ( 2 .. ( $options{'-tips'} - 1 ) ) {

				# generate branch length
				if ($yule) {
					$bl = random_exponential( 1, 1 / ( $i + 1 ) );
				}
				elsif ($hey) {
					$bl = random_exponential( 1, ( 1 / ( $i * ( $i + 1 ) ) ) );
				}

				# draw a random integer between 0 and
				# the tip array length
				my $j = int rand scalar @tips;

				# dereference to obtain parent of current split
				my $parent = $tips[$j];

				for ( 1 .. 2 ) {
					my $node = $fac->create_node( '-name' => "node.$i.$_" );
					$node->set_branch_length($bl);
					$tree->insert($node);
					$node->set_parent($parent);
				}

				# remove parent from tips array
				splice @tips, $j, 1;

				# stretch all tips to the present
				foreach (@tips) {
					my $oldbl = $_->get_branch_length;
					$_->set_branch_length( $oldbl + $bl );
				}

				# add new nodes to tips array
				push @tips, @{ $parent->get_children };
			}
			$forest->insert($tree);
		}
		return $forest;
	}

=item gen_exp_pure_birth()

This method generates a Bio::Phylo::Forest object 
populated with Yule/Hey trees whose branch lengths 
are proportional to the expected waiting times (i.e. 
not sampled from a distribution).

 Type    : Generator
 Title   : gen_exp_pure_birth
 Usage   : my $trees = $gen->gen_exp_pure_birth(
               '-tips'  => 10, 
               '-model' => 'yule',
               '-trees' => 10,
           );
 Function: Generates markov tree shapes, 
           with branch lengths following 
           the expectation under a user 
           defined model of clade growth, 
           for a user defined number of tips.
 Returns : A Bio::Phylo::Forest object.
 Args    : -tips  => number of terminal nodes,
           -model => either 'yule' or 'hey'
           -trees => number of trees to generate
	   Optional: -factory => a Bio::Phylo::Factory object

=cut

	sub gen_exp_pure_birth {
		my $random  = shift;
		my %options = looks_like_hash @_;
		my $fac = $options{'-factory'} || $factory;
		my ( $yule, $hey );
		if ( $options{'-model'} =~ m/yule/i ) {
			$yule = 1;
		}
		elsif ( $options{'-model'} =~ m/hey/i ) {
			$hey = 1;
		}
		else {
			throw 'BadFormat' => "model \"$options{'-model'}\" not implemented";
		}
		my $forest = $fac->create_forest;
		for ( 0 .. $options{'-trees'} ) {

			# instantiate new tree object
			my $tree = $fac->create_tree;

			# $i = a counter, $bl = branch length
			my ( $i, $bl ) = 1;

			# generate branch length
			if ($yule) {
				$bl = 1 / ( $i + 1 );
			}
			elsif ($hey) {
				$bl = 1 / ( $i * ( $i + 1 ) );
			}

			# instantiate root node
			my $root = $fac->create_node( '-name' => 'root' );
			$root->set_branch_length(0);
			$tree->insert($root);

			# instantiate children
			for ( 1 .. 2 ) {
				my $node = $fac->create_node( '-name' => "node.$i.$_" );
				$node->set_branch_length($bl);
				$tree->insert($node);
				$node->set_parent($root);
			}

			# there are now two tips from which the tree
			# can grow, we store these in the tip array,
			# from which we well randomly draw a tip
			# for the next split.
			my @tips;
			push @tips, @{ $root->get_children };

			# start growing the tree
			for my $i ( 2 .. ( $options{'-tips'} - 1 ) ) {

				# generate branch length
				if ($yule) {
					$bl = 1 / ( $i + 1 );
				}
				elsif ($hey) {
					$bl = 1 / ( $i * ( $i + 1 ) );
				}

				# draw a random integer between 0 and
				# the tip array length
				my $j = int rand scalar @tips;

				# dereference to obtain parent of current split
				my $parent = $tips[$j];

				# instantiate children
				for ( 1 .. 2 ) {
					my $node = $fac->create_node( '-name' => "node.$i.$_" );
					$node->set_branch_length($bl);
					$tree->insert($node);
					$node->set_parent($parent);
				}

				# remove parent from tips array
				splice @tips, $j, 1;

				# stretch all tips to the present
				foreach (@tips) {
					my $oldbl = $_->get_branch_length;
					$_->set_branch_length( $oldbl + $bl );
				}

				# add new nodes to tips array
				push @tips, @{ $parent->get_children };
			}
			$forest->insert($tree);
		}
		return $forest;
	}

=item gen_equiprobable()

This method draws tree shapes at random, 
such that all shapes are equally probable.

 Type    : Generator
 Title   : gen_equiprobable
 Usage   : my $trees = $gen->gen_equiprobable( 
               '-tips'  => 10, 
               '-trees' => 5,
           );
 Function: Generates an equiprobable tree 
           shape, with branch lengths = 1;
 Returns : A Bio::Phylo::Forest object.
 Args    : -tips  => number of terminal nodes,
           -trees => number of trees to generate,
	   Optional: -factory => a Bio::Phylo::Factory object

=cut

	sub gen_equiprobable {
		my $random  = shift;
		my %options = looks_like_hash @_;
		my $fac = $options{'-factory'} || $factory;
		my $forest  = $fac->create_forest( '-name' => 'Equiprobable' );
		for ( 0 .. $options{'-trees'} ) {
			my $tree = $fac->create_tree( '-name' => 'Tree' . $_ );
			for my $i ( 1 .. ( $options{'-tips'} + ( $options{'-tips'} - 1 ) ) )
			{
				my $node = $fac->create_node(
					'-name'          => 'Node' . $i,
					'-branch_length' => 1,
				);
				$tree->insert($node);
			}
			my $nodes   = $tree->get_entities;
			my $parents = $nodes;
			for my $node ( @{$nodes} ) {
			  CHOOSEPARENT: while ( @{$parents} ) {
					my $j      = int rand scalar @{$parents};
					my $parent = $parents->[$j];
					if ( $parent != $node && !$node->is_ancestor_of($parent) ) {
						if ( $parent->is_terminal ) {
							$node->set_parent($parent);
							last CHOOSEPARENT;
						}
						elsif ( scalar @{ $parent->get_children } == 1 ) {
							$node->set_parent($parent);
							splice( @{$parents}, $j, 1 );
							last CHOOSEPARENT;
						}
					}
				}
			}
			$forest->insert($tree);
		}
		return $forest;
	}

=back

=head1 SEE ALSO

=over

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=back

=head1 REVISION

 $Id$

=cut

}
1;
