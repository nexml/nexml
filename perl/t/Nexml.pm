package Bio::Phylo::Parsers::Nexml;
use strict;
use XML::Twig;
use Bio::Phylo::IO;
use Bio::Phylo::Taxa;
use Bio::Phylo::Taxa::Taxon;
use Bio::Phylo::Matrices::Datum;
use Bio::Phylo::Matrices::Matrix;
use Bio::Phylo::Forest;
use Bio::Phylo::Forest::Node;
use Bio::Phylo::Forest::Tree;
use vars '@ISA';
@ISA=qw(Bio::Phylo::IO);

# this is the constructor that gets called by Bio::Phylo::IO,
# here we create the object instance that will process the file/string
sub _new { 
	my $class = shift;
	$class->debug("instantiating $class");
	
	# this is the actual parser object, which needs to hold a reference
	# to the XML::Twig object, to a hash of processed blocks (for fast lookup by id)
	# and an array of ids (to preserve processing order)
	my $self      = bless { 
		'_twig'   => undef, 
		'_blocks' => {},
		'_ids'    => [],
	}, $class;
	$class->debug("created nexml parser object");
	
	# the handlers need to hold a reference to the parser object $self, so that
	# the handler methods (e.g. _process_otus) have access to $self and
	# they can store processed blocks
	my $handlers = { 
		'otus'       => sub { &_process_otus( @_, $self ) },
		'characters' => sub { &_process_characters( @_, $self ) },
		'trees'      => sub { &_process_trees( @_, $self ) },
	};
	$class->debug("created xml handlers");
	
	# here we put the two together, i.e. create the actual XML::Twig object
	# with its handlers, and create a reference to it in the parser object
	$self->{'_twig'} = XML::Twig->new( 'TwigHandlers' => $handlers );
	$class->debug("instantiated xml parser");
	return $self;	
}

# the official interface for Bio::Phylo::IO parser subclasses requires a
# _from_handle method (to process data on a file handle) and a _from_string
# method, for data in a string variable. Since XML::Twig can parse both
# from handle and string with the same XML::Twig->parse method call, we can
# suffice with aliases that point to the same method _from_both
*_from_handle = \&_from_both;
*_from_string = \&_from_both;

# this method will be called by Bio::Phylo::IO, indirectly, through 
# _from_handle if the parse function is called with the -file => $filename 
# argument, or through _from_string if called with the -string => $string
# argument 
sub _from_both {
	my $self = shift;
	$self->debug("going to parse xml");
	my %opt  = @_;
	my $xml  = $opt{'-handle'} || $opt{'-string'};
	$self->{'_twig'}->parse($xml);
	my @ordered_blocks;
	for ( @{ $self->{'_ids'} } ) {
		push @ordered_blocks, $self->{'_blocks'}->{$_};
	}
	return \@ordered_blocks;
}


# This subroutine creates a new Bio::Phylo::Taxa object 
# and populates it with appropriate taxa objects
sub _process_otus {
	my ( $twig, $otus, $self ) = @_;
	$self->debug("processing otus");
	my $otus_id   = $otus->att('id');
	my $otus_name = $otus->att('label');
	
	# Create a new Bio::Phylo::Taxa object
	my $taxa = Bio::Phylo::Taxa->new;
	$taxa->set_name( $otus_name ) if $otus_name;
	$taxa->set_generic( 'id' => $otus_id );		 		
	$self->debug("instantiated taxa object");
   
	# Iterate through otu data, create and populate 
	# Bio::Phylo::Taxon objects to 
	# populate the Taxa object  
	for my $otu ( $otus->children('otu') ) {  
		my $otu_id   = $otu->att('id');
		my $otu_name = $otu->att('label');
		my $taxon = Bio::Phylo::Taxa::Taxon->new;
		$taxon->set_name( $otu_name ) if $otu_name;
		$taxon->set_generic( 'id' => $otu_id );
		$self->debug("instantiated taxon object"); 
		
		# insert taxon in taxa
		$taxa->insert( $taxon );		       
	}
	
	# we store them as a hash so we can easily resolve references
	# later on when other elements refer to otus, but we want to
	# preserve the original ordering also, so we keep an array ref
	# just of the ids
	$self->{'_blocks'}->{ $otus_id } = $taxa;
	push @{ $self->{'_ids'} }, $otus_id;
	
	return $self;
}

