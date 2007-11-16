use strict;
use warnings;
use lib '../lib';    # TODO delete me, eventually
use Bio::Phylo::IO 'parse';
use Test::More 'no_plan';
use Bio::Phylo::Util::Logger;
Bio::Phylo::Util::Logger->VERBOSE(
	-level => 3,
	-class => 'Bio::Phylo::Parsers::Nexml'
);
use XML::Twig;
use Data::Dumper;

my $XML_PATH = '../../examples';    # TODO fixme

# here we just parse a file with only taxon elements
my $taxa = parse( -format => 'nexml', -file => "$XML_PATH/taxa.xml" )->[0];

# check the ids for the children
my @ids      = qw(t1 t2 t3 t4 t5);
my @children = @{ $taxa->get_entities };
for my $i ( 0 .. $#children ) {
	ok( $ids[$i] eq $children[$i]->get_generic('id'), "$ids[$i]" );
}

# here we parse a file with taxon elements and a trees element
my $blocks    = parse( -format => 'nexml', -file => "$XML_PATH/trees.xml" );
my $forest    = $blocks->[1];
my %internals = map { $_ => 1 } qw(n1 n3 n4 n7);
my %terminals = map { $_ => 1 } qw(n2 n5 n6 n8 n9);
my %parent_of = (
	'n3' => 'n1',
	'n4' => 'n3',
	'n7' => 'n3',
	'n2' => 'n1',
	'n5' => 'n4',
	'n6' => 'n4',
	'n8' => 'n7',
	'n9' => 'n7',
);
my %taxon_of = (
	'n2' => 't1',
	'n5' => 't3',
	'n6' => 't2',
	'n8' => 't5',
	'n9' => 't4',
);
for my $tree ( @{ $forest->get_entities } ) {

	for my $node ( @{ $tree->get_entities } ) {
		my $id = $node->get_generic('id');
		if ( $id eq 'n4' ) {
			my $dict  = $node->get_generic('dict');
			my $value = $dict->{'has_tag'};
			ok( $value->[0] eq 'boolean', "dict value type is boolean" );
			ok( $value->[1], "dict boolean value is true" );
		}
		if ( $node->is_internal ) {
			ok( exists $internals{$id}, "$id is an internal node" );
		}
		else {
			ok( exists $terminals{$id}, "$id is a terminal node" );
			ok( $node->get_taxon->get_name eq $taxon_of{$id},
				"taxon if $id is $taxon_of{$id}" );
		}
		if ( my $parent = $node->get_parent ) {
			my $parent_id = $parent->get_generic('id');
			ok( $parent_of{$id} eq $parent_id, "$parent_id is parent of $id" );
		}
	}
}
ok( $forest->first->calc_symdiff( $forest->last ) == 0,
	"identical topologies, symdiff == 0" );

# here we parse a file with two character matrices, one continuous, one standard
$blocks = parse( -format => 'nexml', -file => "$XML_PATH/characters.xml" );
my $raw_matrices = {
	'CONTINUOUS' => [
		[
			-1.545414144070023,  -2.3905621575431044,
			-2.9610221833467265, 0.7868662069161243,
			0.22968509237534918
		],
		[
			-1.6259836379710066, 3.649352410850134,
			1.778885099660406,   -1.2580877968480846,
			0.22335354995610862
		],
		[
			-1.5798979984134964, 2.9548251411133157,
			1.522005675256233,   -0.8642016921755289,
			-0.938129801832388
		],
		[
			2.7436692306788086, -0.7151148143399818,
			4.592207937774776,  -0.6898841440534845,
			0.5769509574453064
		],
		[
			3.1060827493657683, -1.0453787389160105,
			2.67416332763427,   -1.4045634106692808,
			0.019890469925520196
		],
	],
	'STANDARD' => [ [ 1, 2 ], [ 2, 2 ], [ 3, 4 ], [ 2, 3 ], [ 4, 1 ], ],
	'DNA' => [	['a','c','g','c','t','c','g','c','a','t','c','g','c','a','t','c','g','c','g','a'],
				['a','c','g','c','t','c','g','c','a','t','c','g','c','a','t','c','g','c','g','a'],
				['a','c','g','c','t','c','g','c','a','t','c','g','c','a','t','c','g','c','g','a'],
			 ],
	'RNA' => [	['a','c','g','c','u','c','g','c','a','u','c','g','c','a','u','c','g','c','g','a'],
			 	['a','c','g','c','u','c','g','c','a','u','c','g','c','a','u','c','g','c','g','a'],
			 	['a','c','g','c','u','c','g','c','a','u','c','g','c','a','u','c','g','c','g','a'],
			 ]
};
for my $block (@$blocks) {
	if ( $block->isa('Bio::Phylo::Taxa') ) {
		ok( $block, "got taxa block" );
		next;
	}
	my $rows = $block->get_entities;
	my $type = uc $block->get_type;
	for my $i ( 0 .. $#{$rows} ) {
		ok( $rows->[$i]->get_taxon->get_name eq 't' . ( $i + 1 ),
			"found linked taxon" );
		if ( exists $raw_matrices->{$type} ) {
			my @chars = $rows->[$i]->get_char;
			for my $j ( 0 .. $#chars ) {
				if ( ($type eq 'CONTINUOUS') || ($type eq 'STANDARD') ){
					ok($chars[$j] == $raw_matrices->{$type}->[$i]->[$j],
						"value in right cell" );
				}
				else {
					ok($chars[$j] eq $raw_matrices->{$type}->[$i]->[$j],
						"value in right cell" );

				}
			}
		}
	}
}

