# $Id: Nexus.pm 4213 2007-07-15 03:11:27Z rvosa $
# Subversion: $Rev: 190 $
package Bio::Phylo::Unparsers::Nexml;
use strict;
use Bio::Phylo::IO;
use Bio::Phylo::Util::CONSTANT qw(:objecttypes);
use Bio::Phylo::Util::Exceptions 'throw';
use vars qw(@ISA $VERSION);
@ISA = qw(Bio::Phylo::IO);

# One line so MakeMaker sees it.
use Bio::Phylo; $VERSION = $Bio::Phylo::VERSION;

eval { require XML::Twig };
if ( $@ ) {
	throw 'ExtensionError' => "Error loading the XML::Twig extension: $@";
}

=head1 NAME

Bio::Phylo::Unparsers::Nexus - Unparses nexus matrices. No serviceable parts
inside.

=head1 DESCRIPTION

This module turns a L<Bio::Phylo::Matrices::Matrix> object into a nexus
formatted matrix. It is called by the L<Bio::Phylo::IO> facade, don't call it
directly. You can pass the following additional arguments to the unparse call:
	
	# an array reference of matrix, forest and taxa objects:
	-phylo => [ $block1, $block2 ]
	
	# the arguments that can be passed for matrix objects, 
	# refer to Bio::Phylo::Matrices::Matrix::to_nexus:
	-matrix_args => {}

	# the arguments that can be passed for forest objects, 
	# refer to Bio::Phylo::Forest::to_nexus:
	-forest_args => {}

	# the arguments that can be passed for taxa objects, 
	# refer to Bio::Phylo::Taxa::to_nexus:
	-taxa_args => {}	
	
	OR:
	
	# for backward compatibility:
	-phylo => $matrix	

=begin comment

 Type    : Constructor
 Title   : _new
 Usage   : my $nex = Bio::Phylo::Unparsers::Nexus->_new;
 Function: Initializes a Bio::Phylo::Unparsers::Nexus object.
 Returns : A Bio::Phylo::Unparsers::Nexus object.
 Args    : none.

=end comment

=cut

sub _new {
	my $class = shift;
	my $self  = {};
	if (@_) {
		my %opts = @_;
		foreach my $key ( keys %opts ) {
			my $localkey = uc $key;
			$localkey =~ s/-//;
			unless ( ref $opts{$key} ) {
				$self->{$localkey} = uc $opts{$key};
			}
			else {
				$self->{$localkey} = $opts{$key};
			}
		}
	}
	bless $self, $class;
	return $self;
}

=begin comment

 Type    : Wrapper
 Title   : _to_string($matrix)
 Usage   : $nexus->_to_string($matrix);
 Function: Stringifies a matrix object into
           a nexus formatted table.
 Alias   :
 Returns : SCALAR
 Args    : Bio::Phylo::Matrices::Matrix;

=end comment

=cut

sub _to_string {
	my $self       = shift;
	my $taxa_obj   = $self->{'PHYLO'};
	my $nexml_twig = XML::Twig->new;
	$nexml_twig->set_xml_version('1.0');
	$nexml_twig->set_encoding('ISO-8859-1');
	$nexml_twig->set_pretty_print('indented');
	my $nexml_root = XML::Twig::Elt->new(
		'nex:nexml',
		{
			'xmlns:nex'          => 'http://www.nexml.org/1.0',
			'version'            => '1.0',
			'generator'          => $0,
			'xmlns:xsi'          => 'http://www.w3.org/2001/XMLSchema-instance',
			'xsi:schemaLocation' => 'http://www.nexml.org/1.0 ../nexml.xsd',
		}
	);
	my $taxa_elt = _process_taxa($taxa_obj);
	$taxa_elt->paste($nexml_root);

	for my $characters_obj ( reverse @{ $taxa_obj->get_matrices } ) {
		my $characters_elt = _process_characters($characters_obj);
		$characters_elt->paste( 'last_child', $nexml_root );
	}
	for my $forest_obj ( reverse @{ $taxa_obj->get_forests } ) {
		my $forest_elt = _process_forest($forest_obj);
		$forest_elt->paste( 'last_child', $nexml_root );
	}
	$nexml_twig->set_root($nexml_root);
	my $nexml_string = $nexml_twig->sprint('indented');
	return $nexml_string;
}

