package Bio::Phylo::Parsers::Nexml;
use strict;
use XML::Twig;
use Bio::Phylo::IO;
use Bio::Phylo::Util::Exceptions 'throw';
use Bio::Phylo::Factory;
use UNIVERSAL 'isa';
use Data::Dumper;
use vars qw(@ISA $VERSION);
@ISA = qw(Bio::Phylo::IO);

=head1 NAME

Bio::Phylo::Parsers::Nexml - Parses nexml data. No serviceable parts inside.

=head1 DESCRIPTION

This module parses nexml data. It is called by the L<Bio::Phylo::IO> facade,
don't call it directly.

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The newick parser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to parse nexml (or any other data Bio::Phylo supports).

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual> and L<http://rutgervos.blogspot.com>.

=item L<http://www.nexml.org>

For more information about the nexml data standard, visit L<http://www.nexml.org>

=back

=head1 REVISION

 $Id$

=cut

# The factory object, to instantiate Bio::Phylo objects
my $factory = Bio::Phylo::Factory->new;

# We re-use the core Bio::Phylo version number.
$VERSION = $Bio::Phylo::VERSION;

# I factored the logging methods in Bio::Phylo (debug, info,
# warning, error, fatal) out of the inheritance tree and put
# them in a separate logging object.
my $logger = Bio::Phylo::Util::Logger->new;

# helper method to add parser reading position to log messages
sub _pos {
	my $self = shift;
	my $t    = $self->{'_twig'};
	join ':', ( $t->current_line, $t->current_column, $t->current_byte );
}

# nice 'n' generic: we provide an element and a class,
# from the class we instantiate a new object, we set
# the element id in the generic slot of the object.
# If the element has a label, use that as name,
# otherwise use id. Additional constructor args can
# be specified using named arguments, e.g. -type => 'dna'
sub _obj_from_elt {
	my ( $self, $elt, $class, %args ) = @_;

	# factory object handles instantiation (and class loading)
	# see Bio::Phylo::Factory
	my $method = "create_$class";
	my $obj = $factory->$method( %args );

	# TODO move id to xml writable property?
	my $id = $elt->att('id');
	$obj->set_name($id);

	# TODO move label to xml writable property?
	my $label = $elt->att('label');
	$obj->set_desc($label) if $label;

	# TODO move dict to xml writable property?
	for my $dict_elt ( $elt->children('dict') ) {
		my $dict_hash = $self->_process_dictionary($dict_elt);
		$obj->set_generic( 'dict' => $dict_hash );
	}
	my $tag = $elt->tag;
	$logger->debug( $self->_pos . " processed <$tag id=\"$id\"/>" );
	return ( $obj, $id );
}

# this is the constructor that gets called by Bio::Phylo::IO,
# here we create the object instance that will process the file/string
sub _new {
	my $class = shift;
	$logger->debug("instantiating $class");

	# this is the actual parser object, which needs to hold a reference
	# to the XML::Twig object, to a hash of processed blocks (for fast lookup by id)
	# and an array of ids (to preserve processing order)
	my $self = _init( bless {}, $class );

	# here we put the two together, i.e. create the actual XML::Twig object
	# with its handlers, and create a reference to it in the parser object
	$self->{'_twig'} = XML::Twig->new( 
		'TwigHandlers' => {
			'otus'       => sub { &_handle_otus(   @_, $self ) },
			'characters' => sub { &_handle_chars(  @_, $self ) },
			'trees'      => sub { &_handle_forest( @_, $self ) },			
		}		
	);
	return $self;
}

# initialize/reset parser object, called
# by the constructor (_new), and at the
# end of each parser call
sub _init {
	my $hash = shift;
	$hash->{'_blocks'}        = [];
	$hash->{'_taxa'}          = {};
	$hash->{'_taxon_in_taxa'} = {};
	return $hash;
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
	$self->_init;

	return $ordered_blocks;
}

# element handler
sub _handle_otus {
	my ( $twig, $taxa_elt, $self ) = @_;
		
	# instantiate taxa object, push on stack of blocks to return to user
	my ( $taxa_obj, $taxa_id ) = $self->_obj_from_elt( $taxa_elt, 'taxa' );
	push @{ $self->{'_blocks'} }, $taxa_obj;
	
	# create lookup to find taxa object for if other blocks refer to it
	$self->{'_taxa'}->{$taxa_id} = $taxa_obj;
	
	# create lookup to find contained taxon objects if other elements refer to it
	$self->{'_taxon_in_taxa'}->{$taxa_id} = {};
	
	# process contained otu elements
	for my $taxon_elt ( $taxa_elt->children('otu') ) {
		
		# instantiate taxon object, insert in taxa object
		my ( $taxon_obj, $taxon_id ) = $self->_obj_from_elt( $taxon_elt, 'taxon' );
		$taxa_obj->insert($taxon_obj);
		
		# add reference for later lookup
		$self->{'_taxon_in_taxa'}->{$taxa_id}->{$taxon_id} = $taxon_obj;		
	}
	
	$logger->info( $self->_pos . " Processed block id: $taxa_id" );
}

