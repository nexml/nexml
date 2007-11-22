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
use Bio::Phylo::Util::Exceptions 'throw';
use Data::Dumper;
use vars qw(@ISA $VERSION);
@ISA = qw(Bio::Phylo::IO);

# We re-use the core Bio::Phylo version number.
$VERSION=$Bio::Phylo::VERSION;

# I factored the logging methods in Bio::Phylo (debug, info,
# warning, error, fatal) out of the inheritance tree and put
# them in a separate logging object. The following code is
# a kludge to deal with both options
my $logger;
eval { require Bio::Phylo::Util::Logger };
if ( $@ ) {
 	$logger = __PACKAGE__;
}
else {
	$logger = Bio::Phylo::Util::Logger->new;
}

# I hate typing, and it's more generic this way, we'll
# instantiate objects not by calling Bio::Phylo::blah->new
# hardcoded every time, but using a generic _obj_from_elt
# method which is called using the current xml element
# and $CLASS{Node} (for example) as arguments.
my %CLASS = (
	'Taxa'   => 'Bio::Phylo::Taxa',
	'Taxon'  => 'Bio::Phylo::Taxa::Taxon',
	'Datum'  => 'Bio::Phylo::Matrices::Datum',
	'Matrix' => 'Bio::Phylo::Matrices::Matrix',
	'Forest' => 'Bio::Phylo::Forest',
	'Node'   => 'Bio::Phylo::Forest::Node',
	'Tree'   => 'Bio::Phylo::Forest::Tree',
);

# nice 'n' generic: we provide an element and a class,
# from the class we instantiate a new object, we set
# the element id in the generic slot of the object.
# If the element has a label, use that as name, 
# otherwise use id. Additional constructor args can
# be specified using named arguments, e.g. -type => 'dna'
sub _obj_from_elt {
	my ( $self, $elt, $class, %args ) = @_;
	my $obj   = $class->new(%args);
	my $id    = $elt->att('id');
	my $label = $elt->att('label');
	$obj->set_name( $id );
	$obj->set_desc( $label ) if $label;
	for my $dict_elt ( $elt->children('dict') ) {
		my $dict_hash = $self->_process_dictionary( $dict_elt );
		$obj->set_generic( 'dict' => $dict_hash );
	}
	$logger->debug("created object of class $class with xml id $id");
	return ( $obj, $id );
}

# gets the current line we're looking at, for informational purposes only
sub _line {
	shift->{'_twig'}->parser->current_line
}

# again, nice 'n' generic: we provide an element, which must have an
# otu attribute; an object that is to be linked to a taxon; the otus
# attribute value of the containing element. Because $self->{_otus}
# collects a hash of hashes keyed on otus_idref => otu_idref we can
# then fetch the appropriate taxon
sub _set_taxon_for_obj {
	my ( $self, $elt, $obj, $taxa_idref ) = @_;
	if ( my $taxon_idref = $elt->att('otu') ) {
		if ( my $taxon_obj = $self->{'_taxon_in_taxa'}->{$taxa_idref}->{$taxon_idref} ) {
			$obj->set_taxon( $taxon_obj );
		}
		else {
			throw 'API' => "no taxon '$taxon_idref' in block '$taxa_idref'", 'line' => $self->{'_twig'}->parser->current_line;
		}
	}
	else {
		die "no taxon idref at line ", $self->{'_twig'}->parser->current_line;
	}
}

# same thing, but for taxa objects
sub _set_taxa_for_obj {
	my ( $self, $elt, $obj ) = @_;
	my $taxa_idref = $elt->att('otus');
	$obj->set_taxa( $self->{'_taxa'}->{$taxa_idref} );
	return $taxa_idref;
}