sub _elt_from_obj {

	# TODO: Consider making accept %args?
	my ( $obj, $elt_type ) = @_;
	my $name = $obj->get_name;
	my $elt = XML::Twig::Elt->new( $elt_type, { 'label' => $name, } );
	if ( my $generic = $obj->get_generic ) {
		if ( exists $generic->{'id'} ) {
			$elt->set_att( 'id' => $generic->{'id'} );
		}
		if ( exists $generic->{'dict'} ) {
			_process_dictionary( $elt, $generic->{'dict'} );
		}
	}
	return $elt;
}

#TODO: Fix this once you figure out how these things are stored :)
sub _process_dictionary {
	my ( $elt, $generic ) = @_;
	my $dict_elt = XML::Twig::Elt->new('dict');
	for my $key ( keys %$generic ) {
		my $value_type = $generic->{$key}->[0];
		my $value      = $generic->{$key}->[1];
		my $key_elt    = XML::Twig::Elt->new( 'key', $key );
		$key_elt->paste( 'last_child', $dict_elt );

		#if it is not a "dict" or "any"
		if ( ( $value_type !~ qr/^dict/ ) && ( !( ref $value_type ) ) ) {
			if ( $value_type =~ qr/vector$/ ) {

				#if it a vector, join the array
				$value = join " ", @$value;
				my $value_elt = XML::Twig::Elt->new( $value_type, $value );
				$value_elt->paste( 'last_child', $dict_elt );
			}
			else {
				my $value_elt = XML::Twig::Elt->new( $value_type, $value );
				$value_elt->paste( 'last_child', $dict_elt );
			}
		}

		#check if it is a "dict"
		elsif ( $value_type eq 'dict' ) {
			my $value_elt =
			  new( $value_type, _process_dictionary( $dict_elt, $value ) );
			$value_elt->paste( 'last_child', $dict_elt );
		}

		#check if it is "dictvector"
		elsif ( $value_type eq 'dictvector' ) {
			for my $dict (@$value) {
				my $value_elt =
				  new( $value_type, _process_dictionary( $dict_elt, $value ) );
				$value_elt->paste( 'last_child', $dict_elt );
			}
		}

		#it is an "any", then it is stored as a Twig:elt
		else {
			my $value_elt = $value->copy($value);
			$value_elt->paste( 'last_child', $dict_elt );
		}
	}
	$dict_elt->paste( 'last_child', $elt );
}

sub _set_taxa {
	my ( $elt, $obj ) = @_;
	if ( my $taxa = $obj->get_taxa ) {
		$elt->set_att( 'taxa' => $taxa->get_name );
	}
	return $elt;
}

sub _set_taxon {
	my ( $elt, $obj ) = @_;
	if ( my $taxon = $obj->get_taxon ) {
		$elt->set_att( 'taxon' => $taxon->get_generic->{'id'} );
	}
	return $elt;
}

sub _process_taxa {
	my $taxa_obj = shift;
	my $taxa_elt = _elt_from_obj( $taxa_obj, 'taxa' );
	for my $taxon_obj ( reverse @{ $taxa_obj->get_entities } ) {
		my $taxon_elt = _elt_from_obj( $taxon_obj, 'taxon' );
		$taxon_elt->paste($taxa_elt);
	}
	return $taxa_elt;
}

sub _process_forest {
	my $forest_obj = shift;
	my $forest_elt = _elt_from_obj( $forest_obj, 'trees' );
	_set_taxa( $forest_elt, $forest_obj );
	for my $tree_obj ( reverse @{ $forest_obj->get_entities } ) {
		my $tree_elt = _process_listtree($tree_obj);

		# TODO: Test if tree is rooted first
		$tree_elt->set_att( 'rooted' => '1' );
		$tree_elt->paste( 'last_child', $forest_elt );
	}
	return $forest_elt;
}

sub _process_listtree {
	my $tree_obj = shift;
	my $tree_elt = _elt_from_obj( $tree_obj, 'tree' );
	$tree_elt->set_att( 'xsi:type' => 'nex:ListTree' );
	for my $internal_node ( @{ $tree_obj->get_internals } ) {
		if ( $internal_node->is_root ) {
			my $internal_elt = _process_node_listtree( $internal_node, 'root' );
			if ( $tree_elt->has_child('dict') ) {
				$internal_elt->paste( after => $tree_elt->first_child );
			}
			else {
				$internal_elt->paste( 'first_child', $tree_elt );
			}
		}
		else {
			my $internal_elt =
			  _process_node_listtree( $internal_node, 'internal' );
			$internal_elt->paste( 'last_child', $tree_elt );
		}
	}
	for my $terminal_node ( @{ $tree_obj->get_terminals } ) {
		my $terminal_elt = _process_node_listtree( $terminal_node, 'terminal' );
		_set_taxon( $terminal_elt, $terminal_node );
		$terminal_elt->paste( 'last_child', $tree_elt );
	}
	return $tree_elt;
}