# again, nice 'n' generic: we provide an element, which must have an
# otu attribute; an object that is to be linked to a taxon; the otus
# attribute value of the containing element. Because $self->{_otus}
# collects a hash of hashes keyed on otus_idref => otu_idref we can
# then fetch the appropriate taxon
sub _set_otu_for_obj {
	my ( $self, $elt, $obj, $taxa_idref ) = @_;
	
	# some elements (tree nodes) only optionally refer to otu elements
	if ( my $taxon_idref = $elt->att('otu') ) {
				
		# referenced element must precede reference, hence $taxon_obj must be true
		if ( my $taxon_obj = $self->{'_taxon_in_taxa'}->{$taxa_idref}->{$taxon_idref} ) {
			$obj->set_taxon($taxon_obj);
		}
		
		# if not, throw exception - invalid data
		else {
			throw(
				'API'  => "no OTU '$taxon_idref' in block '$taxa_idref'",
				'line' => $self->{'_twig'}->parser->current_line
			);
		}
	}
	
	# notify user
	else {
		$logger->info( $self->_pos . " no taxon idref" );
	}
}

# same thing, but for taxa objects
sub _set_otus_for_obj {
	my ( $self, $elt, $obj ) = @_;
	
	# linking to otus elements is not optional!
	if ( my $taxa_idref = $elt->att('otus') ) {
		
		# referenced element must precede reference
		if ( my $taxa_obj = $self->{'_taxa'}->{$taxa_idref} ) {
			$obj->set_taxa( $self->{'_taxa'}->{$taxa_idref} );
			return $taxa_idref;
		}
		
		# throw if $taxa_obj hasn't been created yet: invalid data
		else {
			throw(
				'API'  => "no taxa object '$taxa_idref'",
				'line' => $self->{'_twig'}->parser->current_line
			);
		}
	}
	
	# throw if there is no reference
	else {
		throw(
			'API'  => "no taxa reference",
			'line' => $self->{'_twig'}->parser->current_line
		);
	}	
}

sub _handle_chars {
	my ( $twig, $characters_elt, $self ) = @_;
	$logger->debug( $self->_pos . " going to parse characters element" );

	# create matrix object, send extra constructor args
	my $type = $characters_elt->att('xsi:type');
	$type =~ s/^nex:(.*?)(?:Cells|Seqs)/$1/;
	my %args = ( '-type' => $type );
	my ( $matrix_obj, $matrix_id ) = $self->_obj_from_elt( $characters_elt, 'matrix', %args );
	my $taxa_idref = $self->_set_otus_for_obj( $characters_elt, $matrix_obj );

	# create character definitions, if any
	my ( $def_hash, $def_array ) = ( {}, [] );
	if ( my $definitions_elt = $characters_elt->first_child('format') ) {
		( $def_hash, $def_array ) = $self->_process_definitions($definitions_elt);
	}

	# create row objects
	# rows are actually stored inside the <matrix/> element
	my $matrix_elt = $characters_elt->first_child('matrix');
	my ( $row_obj, $chars_hash );
	for my $row_elt ( $matrix_elt->children('row') ) {
		( $row_obj, $chars_hash ) = $self->_process_row( $row_elt, $def_hash, $def_array, %args );
		my @chars;
		if ( @{$def_array} ) {
			my $missing = $row_obj->get_missing;
			for my $def_id ( @{$def_array} ) {
				if ( exists $chars_hash->{$def_id} ) {
					push @chars, $chars_hash->{$def_id};
				}
				else {
					push @chars, $missing;
				}
			}
		}
		else {
			my $highest_pos_for_this_row = ( sort { $a <=> $b } keys %{ $chars_hash } )[-1];
			my $missing = $row_obj->get_missing;
			for my $i ( 0 .. $highest_pos_for_this_row ) {
				if ( exists $chars_hash->{$i} ) {
					push @chars, $chars_hash->{$i};
				}
				else {
					push @chars, $missing;
				}
			}
		}
		$logger->debug( $self->_pos . " set char: '@chars'" );
		$row_obj->set_char( \@chars );
		$self->_set_otu_for_obj( $row_elt, $row_obj, $taxa_idref );
		$matrix_obj->insert($row_obj);
	}
	push @{ $self->{'_blocks'} }, $matrix_obj;
}