sub _process_characters {
	my ( $twig, $characters, $self ) = @_;
	my $characters_id = $characters->att('id');
	
	# build a mapping from state id to state symbol
	# for non DNA characters
	my $lookup = {};
	if ( $characters->att('xsi:type') ne 'DNA' ) {
		for my $def ( $characters->descendants('def') ) {
			my $def_id = $def->att('id');
			$lookup->{ $def_id } = {};
			for my $val ( $def->children('val') ) {
				$lookup->{ $def_id }->{ $val->att('id') } = $val->att('sym');
			}
		}
	}
	# instantiate matrix object
	my $matrix = Bio::Phylo::Matrices::Matrix->new(
		'-type'    => $characters->att('xsi:type'),
		#'-taxa'    => $self->{'_blocks'}->$characters->att('otus'),
		'-generic' => { 'id' => $characters_id },
	);
	if (defined($characters->att('label')))
	{
      $matrix -> set_name ($characters->att('label'));
   }
   
   if ( $characters->att('xsi:type') ne 'DNA' )
   {
	   print "I made a lookup!\n";
      $matrix -> set_generic ('lookup' => $lookup);
   }

	# build a mapping from character id to character position
	my ( $i, $char2index ) = ( 1, {} );
	if ( $characters->att('xsi:type') ne 'DNA' )
	{
	   for my $def ( $characters->descendants('def') ) {
   		$char2index->{ $def->att('id') } = $i++;
	   }
   }
   # DNA datatype lacks a definitions block, so must be processed differently
   else
   {
      my @rows = $characters -> descendants ('row');
      for my $obs ($rows[0] -> descendants ('obs'))
      {
         $char2index->{ $obs ->att('def') } = $i++;
      }
   }
   
   # Store char2index value as a generic for output 
   $matrix -> set_generic ('char2index' => $char2index);
	
	# process matrix rows
	for my $row ( $characters->descendants('row') ) {
		
		# get the taxon object to link to
		my $taxon;
		my $taxon_id = $row->att('otu');

# 		$matrix->get_taxa->visit(
# 			sub {
# 				$taxon = $_[0] if $_[0]->get_generic('id') eq $taxon_id
# 			}
# 		);
		
		# process row observations
		for my $obs ( $row->descendants('obs') ) {
			my $val = $obs->att('val');
			my $def_id = $obs->att('def');
			my $state = $lookup->{$def_id}->{$val};
			# instantiate and insert datum objects
			my $datum =	Bio::Phylo::Matrices::Datum->new(
					'-taxon'   => $taxon,
					'-type'    => $characters->att('xsi:type'),
					'-char'    => defined $state ? $state : $val,
					'-pos'     => $char2index->{ $obs->att('def') },
					'-generic' => { 'id' => $obs->att('id') },
				);
#Temp code until taxon linking is in
			$datum -> set_name ($taxon_id);

			$matrix->insert($datum);

		}
	}
	
	# keep reference to matrix in global hash (identified by id)
	$self->{'_blocks'}->{ $characters_id } = $matrix;
	
	# keep id in order
	push @{ $self->{'_ids'} }, $characters_id;
}
sub _process_trees
{
	my ($twig, $trees, $self) = @_;
	
	my @node_list = ();
	
	my $forest_name = $trees -> att('label');
	my $forest_id = $trees-> att('id');
	
	my $forest = Bio::Phylo::Forest->new;
	$forest->set_name( $forest_name ) if $forest_name;
	$forest->set_generic( 'id' => $forest_id );		 		
	#$self->debug("instantiated forest object");

   
   #Iterate through forest data, create and populate Bio::Phylo::Tree objects to 
   #populate the Forest object  
   foreach my $tree ($trees -> children ('tree'))
   { 
		#Extract attribute information from tree element and create tree
		my $name = $tree -> att('label');
		if ($name)
		{
		 $name = "\"" . $name . "\"";
		} 
		my $id = $tree -> att('id');
		print "$id\n";
		my $type = $tree -> att('xsi:type');
		$type =~ tr/a-z/A-Z/;
		
		my $tree_obj = Bio::Phylo::Forest::Tree->new;
		$tree_obj->set_name( $name ) if $name;
		$tree_obj->set_generic( 'id' => $id,
		                        'type' => $type);    
		
		my @nodes = $tree -> descendants ('node');
		my @roots = $tree -> children ('root');
		
		push (@nodes, @roots);
	   	
	   	
	   	#A hash that will be store references for parent lookups
	   	#so that a node can be found by its ID
	   	my %node_lookup = ();	
	   	
	   	#A hash that will be used to look up parent relationships after tree is parsed
	   	my %parent_hash =();
	   	
	   	#A hash that will contain children information
	   	my %children_hash =();
	   	
	   	
	   	foreach my $node (@nodes)
   		{   	
	   		#Get all dict tags for node
	   		my %properties_hash = ();
	   		foreach my $dict ($node -> children ('dict'))
	   		{
	   			#Get all of the children of the dict
	   			my @dict_children = $dict -> children;
	   			
	   			#Find key-value pairs and add to %properties_hash
	   			#Only go to length - 1 because checking for pairs 
	   			for (my $i = 0; $i <(@dict_children - 1);)
	   			{
	   				my $type = $dict_children[$i] -> tag;
	   				#If child is a "key" add to hash with following  item's value
	   				if ($type eq 'key')
	   				{
	   					my $key = $dict_children[$i] -> text;
	   					my $value = $dict_children[$i+1] -> text;
	   					$properties_hash{$key} = $value;
	   					$i += 2;				
	   				}
	   				#Otherwise, check the next item in list
	   				else
	   				{
	   					$i++;
	   				}
	   			}			
	   		}
   		
	   		#Get remaining node info
	   		my $node_id = $node -> att ('id');
	   		my $node_otu = $node -> att ('otu');
	   		my $branch_length = '';
	   		
	   		#If node has a branch length, pull it out of the hash
	   		if (exists $properties_hash{branchlength})
	   		{
	   			$branch_length = $properties_hash{branchlength};
	   			delete $properties_hash{branchlength};
	   		}
	   
	   #****Strategy here is to create each node without adding parent/child info yet
	   #****This info will be added after tree is parsed fully
	   #****Nodes are stored in a hash keyed on their ID.		
	   				
	   		#If node is a root, don't look for the parent
	   		if ($node -> tag eq 'root')
	   		{
	   			#Record root name

	   			my $tree_node = Bio::Phylo::Forest::Node->new ('-name' => $node_id,
#  															   '-taxon' => $node_otu,
	   											      		'-branch_length' => $branch_length,
	   														   '-generic' => \%properties_hash);
	   			
	   			$node_lookup{$node_id} = $tree_node;
	   			
	   			#If tree is a nested tree, find children
	   			if ($type =~ m/NESTEDTREE/i)
	   			{
	   				my @child_nodes = $node -> children ('node');
	   				my @children = ();
	   				foreach my $child (@child_nodes)
	   				{
	      				push (@children, $child ->att ('id'));
	                }
	      			$children_hash{$node_id} = \@children;
	   			}									  		
	   																	   
	   		}
	   		#Else node isn't a root and must have a parent
	   		else
	   		{
	      		#set parent for list tree style node
	      		if ($type =~ m/LISTTREE/i )
	      		{
	      			my @parent = $node -> children ('parent');
	      			my $parent_id = $parent[0] -> att ('node');
	      			$parent_hash{$node_id} = $parent_id;
	   			}
	   			
	   			#set parent for nested tree style node
	   			elsif ($type =~ m/NESTEDTREE/i )
	   			{
	      			my $parent = $node -> parent;
	      			my $parent_id = $parent -> att ('id');
	      			$parent_hash{$node_id} = $parent_id;
	   			}
	   			
	   
	   			my $tree_node = Bio::Phylo::Forest::Node->new ('-name' => $node_id,
	   #											           '-taxon' => $node_otu,
	   											               '-branch_length' => $branch_length,
	   											               '-generic' => \%properties_hash);
	   		
	   	
	   			$node_lookup{$node_id} = $tree_node;
	   			
	   			#If tree is a nested tree, find children
	   			if ($type =~ m/NESTEDTREE/i)
	   			{
	   				my @child_nodes = $node -> children ('node');
	   				my @children = ();
	   				foreach my $child (@child_nodes)
	   				{
	      				push (@children, $child ->att ('id'));
	                }
	      			$children_hash{$node_id} = \@children;
	   			}									
	   		}  		
   		}
   	
   	#Determine children of each node for list tree
   	if ($type =~/LISTTREE/i)
   	{
      	foreach my $child (keys(%parent_hash))
      	{
      		my $parent = $parent_hash{$child};
      		#Check if this child's parent has already been seen
      		if (exists $children_hash{$parent})
      		{
      			#Add the child to the array of children
      			push (@{$children_hash{$parent}}, $child);
      			
      		}
      		else
      		{
      			my @children_array = ();
      			$children_array[0] = $child;
      			$children_hash{$parent} = \@children_array;
      		}
      	}
    }
    
    #Update all nodes with parent/child info
   	foreach my $node (keys (%node_lookup))
   	{
   		print "Node = $node\tParent = ";
   		if (exists ($parent_hash{$node}))
   		{
   			print "$parent_hash{$node}";
   			$node_lookup{$node} -> set_parent ($node_lookup{$parent_hash{$node}});
   			
   		}
   		print "\tChildren = ";
   		if (exists ($children_hash{$node}))
   		{
   
   			foreach my $child (@{$children_hash{$node}})
   			{
   				print "$child ";
   				$node_lookup{$node} -> set_child ($node_lookup{$child});
   			}
   		}
   		print "\n";
   	}
   	#Add nodes to tree
   	foreach my $node (keys (%node_lookup))
   	{
	   	$tree_obj -> insert ($node_lookup{$node});
   	}
   	  				
  	#Add tree to forest
  	$forest->insert ($tree_obj);
  	
   }
   $self -> {'_blocks'} -> {$forest_id} = $forest;
   push (@{$self -> {'_ids'}}, $forest_id);
   
   return $self;
   
}

sub _process_dictionary {}

1;