my $verbose2 = parse( -format => 'nexml', -file => "$XML_PATH/verbose2.xml" );

my $nexml = _to_string ($verbose2->[0]);
my $outfile = 'test_out.xml';
$nexml -> print_to_file ($outfile);

# my $test_out = parse( -format => 'nexml', -file => "$outfile" );
# my $nexml2 = _to_string ($verbose2->[0]);
# $nexml -> print_to_file ('test_out2.xml');

# for my $block (@$blocks) {
# 	if ( $block->isa('Bio::Phylo::Taxa') ) {
#       my $nexml = _to_string ($block);
# 		$nexml -> print('indented');
# 	}
#    print "$block\n";
# }

#############################################################
#############################################################
#TODO: Move to unparser
#############################################################
#############################################################
sub _to_string {
   my $taxa_obj = shift;

   my $nexml_twig = XML::Twig -> new;
   $nexml_twig -> set_xml_version ('1.0');
   $nexml_twig -> set_encoding ('ISO-8859-1');
   $nexml_twig -> set_pretty_print ('indented');
   
   my $nexml_root = XML::Twig::Elt -> new(
      'nex:nexml', {
      'xmlns:nex'          => 'http://www.nexml.org/1.0',
      'version'            => '1.0',
      'generator'          => $0,
      'xmlns:xsi'          => 'http://www.w3.org/2001/XMLNSSchema-instance',
      'xsi:schemaLocation' => 'http://www.nexml.org/1.0 ../nexml.xsd',
      'xmlns:tol'          => 'http://tolweb.org',
      }  
   );

   my $taxa_elt = _process_taxa ($taxa_obj);
   $taxa_elt -> paste ($nexml_root);
   
   for my $characters_obj (reverse @{$taxa_obj -> get_matrices}) {
      my $characters_elt = _process_characters ($characters_obj);
      $characters_elt -> paste ('last_child', $nexml_root);
   }
   
   for my $forest_obj (reverse @{$taxa_obj -> get_forests}) {
      my $forest_elt = _process_forest ($forest_obj);
      $forest_elt -> paste ('last_child', $nexml_root);
   }


   $nexml_twig -> set_root ($nexml_root);   
   return $nexml_twig;
}

sub _elt_from_obj {
   # TODO: Consider making accept %args?
   my ($obj, $elt_type) = @_;
   my $name = $obj -> get_name;  
   my $elt = XML::Twig::Elt -> new(
      $elt_type,
      {'label'    =>  $name,}
      );
   if (my $generic = $obj -> get_generic) {
      if (exists $generic -> {'id'}) {
         $elt -> set_att ('id' => $generic -> {'id'});
      }
      if (exists $generic -> {'dict'}) {
         _process_dictionary ($elt, $generic -> {'dict'});
      }
   }  
   return $elt;
}

#TODO: Fix this once you figure out how these things are stored :)
sub _process_dictionary {
   my ($elt, $generic) = @_;
   
   my $dict_elt = XML::Twig::Elt -> new ('dict');
   for my $key (keys %$generic) {
      my $value_type = $generic -> {$key} -> [0];
      my $value = $generic -> {$key} -> [1];
      #Determine type of value stored in hash
      if (my $ref_type = ref $value) {
         my $key_elt = XML::Twig::Elt -> new ('key', $key);
         # TODO: All html values were stored as XML::Twig::Elt.
         # TODO: Make another test datafile to see if all dicts are stored as Twig::Elts.
         $key_elt -> paste ('last_child', $dict_elt);
         #Copying because $value is still part of a tree
         my $value_elt = $value -> copy ($value);
         $value_elt -> paste ('last_child', $dict_elt);
         
      }
      #Not a reference
      else {
         my $key_elt = XML::Twig::Elt -> new ('key', $key);
         $key_elt -> paste ('last_child', $dict_elt);
         my $value_elt = XML::Twig::Elt -> new ($value_type, $value);
         $value_elt -> paste ('last_child', $dict_elt);
      }
   }
   $dict_elt -> paste ('last_child', $elt);
}

sub _set_taxa {
   my ($elt, $obj) = @_;
   if (my $taxa = $obj -> get_taxa) {
      $elt -> set_att ('taxa' => $taxa -> get_name);
   }
   return $elt;
}

sub _set_taxon {
   my ($elt, $obj) = @_;
   if (my $taxon = $obj -> get_taxon) {
      $elt -> set_att ('taxon' => $taxon -> get_name);
   }
   return $elt;
}
     