# here we create a hash keyed on column ids => state ids => state symbols
sub _process_definitions {
	my ( $self, $format_elt ) = @_;
	my ( $states_hash, $chars_hash, $states_array ) = ( {}, {}, [] );

	# here we iterate over state set definitions, i.e. each
	# $states_elt <states/> describes a set of mappings
	for my $states_elt ( $format_elt->children('states') ) {
		my $states_id = $states_elt->att('id');
		$states_hash->{$states_id} = {};		

		# here we iterate of state definitions, i.e. each
		# $state_elt <state/> describes what symbol that state has,
		# TODO and possible ambiguity mappings
		for my $state_elt ( $states_elt->children('state') ) {
			my $state_id  = $state_elt->att('id');
			my $state_sym = $state_elt->att('symbol');

			# for continuous data, $state_sym is undefined
			$states_hash->{$states_id}->{$state_id} = $state_sym;
		}
	}
	
	# finally, we iterate over column definitions which may
	# reuse state sets.
	for my $char_elt ( $format_elt->children('char') ) {
		my $char_id = $char_elt->att('id');
		my $states_idref = $char_elt->att('states');	
		
		# $states_idref can be false (which in this case is always
		# the same as undefined, because xml id's cannot be integers,
		# so an id of "0" is impossible). This would be the case if
		# the characters element is for continuous characters, which
		# can have column definitions, but not state sets (which would
		# have to be of infinite size).
		if ( $states_idref ) {
			$chars_hash->{$char_id} = $states_hash->{$states_idref};
		}
		
		# in order to keep characters ordered (including in sparse 
		# matrices) we can't just use a hash, need an array as
		# well
		push @$states_array, $char_id;
	}

	return ( $chars_hash, $states_array );
}

sub _process_row {
	my ( $self, $row_elt, $def_hash, $def_array, %args ) = @_;

	# create datum object
	my ( $row_obj, $row_id ) = $self->_obj_from_elt( $row_elt, 'datum', %args );
	
	# check granularity, process accordingly
	if ( $row_elt->children('cell') ) {
		return ( $row_obj, $self->_process_cells( $row_elt, $def_hash, $def_array ) );		
	}
	else {
		return ( $row_obj, $self->_process_seqs( $row_elt, $def_hash, $args{'-type'} ) );
	}

}

sub _process_cells {
	my ( $self, $row_elt, $def_hash, $def_array ) = @_;
	my $chars_hash = {};
	
	# loop over <cell/> elements
	my $i = 0;
	for my $cell_elt ( $row_elt->children('cell') ) {
		my $char_idref  = $cell_elt->att('char');
		my $state_idref = $cell_elt->att('state');
		if ( not defined $char_idref ) {
			$char_idref = $i++;
		}
		my $state;

		# may not exist for types without format block
		if ( exists $def_hash->{$char_idref} ) {
			my $lookup = $def_hash->{$char_idref};

			# may not be a hash for continuous states
			if ( isa( $lookup, 'HASH' ) and defined $lookup->{$state_idref} ) {
				$state = $lookup->{$state_idref};
			}
			else {
				$state = $state_idref;
			}
		}
		else {
			$state = $state_idref;
		}
		$chars_hash->{$char_idref} = $state;
	}
	return $chars_hash;	
}

sub _process_seqs {
	my ( $self, $row_elt, $def_hash, $type ) = @_;	
	my $chars_hash = {};
	my @seq_list;	
	
	if ( my $seq_string = $row_elt->first_child_text('seq') ) {
		if ( $type =~ m/^(DNA|RNA|PROTEIN|RESTRICTION)/i ) {    
			$seq_string =~ s/\s//g;
			@seq_list = split //, $seq_string;			  
		}
		else {
			@seq_list = split /\s+/, $seq_string;
		}
		for my $i ( 0 .. $#seq_list ) {    
			$chars_hash->{$i} = $seq_list[$i];
		}		
	}

	return $chars_hash;		
}