# this is the constructor that gets called by Bio::Phylo::IO,
# here we create the object instance that will process the file/string
sub _new {
	my $class = shift;
	$logger->debug("instantiating $class");

	# this is the actual parser object, which needs to hold a reference
	# to the XML::Twig object, to a hash of processed blocks (for fast lookup by id)
	# and an array of ids (to preserve processing order)
	my $self = bless {
		'_twig'          => undef,
		'_blocks'        => [],
		'_taxa'          => {}, # we want to find the right taxon to link to
		'_taxon_in_taxa' => {},
	}, $class;
	$logger->debug("created nexml parser object");

	# the handlers need to hold a reference to the parser object $self, so that
	# the handler methods (e.g. _process_otus) have access to $self and
	# they can store processed blocks
	my $handlers = {
		'otus'      =>  sub { &_process_taxa(   @_, $self ) },
		'characters' => sub { &_process_chars(  @_, $self ) },
		'trees'      => sub { &_process_forest( @_, $self ) },
		'#PI'        => \&_process_pi,
	};
	$logger->debug("created xml handlers");

	# here we put the two together, i.e. create the actual XML::Twig object
	# with its handlers, and create a reference to it in the parser object
	$self->{'_twig'} = XML::Twig->new( 'TwigHandlers' => $handlers );
	$logger->debug("instantiated xml parser");
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
	$logger->debug("going to parse xml");
	my %opt = @_;
	
	# XML::Twig doesn't care if we parse from a handle or a string
	my $xml = $opt{'-handle'} || $opt{'-string'};
	$self->{'_twig'}->parse($xml);
	
	# we're done, now order the blocks
	my $ordered_blocks = $self->{'_blocks'};	
	
	# reset everything in its initial state: Bio::Phylo::IO caches parsers
	$self->{'_blocks'}        = [];
	$self->{'_taxa'}          = {}; # nested hash to find otus later
	$self->{'_taxon_in_taxa'} = {};
	# (we don't have to reset the twig, it can be called multiple times)
	
	return $ordered_blocks;
}

# parses processing instructions, for informational purposes only
sub _process_pi {
	my ( $twig, $pi_elt ) = @_;
	throw 'API' => 'pi', 'line' => $twig->parser->current_line;
}

sub _process_taxa {
	my ( $twig, $taxa_elt, $self ) = @_;
	my ( $taxa_obj, $taxa_id ) = $self->_obj_from_elt( $taxa_elt, $CLASS{Taxa} );
	$logger->debug("Taxa id: $taxa_id");
	push @{ $self->{'_blocks'} }, $taxa_obj;
	$self->{'_taxa'}->{$taxa_id} = $taxa_obj;
	$self->{'_taxon_in_taxa'}->{$taxa_id} = {};
	for my $taxon_elt ( $taxa_elt->children('otu') ) {
		my ( $taxon_obj, $taxon_id ) = $self->_obj_from_elt( $taxon_elt, $CLASS{Taxon} );
		$self->{'_taxon_in_taxa'}->{$taxa_id}->{$taxon_id} = $taxon_obj;
		$taxa_obj->insert( $taxon_obj );
	}
}

sub _process_chars {
	my ( $twig, $characters_elt, $self ) = @_;
	$logger->debug("going to parse characters element");
		
	# create matrix object, send extra constructor args
	my $type = $characters_elt->att('xsi:type');
	$type =~ s/^nex:(.*?)(?:Cells|Seqs)/$1/;
	my %args = ( '-type' => $type );
	my ( $matrix_obj, $matrix_id ) = $self->_obj_from_elt( $characters_elt, $CLASS{Matrix}, %args );
	my $taxa_idref = $self->_set_taxa_for_obj( $characters_elt, $matrix_obj );
	
	# TODO do we really need the <matrix/> container?
	# rows are actually stored inside the <matrix/> element
	my $matrix_elt = $characters_elt->first_child('matrix');
	
	# create character definitions, if any
	my ( $def_hash, $def_array ) = ( {}, [] );
	if ( my $definitions_elt = $matrix_elt->first_child('definitions') ) {
		( $def_hash, $def_array ) = $self->_process_definitions( $definitions_elt );
	}
	
	# create row objects
	my ( $row_obj, $chars_hash );
	for my $row_elt ( $matrix_elt->children('row') ) {
		( $row_obj, $chars_hash ) = $self->_process_row( $row_elt, $def_hash, $def_array, %args );
		my @chars;
		if ( @{ $def_array } ) {
			for my $def_id ( @{ $def_array } ) {
				if ( exists $chars_hash->{$def_id} ) {
					push @chars, $chars_hash->{$def_id};
				}
				else {
					push @chars, $row_obj->get_missing;
				}
			}
		}
		else {
			my $highest_pos_for_this_row = ( sort { $a <=> $b } keys %{ $chars_hash} )[-1];
			for my $i ( 0 .. $highest_pos_for_this_row ) {
				if ( exists $chars_hash->{$i} ) {
					push @chars, $chars_hash->{$i};
				}
				else {
					push @chars, $row_obj->get_missing;
				}			
			}
		}
		$logger->debug("set char: '@chars'");
		$row_obj->set_char(\@chars);		
		$self->_set_taxon_for_obj( $row_elt, $row_obj, $taxa_idref );
		
		$matrix_obj->insert($row_obj);	
	}
	push @{ $self->{'_blocks'} }, $matrix_obj;
}