sub _process_taxa {
   my $taxa_obj = shift;
   
   my $taxa_elt = _elt_from_obj ($taxa_obj, 'taxa');
   for my $taxon_obj (reverse @{$taxa_obj -> get_entities}) {
      my $taxon_elt = _elt_from_obj ($taxon_obj, 'taxon');
      $taxon_elt -> paste ($taxa_elt);
   }
   return $taxa_elt;
}

sub _process_forest {
   my $forest_obj = shift;
   
   my $forest_elt = _elt_from_obj ($forest_obj, 'trees');
   _set_taxa ($forest_elt, $forest_obj);
   for my $tree_obj (reverse @{$forest_obj -> get_entities}) {
      my $tree_elt = _process_listtree ($tree_obj);
      # TODO: Test if tree is rooted first
      $tree_elt -> set_att ('rooted' => '1');
      $tree_elt -> paste ('last_child',$forest_elt);
   }
   return $forest_elt;
}

sub _process_listtree {
   my $tree_obj = shift;
   
   my $tree_elt = _elt_from_obj ($tree_obj, 'tree');
   $tree_elt -> set_att ('xsi:type' => 'nex:ListTree');   

   for my $internal_node (@{$tree_obj -> get_internals}) {
      if ($internal_node -> is_root) {
         my $internal_elt = _process_node_listtree ($internal_node, 'root');
         $internal_elt -> paste ('first_child', $tree_elt);
      }
      else {   
         my $internal_elt = _process_node_listtree ($internal_node, 'internal');
         $internal_elt -> paste ('last_child', $tree_elt);         
      }
   }
   for my $terminal_node (@{$tree_obj -> get_terminals}) {
      my $terminal_elt = _process_node_listtree ($terminal_node, 'terminal');
      _set_taxon ($terminal_elt, $terminal_node);
      $terminal_elt -> paste ('last_child', $tree_elt);
   }
   return $tree_elt;   
}

sub _process_node_listtree {
   my ($node_obj, $tag) = @_;
   if (!$tag) {die "node type not defined for node $node_obj";}
   my $node_elt = _elt_from_obj ($node_obj, $tag);
   if (my $branchlength = $node_obj -> get_branch_length) {
      $node_elt -> set_att ('float' => $branchlength);
   }
   if (my $parent_node = $node_obj -> get_parent) {
      $node_elt -> set_att ('parent' => $parent_node -> get_name);
   }
   # TODO: Find a way to change att order?  Their order is not intuitive.       
   return $node_elt;  
}

sub _process_characters {
   my $characters_obj = shift;
   
   my $characters_elt = _elt_from_obj ($characters_obj, 'characters');
   _set_taxa ($characters_elt, $characters_obj);   
   my $type;
   if ($type = "nex:" . uc $characters_obj -> get_type) {
      $characters_elt -> set_att ('xsi:type' => $type);
   }
   else {die "No characters type specified";}
   my $matrix_elt = XML::Twig::Elt -> new ('matrix', {'aligned' => 'true'});

   for my $row_obj (@{$characters_obj -> get_entities}) {    
       
      my $row_elt = _elt_from_obj ($row_obj, 'row');
      _set_taxon ($row_elt, $row_obj);
      my $seq = $row_obj -> get_char;;
      # TODO: Pretty up string with line breaks
      my $seq_elt = XML::Twig::Elt -> new ('seq', $seq);
      $seq_elt -> paste ($row_elt);
      $row_elt -> paste ('last_child', $matrix_elt);
   }
   
   if ($type !~ m/(dna|rna|protein)$/i) {
       my $definitions_elt = _process_definitions ($characters_obj, $type);
       $definitions_elt -> paste ($characters_elt);
   }
   $matrix_elt -> paste ('last_child', $characters_elt);
   return $characters_elt; 
}

sub _process_definitions {
   my ($characters_obj, $type) = @_;
   my $definitions_elt = XML::Twig::Elt -> new ('definitions');
   #TODO: Fix this awful kludge - horrible run time but fully compatible (I HOPE!)    
   my $raw_data = $characters_obj -> get_raw;
  
   my $states = {};
   
   #$states => {keyed on id_number - using col_number as stand-in} => {keyed on state names} = filler
   for my $row_data (@$raw_data) {
      for my $col_number (1..(@$row_data-1)) {
         my $col_data = $row_data -> [$col_number];       
         $states -> {$col_number} -> {$col_data} = 'no_sym';
      }
   }
   if ($type =~ m/continuous/i) {
      for my $col_id (sort keys %{$states}) {
         my $def_elt = XML::Twig::Elt -> new ('def', {'id' => $col_id});
         $def_elt -> paste ('last_child',$definitions_elt);
      }
   }
   else {
      for my $col_id (sort keys %{$states}) {
         my $def_elt = XML::Twig::Elt -> new ('def', {'id' => $col_id});
         for my $state (sort keys %{$states -> {$col_id}}) {
            my $val_elt = XML::Twig::Elt -> new ('val', {'id' => $state});
            $val_elt -> paste ('last_child', $def_elt);
         }       
         $def_elt -> paste ('last_child',$definitions_elt);
      }
   }
   return $definitions_elt; 
}
