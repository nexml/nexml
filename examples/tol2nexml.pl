#!/usr/bin/perl
use strict;
use warnings;
use XML::Twig;
use HTML::Entities;
use URI::Escape;

my $nexml_twig = XML::Twig->new;
$nexml_twig->set_xml_version(      '1.0' );
$nexml_twig->set_encoding(  'ISO-8859-1' );
$nexml_twig->set_pretty_print('indented' );

my $nexml_root = new_elt( 
	'nex:nexml', 
	'version'            => '1.0',
	'generator'          => $0,
	'xmlns:xsi'          => 'http://www.w3.org/2001/XMLSchema-instance',
	'xmlns:xml'          => 'http://www.w3.org/XML/1998/namespace',
	'xmlns:nex'          => 'http://www.nexml.org/1.0', 	
	'xmlns:tol'          => 'http://www.tolweb.org',
	'xmlns:xlink'        => 'http://www.w3.org/1999/xlink',
	'xsi:schemaLocation' => 'http://www.nexml.org/1.0 ../nexml.xsd',
	'xml:base'           => 'http://www.tolweb.org',
);
my $nexml_taxa  = new_elt( 'taxa',  'id' => 'taxa1' );
my $nexml_trees = new_elt( 
	'trees', 
	'id'   => 'trees1', 
	'taxa' => 'taxa1', 
);
my $nexml_tree = new_elt( 
	'tree',  
	'id'       => 'tree1', 
	'xsi:type' => 'nex:ListTree',
	'rooted'   => 1,	
); 

$nexml_tree->paste(    $nexml_trees );
$nexml_trees->paste(   $nexml_root  );
$nexml_taxa->paste(    $nexml_root  );
$nexml_twig->set_root( $nexml_root  );

my $tol_twig = XML::Twig->new(
 	'twig_handlers' => {
 		'NODE'  => \&process_NODE, 		
 	}
);

my $tol_file = shift( @ARGV );
$tol_twig->parsefile( $tol_file );
$nexml_twig->flush;

sub process_NODE {
	my ( $tol_twig, $tol_node ) = @_;
	
	my $name      = encode_entities($tol_node->first_child('NAME')->text) if $tol_node->first_child('NAME');
	my $id        = $tol_node->att('ID');
	my $parent_id = $tol_node->parent('NODES')->parent('NODE')->att('ID') if $tol_node->parent('NODES'); 
	my $is_leaf   = $tol_node->att('LEAF');
	
	my $nexml_node = new_node( $id, $name, $is_leaf, $parent_id );
	attach_metadata( $tol_node, $nexml_node );
	$nexml_node->paste( $nexml_tree );
	
	$tol_node->delete;
	$tol_twig->purge;	
}

sub new_elt {
	my ( $name, %args ) = @_;
	my $elt = XML::Twig::Elt->new( $name );
	$elt->set_att( %args ) if %args;
	return $elt;
}

sub new_node {
	my ( $id, $name, $is_leaf, $parent_id ) = @_;
	my %nexml_node_args = ( 'id' => "n$id" );
	if ( $name ) {
		$nexml_node_args{'label'}    = $name;
		$nexml_node_args{'xlink:href'} = '/' . uri_escape($name) . "/$id";
	}
	my $nexml_node_tag;
	if ( not $parent_id ) {
		$nexml_node_tag = 'root';
	}
	else {
		$nexml_node_args{'parent'} = "n$parent_id";
		if ( not $is_leaf ) {
			$nexml_node_tag = 'internal';
		}
		else {
			$nexml_node_tag = 'terminal';
			$nexml_node_args{'taxon'} = "t$id";
			delete $nexml_node_args{'label'};
			my %nexml_taxon_args = ( 'id' => "t$id" );
			$nexml_taxon_args{'label'} = $name if $name;
			my $nexml_taxon = new_elt( 'taxon', %nexml_taxon_args );
			$nexml_taxon->paste( $nexml_taxa );
		}
	}
	return new_elt( $nexml_node_tag, %nexml_node_args );
}

sub attach_metadata {
	my ( $tol_node, $nexml_node ) = @_;
	my $nexml_dict = new_elt( 'dict' );
	attach_attributes( $tol_node, $nexml_dict );
	attach_elements( $tol_node, $nexml_dict );
	$nexml_dict->paste( $nexml_node ) if $nexml_dict->children;
}

sub attach_attributes {
	my ( $tol_node, $nexml_dict ) = @_;

	my @tol_keys = grep { $_ !~ qr/^(?:ID|LEAF)$/ } $tol_node->att_names;	
	if ( @tol_keys ) {

		my $nexml_attr_key = new_elt( 'key' );
		$nexml_attr_key->set_text( 'ATTRIBUTES' );
		$nexml_attr_key->paste( 'last_child', $nexml_dict );

		my $nexml_attr_dict = new_elt( 'dict' );
		$nexml_attr_dict->paste( 'last_child', $nexml_dict );

		for my $tol_key ( @tol_keys ) {			
			my $tol_val = $tol_node->att($tol_key);
			my ( $nexml_key, $nexml_val ) = ( new_elt('key'), new_elt('integer') );
			
			$nexml_key->set_text( $tol_key );
			$nexml_key->paste( 'last_child', $nexml_attr_dict );
			
			$nexml_val->set_text( $tol_val );
			$nexml_val->paste( 'last_child', $nexml_attr_dict );
		}
	}
}

sub attach_elements {
	my ( $tol_node, $nexml_dict ) = @_;
	my @children = $tol_node->children( sub { shift->tag !~ qr/^(?:NODES|NAME)$/ } );
	if ( @children ) {
		my $nexml_elts_key = new_elt( 'key' );
		$nexml_elts_key->set_text( 'ELEMENTS' );
		$nexml_elts_key->paste( 'last_child', $nexml_dict );
		
		my $nexml_elts_val = new_elt( 'any' );
		$nexml_elts_val->paste( 'last_child', $nexml_dict );
		
		for my $child ( @children ) {
			my $key = $child->tag;			
			$child->set_tag( "tol:$key" );
			$child->cut;
			$child->paste( 'last_child', $nexml_elts_val );
		}
	}
}