sub _process_row {
	my ( $self, $row_elt, $def_hash, $def_array, %args ) = @_;	
	
	# create datum object
	my ( $row_obj, $row_id ) = $self->_obj_from_elt( $row_elt, $CLASS{Datum}, %args );
	my $chars_hash = {};
	
	# loop over <obs/> elements
	my $i = 0;
	for my $obs_elt ( $row_elt->children('obs') ) {
		my $def_idref = $obs_elt->att('def');
		my $val_idref = $obs_elt->att('val');
		if ( not defined $def_idref ) {
			$def_idref = $i++;
		}
		my $val;
		
		# may not exist for dna
		if ( exists $def_hash->{$def_idref} ) {
			my $lookup = $def_hash->{$def_idref};
				
			# may not be a hash for continuous
			if ( UNIVERSAL::isa( $lookup, 'HASH' ) and defined $lookup->{$val_idref} ) {
				$val = $lookup->{$val_idref} ;
			}
			else {
				$val = $val_idref;
			}
		}
		else {
			$val = $val_idref;
		}
		$chars_hash->{$def_idref} = $val;
	}
	
	# TODO: this doesn't always work, see commit messages
	if (my $seq_string = $row_elt->first_child_text('seq')){
		if ( $args{-type} =~ m/^(DNA|RNA|PROTEIN)/i ){	# more lax here, do we really wanna fail if it's lower case?	
			$seq_string =~ s/\s//g;
			my @seq_list = split //, $seq_string; # no parentheses around built-ins, see "Perl Best Practices" & Perl::Critic
			for my $i ( 0 .. $#seq_list ) { # C-style for-loop not necessary, can just use range operator
				$chars_hash->{$i} = $seq_list[$i];
			}
		}
		else {
			my @seq_list = split /\s+/, $seq_string;
			for my $i ( 0 .. $#seq_list ) {
				my $def_idref = $def_array->[$i];
				my $val_idref = $seq_list[$i];
				if ( not defined $def_idref ) {
					$def_idref = $i++;
				}
				my $val;
				
				# molecular data should never get here, so do not handle
				my $lookup = $def_hash->{$def_idref};
					
				# may not be a hash for continuous
				if ( UNIVERSAL::isa( $lookup, 'HASH' ) and defined $lookup->{$val_idref} ) {
					$val = $lookup->{$val_idref};
					# XXX: because some rows may have <obs/> cells, there may need to be a $lookup
					# for those cells, which have a string val="$val_idref" attribute. However
					# <seq/> rows will have the raw state data (down here these would be integers
					# because the only way we could get here is with STANDARD data, I think), 
					# hence $lookup may be a 'HASH', but $lookup->{$val_idref} will never be 
					# defined so we can put a die without it ever being visited. 					
					die "We never get here, because <seq/> holds raw data, there is no $lookup->{$val_idref}!";					
				}
				else {
					$val = $val_idref;
				}

				$chars_hash->{$def_idref} = $val;		
			}		
	
		}
	}
    
    return ( $row_obj, $chars_hash );
}

