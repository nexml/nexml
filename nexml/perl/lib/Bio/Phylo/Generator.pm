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

The generator module is used to simulate trees under various models.

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

	sub _yule_rand_bl {
		my $i = shift;
		return random_exponential( 1, 1 / ( $i + 1 ) );
	}
	sub _hey_rand_bl {
		my $i = shift;
		random_exponential( 1, ( 1 / ( $i * ( $i + 1 ) ) ) );
	}
	
	sub _make_split {
		my ( $i, $parent, $length, $fac ) = @_;
		my @tips;
		for ( 1 .. 2 ) {
			my $node = $fac->create_node( '-name' => "node.$i.$_" );
			$node->set_branch_length($length);
			$node->set_parent($parent);
			push @tips, $node;
		}
		return @tips;
	}
	
	sub gen_rand_pure_birth {
		my $random  = shift;
		my %options = looks_like_hash @_;
		my $model = $options{'-model'};
		if ( $model =~ m/yule/i ) {
			return $random->_gen_pure_birth(
				'-blgen' => \&_yule_rand_bl,
				@_,
			);
		}
		elsif ( $model =~ m/hey/i ) {
			return $random->_gen_pure_birth(
				'-blgen' => \&_hey_rand_bl,
				@_,
			);			
		}
		else {
			throw 'BadFormat' => "model '$model' not implemented";
		}		
	}

	sub _gen_pure_birth {
		my $random  = shift;
		my %options = looks_like_hash @_;
		my $fac = $options{'-factory'} || $factory;
		my $blgen = $options{'-blgen'};

		my $forest = $fac->create_forest;
		for ( 0 .. ( $options{'-trees'} - 1 ) ) {

			# instantiate root node
			my $root = $fac->create_node( '-name' => 'root' );
			$root->set_branch_length(0);
			my %nodes = ( $root->get_id => $root );

			# make the first split, insert new tips in @tips, from
			# which we will draw (without replacement) a new tip
			# to split until we've reached target number
			push my @tips, _make_split(1,$root,$blgen->(1),$fac);

			# start growing the tree
			for my $i ( 2 .. ( $options{'-tips'} - 1 ) ) {

				# draw a random index in @tips
				my $random_tip = int rand scalar @tips;

				# obtain candidate parent of current split
				my $parent = splice @tips, $random_tip, 1; 

				# generate branch length
				my $bl = $blgen->($i);

				# stretch all remaining tips to the present
				for my $tip (@tips) {
					my $oldbl = $tip->get_branch_length;
					$tip->set_branch_length($oldbl + $bl);
					$nodes{$tip->get_id} = $tip;
				}
				
				# add new nodes to tips array
				push @tips, _make_split($i,$parent,$bl,$fac);				
			}
			my $tree = $fac->create_tree;
			$tree->insert(
				map  { $_->[0] }
				sort { $a->[1] <=> $b->[1] }
				map  { [ $_, $_->get_id ] }
				values %nodes
			);
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

	sub _yule_exp_bl {
		my $i = shift;
		return 1 / ( $i + 1 );
	}
	
	sub _hey_exp_bl {
		my $i = shift;
		return 1 / ( $i * ( $i + 1 ) );
	}
	
	sub gen_exp_pure_birth {
		my $random  = shift;
		my %options = looks_like_hash @_;	
		if ( $options{'-model'} =~ m/yule/i ) {
			return $random->_gen_pure_birth(
				'-blgen' => \&_yule_exp_bl,
				@_,
			);
		}
		elsif ( $options{'-model'} =~ m/hey/i ) {
			return $random->_gen_pure_birth(
				'-blgen' => \&_hey_exp_bl,
				@_,
			);			
		}
		else {
			throw 'BadFormat' => "model \"$options{'-model'}\" not implemented";
		}		
	}	


=item gen_equiprobable()

This method draws tree shapes at random, 
such that all shapes are equally probable.

 Type    : Generator
 Title   : gen_equiprobable
 Usage   : my $trees = $gen->gen_equiprobable( '-tips' => 10 );
 Function: Generates an equiprobable tree 
           shape, with branch lengths = 1;
 Returns : A Bio::Phylo::Forest object.
 Args    : -tips  => number of terminal nodes,
           Optional: -trees => number of trees to generate (default: 1),
	   Optional: -factory => a Bio::Phylo::Factory object

=cut
	
	sub _fetch_equiprobable {
		my @tips = @_;
		my $tip_index = int rand scalar @tips;
		my $tip = splice @tips, $tip_index, 1;
		return $tip, @tips;
	}	
	
	sub _fetch_balanced {
		return @_;
	}
	
	sub _fetch_ladder {
		my $tip = pop;
		return $tip, @_;
	}

	sub _gen_simple {
		my $random  = shift;
		my %options = looks_like_hash @_;
		my $fetcher = $options{'-fetcher'};
		my $factory = $options{'-factory'} || $factory;
		my $ntrees  = $options{'-trees'} || 1;
		my $name    = $options{'-name'};
		my $forest  = $factory->create_forest( '-name' => $name );
		for my $i ( 1 .. $ntrees ) {
			my $tree = $factory->create_tree( '-name' => "Tree$i" );
			my ( @tips, @nodes );
			
			# each iteration, we will remove two "tips" from this
			# and add their newly created parent to it
			push @tips, $factory->create_node(
				'-name' => "Tip$i",
				'-branch_length' => 1,
			) for ( 1 .. $options{'-tips'} );			
			
			# this stays above 0 because the root ends up in it
			while( @tips > 1 ) {
				my $parent = $factory->create_node(
					'-name' => "Node$i",
					'-branch_length' => 1,
				);
				$tree->insert($parent);
				for ( 1 .. 2 ) {
					my $tip;
					( $tip, @tips ) = $fetcher->(@tips);
					$tree->insert($tip->set_parent($parent));
				}
				
				# the parent becomes a new candidate tip
				push @tips, $parent;
			}
			$forest->insert($tree);
		}
		return $forest;
	}	
	
	sub gen_equiprobable {
		return _gen_simple(
			@_,
			'-fetcher' => \&_fetch_equiprobable,
			'-name'    => 'Equiprobable',
		);
	}			

=item gen_balanced()

This method creates the most balanced topology possible given the number of tips

 Type    : Generator
 Title   : gen_balanced
 Usage   : my $trees = $gen->gen_balanced( '-tips'  => 10 );
 Function: Generates the most balanced topology
           possible, with branch lengths = 1;
 Returns : A Bio::Phylo::Forest object.
 Args    : -tips  => number of terminal nodes,
           Optional: -trees => number of trees to generate (default: 1),
	   Optional: -factory => a Bio::Phylo::Factory object

=cut

	sub gen_balanced {
		return _gen_simple(
			@_,
			'-fetcher' => \&_fetch_balanced,
			'-name'    => 'Balanced',
		);
	}

=item gen_ladder()

This method creates a ladder tree for the number of tips

 Type    : Generator
 Title   : gen_ladder
 Usage   : my $trees = $gen->gen_ladder( '-tips'  => 10 );
 Function: Generates the least balanced topology
           (a ladder), with branch lengths = 1;
 Returns : A Bio::Phylo::Forest object.
 Args    : -tips  => number of terminal nodes,
           Optional: -trees => number of trees to generate (default: 1),
	   Optional: -factory => a Bio::Phylo::Factory object

=cut

	sub gen_ladder {
		return _gen_simple(
			@_,
			'-fetcher' => \&_fetch_ladder,
			'-name'    => 'Ladder',
		);
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