sub _handle_forest {
	my ( $twig, $trees_elt, $self ) = @_;

	# instantiate forest object, set id, taxa and name
	my @args = ( $trees_elt, 'forest' );
	my ( $forest_obj, $forest_id ) = $self->_obj_from_elt( @args );
	my $taxa_idref = $self->_set_otus_for_obj( $trees_elt, $forest_obj );

	# loop over tree elements
	for my $tree_elt ( $trees_elt->children('tree') ) {

		# for now we can only process true trees, not networks,
		# which would require extensions to the Bio::Phylo API
		my $type = $tree_elt->att('xsi:type');
		if ( $type =~ qr/Tree$/ ) {

			# instantiate the tree object, set name and id
			@args = ( $tree_elt, 'tree' );
			my ( $tree_obj, $tree_id ) = $self->_obj_from_elt( @args );

			# things to pass to process methods
			@args = ( $tree_elt, $tree_obj, $taxa_idref );

			$forest_obj->insert( $self->_process_listtree(@args) );

		}

		# TODO fixme
		else {
			$logger->warn( $self->_pos . " Can't process networks yet" );
		}

	}
	push @{ $self->{'_blocks'} }, $forest_obj;
}

sub _process_listtree {
	my ( $self, $tree_elt, $tree_obj, $taxa_idref ) = @_;
	my $tree_id = $tree_elt->att('id');

	# this is going to be our lookup to get things back by id
	my ( %node_by_id, %parent_of );

	# loop over nodes
	for my $node_elt ( $tree_elt->children('node') ) {
		my ( $node_obj, $node_id ) =
		  $self->_obj_from_elt( $node_elt, 'node' );
		$node_by_id{$node_id} = $node_obj;
		$self->_set_otu_for_obj( $node_elt, $node_obj, $taxa_idref );
		$tree_obj->insert($node_obj);
	}

	# loop over branches
	for my $edge_elt ( $tree_elt->children('edge') ) {
		my $node_id   = $edge_elt->att('target');
		my $parent_id = $edge_elt->att('source');
		my $edge_id   = $edge_elt->att('id');

		# referential integrity check for target
		if ( not exists $node_by_id{$node_id} ) {
			throw(
				'API'  => "no target '$node_id' for edge '$edge_id' in tree '$tree_id'",
				'line' => $self->{'_twig'}->parser->current_line
			);
		}

		# referential integrity check for source
		if ( not exists $node_by_id{$parent_id} ) {
			throw(
				'API'  => "no source '$parent_id' for edge '$edge_id' in tree '$tree_id'",
				'line' => $self->{'_twig'}->parser->current_line
			);
		}

		$node_by_id{$node_id}->set_parent( $node_by_id{$parent_id} );
		if ( defined( my $length = $edge_elt->att('length') ) ) {
			$node_by_id{$node_id}->set_branch_length($length);
		}

	}

	# tree structure integrity check
	my $orphan_count = 0;
	for my $node_id ( keys %node_by_id ) {
		$orphan_count++ if not $node_by_id{$node_id}->get_parent;
	}
	if ( $orphan_count == 0 ) {
		throw(
			'API'  => "tree '$tree_id' has reticulations",
			'line' => $self->{'_twig'}->parser->current_line
		);
	}
	if ( $orphan_count > 1 ) {
		throw(
			'API'  => "tree '$tree_id' has too many orphans",
			'line' => $self->{'_twig'}->parser->current_line
		);
	}

	return $tree_obj;
}

sub _process_listnode {
	my ( $self, $node_elt, $taxa_idref ) = @_;

	# instantiate internal node, set id and label
	my ( $node_obj, $node_id ) =
	  $self->_obj_from_elt( $node_elt, 'node' );
	my $parent_id = $node_elt->att('parent');

	# link to taxon
	if ( $node_elt->tag eq 'terminal' ) {
		$self->_set_otu_for_obj( $node_elt, $node_obj, $taxa_idref );
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
	my $dict     = {};
	my @children = $dict_elt->children;

	# loop over items two at a time, i.e. key/value
	for ( my $i = 0 ; $i <= $#children ; $i += 2 ) {
		my $key        = $children[$i]->text;
		my $value      = $children[ $i + 1 ];
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
			for my $name (@names) {
				$any->{$name} = $value->att($name);
			}
			$dict->{$key} = [ $any, $value ];
		}

		# nested dictionary, recurse
		elsif ( $value_type eq 'dict' ) {
			$dict->{$key} = [ 'dict', $self->_process_dictionary($value) ];
		}

		# nested dictionary list, recurse
		elsif ( $value_type eq 'dictvector' ) {
			my @dicts;
			push @dicts, $self->_process_dictionary($_) for $value->children;
			$dict->{$key} = [ 'dictvector', \@dicts ];
		}
	}
	return $dict;
}
1;