# here we create a hash keyed on column ids => state ids => state symbols
sub _process_definitions {
	my ( $self, $definitions_elt ) = @_;
	my ( $def_hash, $def_array ) = ( {}, [] );
	
	# here we iterate over character definitions, i.e. each
	# $def_elt describes a matrix column
	for my $def_elt ( $definitions_elt->children('def') ) {
		my $def_id = $def_elt->att('id');
		$def_hash->{$def_id} = {};
		push @$def_array, $def_id;
		
		# here we iterate of state definitions, i.e. each
		# $val_elt describes what states that column may occupy
		for my $val_elt ( $def_elt->children('val') ) {
			my $val_id = $val_elt->att('id');
			my $val_sym = $val_elt->att('sym');
			
			# for continuous data, $val_sym is undefined
			$def_hash->{$def_id}->{$val_id} = $val_sym;
		}
	}
	
	return ( $def_hash, $def_array );
}

sub _process_forest {
	my ( $twig, $trees_elt, $self ) = @_;

	# instantiate forest object, set id, taxa and name	
	my ( $forest_obj, $forest_id ) = $self->_obj_from_elt( $trees_elt, $CLASS{Forest} );
	my $taxa_idref = $self->_set_taxa_for_obj( $trees_elt, $forest_obj );
	
	# loop over tree elements
	for my $tree_elt ( $trees_elt->children('tree') ) {
		
		# instantiate the tree object, set name and id
		my ( $tree_obj, $tree_id ) = $self->_obj_from_elt( $tree_elt, $CLASS{Tree} );	
		
		# there is always a root, so might as well instantiate that
		my $root_elt = $tree_elt->first_child('root');
		my ( $root_obj, $root_id ) = $self->_obj_from_elt( $root_elt, $CLASS{Node} );
	
		# can insert really at any time, nice to have it first in the list though
		$tree_obj->insert($root_obj);	
		
		# things to pass to process methods
		my @args = ( $tree_elt, $tree_obj, $root_obj, $root_id, $taxa_idref );
		
		# now dispatch based on xsi:type
		my $type = $tree_elt->att('xsi:type');
		if ( $type eq 'nex:NestedTree' ) {
			$forest_obj->insert( $self->_process_nestedtree( @args ) );
		}
		elsif ( $type eq 'nex:ListTree' ) {
			$forest_obj->insert( $self->_process_listtree( @args ) );
		}
		else {
			die "Can't handle xsi:type '$type'";
		}
	}
	push @{ $self->{'_blocks'} }, $forest_obj;
}

# this is just the entry point for the recursive _process_nestednode
# method below
sub _process_nestedtree { 
	my ( $self, $tree_elt, $tree_obj, $root_obj, $root_id, $taxa_idref ) = @_;
	$self->_process_nestednode( $tree_obj, $tree_elt->first_child('root'), $root_obj, $taxa_idref );
	return $tree_obj;
}

# beware, here we're recursing
sub _process_nestednode {
	my ( $self, $tree_obj, $node_elt, $node_obj, $taxa_idref ) = @_;
	
	# link to taxon
	if ( $node_elt->tag eq 'terminal' ) {
		$self->_set_taxon_for_obj( $node_elt, $node_obj, $taxa_idref );
	}	
	
	# get all the child elements of focal element with name 'internal' or 'terminal'
	my @child_elts = ( $node_elt->children('internal'), $node_elt->children('terminal') );
	
	# iterate over child elements
	for my $child_elt ( @child_elts ) {
		
		# instantiate child node objects, set branch length
		my ( $child_obj, $child_id ) = $self->_obj_from_elt( $child_elt, $CLASS{Node} );
		my $branch_length;
		if ( defined $child_elt->att('float') ) {
			$branch_length = $child_elt->att('float');
		}
		elsif ( defined $child_elt->att('int') ) {
			$branch_length = $child_elt->att('int');
		}
		$child_obj->set_branch_length( $branch_length ) if defined $branch_length;
		
		# we're now going one level deeper, once we've set the parent, and inserted
		# the node
		$child_obj->set_parent( $node_obj );
		$tree_obj->insert( $child_obj );
		
		# same thing all over again, one generation deeper
		$self->_process_nestednode( $tree_obj, $child_elt, $child_obj, $taxa_idref );
	}
}

