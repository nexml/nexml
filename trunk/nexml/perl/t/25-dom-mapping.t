use Test::More tests => 80;
use Test::Exception;
use strict;
use lib '../lib';
use XML::Simple;
use File::Temp;

# element order required by NeXML standard
# see _order() routine below
my $nexml_order = {
    'nex:nexml'      => [qw( otus trees characters )],
    'otus'       => [qw( otu )],
    'otu'        => [qw( id label )],
    'trees'      => [qw( tree )],
    'tree'       => [qw( node edge rootedge )],
    'characters' => [qw( format matrix )],
    'format'     => [qw( states char )],
    'states'     => [qw( state uncertain_state_set )],
    'uncertain_state_set' => [qw( member )],
    'matrix'     => [qw( row )],
    'row'        => [qw( cell seq )]
};

# test DOM mapping formats for interface compliance and functionality

use_ok('Bio::Phylo::Util::DOM');

my @formats     = qw( twig libxml );
my @fac_methods = qw( create_element create_document get_format set_format );
my @elt_methods = qw( new get_attributes set_attributes clear_attributes
                   get_tagname set_tagname set_text get_text clear_text
                   get_parent get_children get_first_child get_last_child
                   get_next_sibling get_prev_sibling get_elements_by_tagname
                   set_child prune_child to_xml_string );
my @doc_methods = qw( new set_encoding get_encoding set_root get_root
                   get_element_by_id get_elements_by_tagname
                   to_xml_string to_xml_file );


ok( my $test = XMLin('data/01_basic.xml', KeepRoot=>1, KeyAttr=>[]), 'test XML file as nested data structure' );

for my $format (@formats) {
    ok( my $dom = Bio::Phylo::Util::DOM->new(-format => $format),
	"$format object" );

    can_ok( $dom, @fac_methods );

    ok( my $elt = $dom->create_element('boog'), "$format element" );
    ok( my $doc = $dom->create_document, "$format document" );

    can_ok( $elt, @elt_methods);
    can_ok( $doc, @doc_methods);

    1;
 
    ok( $elt = _parse( undef, 'nex:nexml', $test, $dom), "parse XML structure as $format DOM");
    ok( $doc->set_root($elt), "set $format document root element" );
    ok( my $fh = File::Temp->new, 'make temp file' );
    my $fn = $fh->filename;
    ok( $doc->to_xml_file($fn), "write XML from $format DOM" );
    $fn =~ s/\\/\//g;
    is( (qx{ bash -c " if (../script/nexvl.pl -Q $fn) ; then echo -n 1 ; else echo -n 0 ; fi" })[0], 1, 'dom-generated XML is valid NeXML' );
    
    is( scalar $doc->get_elements_by_tagname('row'), 6, "get_elements_by_tagname");
    ok( my $s11 = $doc->get_element_by_id('s11'), "found uncertain_state_set s11" );
    ok( my $s12 = $doc->get_element_by_id('s12'), "found uncertain_state_set s12" );
    ok( !$doc->get_element_by_id('s13'), "no s13" );
    ok( !$elt->get_elements_by_tagname('boog'), "no boog here");
    is( scalar $s12->get_elements_by_tagname('member'), 11, "found all members of s12");


    # test: *_text methods
    ok( $s11->set_text("This state set is somewhat uncertain"), "set text");
    ok( $s11->set_text(" and it still is."), "set 2d text");
    is( $s11->get_text, "This state set is somewhat uncertain and it still is.", "text concatenated");
    ok($s11->clear_text, "clear text attempt");
    ok( !$s11->get_text, "text is gone");
    # test: traversal, prune methods - make sure ids of pruned descendants
    #  disappear from document
    ok( $s12->set_child( $dom->create_element('boog', 'id'=>'schlarb') ), 'test child');
    ok( my $child = $doc->get_element_by_id('schlarb'), 'found child');
    ok( !$s12->prune_child($elt), "can't prune a non-child");
    ok( $s12->prune_child( $child ), "prune child");
    ok( !$doc->get_element_by_id('schlarb'), "child gone by_id");
    # test: clear_* methods
    ok( my $row13 = $doc->get_element_by_id('row13'), "get row13");
    is( $row13->get_attributes('label'), "otuD", "get label");
    ok( $row13->clear_attributes('label'), "clear label attempt");
    ok( !$row13->get_attributes('label'), "label gone" );
    ok( $row13->clear_attributes('id', 'otu'), "clear id, otu attrs");
    ok( !$row13->get_attributes('otu'), "otu attr gone");
    ok( !$doc->get_element_by_id('row13'), "row13 id gone by_id");
    is( $row13->get_first_child->get_tagname, "cell", "first child");
    is( $row13->get_next_sibling->get_attributes('label'), "otuE", "next sibling");
    is( $row13->get_prev_sibling->get_attributes('label'), "otuC", "prev sibling");
    is( $elt->get_first_child->get_tagname, "otus", "first child of root");
    is( $elt->get_last_child->get_tagname, "characters", "last child of root");
} # formats


sub _parse {
    my ($elt, $key, $h, $dom) = @_;
    unless ($elt) {
	$elt = $dom->create_element($key);
	foreach my $k ( _order($key, keys %{$$h{$key}}) ) {
	    _parse($elt, $k, $$h{$key}{$k}, $dom);
	}
	return $elt;
    }
    for (ref $h) {
	!$_ && do {
	    $elt->set_attributes($key, $h);
	    last;
	};
	/HASH/ && do {
	    my $new_elt = $dom->create_element($key);
	    $elt->set_child($new_elt);
	    foreach my $new_key (_order($key, keys %$h)) {
		_parse($new_elt, $new_key, $$h{$new_key}, $dom);
	    }
	    last;
	};
	/ARRAY/ && do {
	    foreach my $new_item (@$h) {
		_parse($elt, $key, $new_item, $dom);
	    }
	    last;
	};
    }
    return;
}

sub _order {
    my ($key, @a) = @_;
    return @a unless ($$nexml_order{$key});
    my (%h, @o,$max);
    @h{ @{$$nexml_order{$key}} } = (0..@{$$nexml_order{$key}});
    @o = @h{@a};
    $max = ($_ > $max ? $_ : $max) for @o;
    map { $_ = ++$max unless defined } @o;
    @a[@o] = @a;
    return @a;
}
	
    
       
    