sub _process_node_listtree {
	my ( $node_obj, $tag ) = @_;
	if ( !$tag ) { die "node type not defined for node $node_obj"; }
	my $node_elt = _elt_from_obj( $node_obj, $tag );
	if ( my $branchlength = $node_obj->get_branch_length ) {
		$node_elt->set_att( 'float' => $branchlength );
	}
	if ( my $parent_node = $node_obj->get_parent ) {
		$node_elt->set_att( 'parent' => $parent_node->get_name );
	}

	# TODO: Find a way to change att order?  Their order is not intuitive.
	return $node_elt;
}

sub _process_characters {
	my $characters_obj = shift;
	my $characters_elt = _elt_from_obj( $characters_obj, 'characters' );
	_set_taxa( $characters_elt, $characters_obj );
	my $type;
	if ( $type = "nex:" . uc $characters_obj->get_type ) {
		$characters_elt->set_att( 'xsi:type' => $type );
	}
	else { die "No characters type specified"; }
	my $matrix_elt = XML::Twig::Elt->new('matrix');
	if ( $type =~ m/(dna|rna|protein)$/i ) {
		$matrix_elt->set_att( 'aligned' => 1 );
	}
	for my $row_obj ( @{ $characters_obj->get_entities } ) {
		my $row_elt = _elt_from_obj( $row_obj, 'row' );
		_set_taxon( $row_elt, $row_obj );
		my $seq = $row_obj->get_char;

		# TODO: Pretty up string with line breaks
		my $seq_elt = XML::Twig::Elt->new( 'seq', $seq );
		$seq_elt->paste( 'last_child', $row_elt );
		$row_elt->paste( 'last_child', $matrix_elt );
	}
	if ( $type !~ m/(dna|rna|protein)$/i ) {
		my $definitions_elt = _process_definitions( $characters_obj, $type );
		$definitions_elt->paste($matrix_elt);
	}
	$matrix_elt->paste( 'last_child', $characters_elt );
	return $characters_elt;
}

sub _process_definitions {
	my ( $characters_obj, $type ) = @_;
	my $definitions_elt = XML::Twig::Elt->new('definitions');

 #TODO: Fix this awful kludge - horrible run time but fully compatible (I HOPE!)
	my $raw_data = $characters_obj->get_raw;
	my $states   = {};

#$states => {keyed on id_number - using col_number as stand-in} => {keyed on state names} = filler
	for my $row_data (@$raw_data) {
		for my $col_number ( 1 .. ( @$row_data - 1 ) ) {
			my $col_data = $row_data->[$col_number];
			$states->{ 'c' . $col_number }->{ 's' . $col_data } = $col_data;
		}
	}
	if ( $type =~ m/continuous/i ) {
		for my $col_id ( sort keys %{$states} ) {
			my $def_elt = XML::Twig::Elt->new( 'def', { 'id' => $col_id } );
			$def_elt->paste( 'last_child', $definitions_elt );
		}
	}
	else {
		for my $col_id ( sort keys %{$states} ) {
			my $def_elt = XML::Twig::Elt->new( 'def', { 'id' => $col_id } );
			for my $state ( sort keys %{ $states->{$col_id} } ) {
				my $val_elt =
				  XML::Twig::Elt->new( 'val',
					{ 'id' => $state, 'sym' => $states->{$col_id}->{$state} } );
				$val_elt->paste( 'last_child', $def_elt );
			}
			$def_elt->paste( 'last_child', $definitions_elt );
		}
	}
	return $definitions_elt;
}

=head1 SEE ALSO

=over

=item L<Bio::Phylo::IO>

The newick unparser is called by the L<Bio::Phylo::IO> object.
Look there to learn how to unparse newick strings.

=item L<Bio::Phylo::Manual>

Also see the manual: L<Bio::Phylo::Manual>.

=back

=head1 REVISION

 $Id: Nexus.pm 4213 2007-07-15 03:11:27Z rvosa $

=cut

1;