sub _process_listtree {
	my ( $self, $tree_elt, $tree_obj, $root_obj, $root_id, $taxa_idref ) = @_;

	# this is going to be our lookup to get things back by id
	my ( %node_by_id, %parent_of );
	$node_by_id{$root_id} = $root_obj;
	
	# loop over nodes
	my @node_elts = ( $tree_elt->children('internal'), $tree_elt->children('terminal') );
	for my $node_elt ( @node_elts ) {
		my ( $node_obj, $node_id, $parent_id ) = $self->_process_listnode( $node_elt, $taxa_idref );
		$node_by_id{$node_id} = $node_obj;
		$parent_of{$node_id} = $parent_id;
	}
	
	# here's where the magic happens!
	for my $id ( keys %node_by_id ) {
		my $node_obj = $node_by_id{$id};
		if ( my $parent = $node_by_id{ $parent_of{$id} } ) {
			$node_obj->set_parent( $parent );
		}
		#TODO: Double check my logic here.  Root was appearing twice in tree
		if ($id eq $root_id) {
   		next;
		}
		#End my addition
		$tree_obj->insert( $node_obj );
	}	
	
	return $tree_obj;
}

sub _process_listnode {
	my ( $self, $node_elt, $taxa_idref ) = @_;

	# instantiate internal node, set id and label
	my ( $node_obj, $node_id ) = $self->_obj_from_elt( $node_elt, $CLASS{Node} );
	my $parent_id = $node_elt->att('parent');
	
	# link to taxon
	if ( $node_elt->tag eq 'terminal' ) {
		$self->_set_taxon_for_obj( $node_elt, $node_obj, $taxa_idref );
	}

	# always test for defined-ness on branch lengths! could be 0
	my $branch_length;
	if ( defined $node_elt->att('float') ) {
		$branch_length = $node_elt->att('float');
	}
	
	# TODO should really be mutually exclusive in schema, but isn't
	elsif ( defined $node_elt->att('integer') ) {
		$branch_length = $node_elt->att('integer');
	}
	if ( defined $branch_length ) {
		$node_obj->set_branch_length($branch_length);
	}
	
	return $node_obj, $node_id, $parent_id;
}

# this method is called from within _obj_from_elt
# to process dictionary key/value pairs embedded 
# in an element that maps onto a Bio::Phylo object
sub _process_dictionary { 
	my ( $self, $dict_elt ) = @_;
	my $dict = {};
	my @children = $dict_elt->children;
	
	# loop over items two at a time, i.e. key/value
	for ( my $i = 0; $i <= $#children; $i += 2 ) {
		my $key = $children[$i]->text;
		my $value = $children[ $i + 1 ];
		my $value_type = $value->tag;
		
		# simple types, no any or nested dict
		if ( $value_type !~ qr/^(?:dict|any)/ ) {
			my $value_text = $value->text;
			
			# simple value
			if ( $value_type !~ qr/vector$/ ) {
				$dict->{$key} = [ $value_type, $value_text ];
			}
			
			# vector value
			else {
				$dict->{$key} = [ $value_type, [ split /\s+/, $value_text ] ];
			}
		}
		
		# any type can have arbitrary attributes
		elsif ( $value_type eq 'any' ) {
			my @names = $value->att_names;
			
			# store attributes, e.g. xmlns:h
			my $any = {};
			for my $name ( @names ) {
				$any->{$name} = $value->att($name);
			}
			$dict->{$key} = [ $any, $value ];
		}
		
		# nested dictionary, recurse
		elsif ( $value_type eq 'dict' ) {
			$dict->{$key} = [ 'dict', $self->_process_dictionary( $value ) ];
		}
		
		# nested dictionary list, recurse
		elsif ( $value_type eq 'dictvector' ) {
			my @dicts;
			push @dicts, $self->_process_dictionary( $_ ) for $value->children;
			$dict->{$key} = [ 'dictvector', \@dicts ];
		}
	}
	return $dict;
}
1;
