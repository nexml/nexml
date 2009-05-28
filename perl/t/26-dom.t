use Test::More tests => 83;
use File::Temp;
use lib '../lib';
use strict;

my @xml_objects = qw( 
		   Bio::Phylo::Forest::Tree
                   Bio::Phylo::Forest::Node
		   Bio::Phylo::Matrices::Matrix
		   Bio::Phylo::Matrices::Datatype
		   Bio::Phylo::Matrices::Datum
		   Bio::Phylo::Project      
                 );
# uses and cans

use_ok('Bio::Phylo::Factory');
use_ok('Bio::Phylo::IO');
use_ok('Bio::Phylo::Util::Dom');
use_ok('Bio::Phylo::Util::XMLWritable');
use_ok('Bio::Phylo::Util::Exceptions');
use_ok('Bio::Phylo::Util::CONSTANT');
use_ok('XML::Simple');
use_ok('XML::LibXML');
use_ok('XML::LibXML::Reader');

foreach (@xml_objects) {
    use_ok($_);
    /Datatype/ && do {
	can_ok($_->new('dna'), 'to_dom');
	next;
    };
    can_ok($_->new, 'to_dom');
}

# test data
use Bio::Phylo::IO qw( parse );
ok( my $test = parse( -string=> do { local $/; <DATA> }, -format=>'nexml' ), 'parse 01_basic.xml');

# factory object
ok( my $fac = Bio::Phylo::Factory->new(), 'make factory' );
# dom element creation
my @elts = qw(forest tree node taxa taxon matrix datum);
my %dom_elts;
my $forest = $$test[1];
my $tree = $forest->get_entities->[0];
my $node = $tree->find_node('otuD');
my $taxa = $$test[0];
my $taxon = $taxa->get_entities->[0];
my $matrix = $$test[2];
my $datum = $matrix->get_entities->[0];

# enchilada
my $proj = $fac->create_project;
$proj->insert($taxa);
$proj->insert($forest);
$proj->insert($matrix);

foreach my $format (qw(twig libxml)) {
    ok( Bio::Phylo::Util::DOM->new(-format => $format), "set DOM format: $format");
    my %dom;
    foreach (@elts) {
	ok( $dom{$_} = eval "\$$_->to_dom", "do \$$_->to_dom()" );
    }

    
    ok( my $doc = $proj->doc, "$format document from project");

  SKIP: {
      skip 'BIO_PHYLO_TEST_NEXML not set', 3 unless $ENV{'BIO_PHYLO_TEST_NEXML'};
    # write to tempfile, run validation script (at ../script/nexvl.pl) on it
      ok( my $fh = File::Temp->new, 'make temp file' );
      my $fn = $fh->filename;
      ok( $doc->to_xml_file($fn, 1), 'write XML from dom' );
      $fn =~ s/\\/\//g;
      is( (qx{ bash -c " if (./script/nexvl.pl -Q $fn) ; then echo -n 1 ; else echo -n 0 ; fi" })[0], 1, 'dom-generated XML is valid NeXML' );
    }

# from here, want to check that all elements in the original file are 
# manifested in the $doc DOM

    ok(my $rdr = XML::LibXML::Reader->new( location => 'data/01_basic.xml' ), 
       'read original data file');
    $rdr->read;
    $rdr->copyCurrentNode(1);
    my $org_doc = $rdr->document;
    my %org_elts;

    my @elt_tags = qw( nex:nexml otus otu trees tree node edge characters format states state uncertain_state_set member char matrix row cell );

    foreach (@elt_tags) {
		$org_elts{$_} = $org_doc->getElementsByTagName($_);
    }

    foreach (@elt_tags) {
		my $a = $org_elts{$_};
		my $n = $a ? ($a->isa('XML::LibXML::NodeList') ? $a->size : 1) : 0;
		my @b = $doc->get_elements_by_tagname($_);
		is(@b, $n, "number of $_ elements correct ($n)");
    }

}

__DATA__
<nex:nexml generator="Bio::Phylo::Project v.0.17_RC9_841" version="0.8" xmlns="http://www.nexml.org/1.0" xmlns:nex="http://www.nexml.org/1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns" xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd">
<!-- this is a simple test file generated from a NEXUS file -->
<otus id="otus1">
    <otu id="otu2" label="otuA"/>
    <otu id="otu3" label="otuB"/>
    <otu id="otu4" label="otuC"/>
    <otu id="otu5" label="otuD"/>
    <otu id="otu6" label="otuE"/>
    <otu id="otu7" label="otuF"/>
  </otus>
  <trees id="trees16" otus="otus1">
    <tree id="tree18" label="'the tree'" xsi:type="nex:IntTree">
      <node id="node19" root="true"/>
      <node id="node20" label="otuA" otu="otu2"/>
      <node id="node21"/>
      <node id="node22"/>
      <node id="node27"/>
      <node id="node23"/>
      <node id="node26" label="otuD" otu="otu5"/>
      <node id="node28" label="otuE" otu="otu6"/>
      <node id="node29" label="otuF" otu="otu7"/>
      <node id="node24" label="otuB" otu="otu3"/>
      <node id="node25" label="otuC" otu="otu4"/>
      <edge id="edge20" length="4" source="node19" target="node20"/>
      <edge id="edge21" length="1" source="node19" target="node21"/>
      <edge id="edge22" length="1" source="node21" target="node22"/>
      <edge id="edge27" length="2" source="node21" target="node27"/>
      <edge id="edge23" length="1" source="node22" target="node23"/>
      <edge id="edge26" length="2" source="node22" target="node26"/>
      <edge id="edge28" length="1" source="node27" target="node28"/>
      <edge id="edge29" length="1" source="node27" target="node29"/>
      <edge id="edge24" length="1" source="node23" target="node24"/>
      <edge id="edge25" length="1" source="node23" target="node25"/>
    </tree>
  </trees>
  <characters id="characters8" otus="otus1" xsi:type="nex:StandardCells">
    <format>
      <states id="states10">
        <state id="s1" symbol="0"/>
        <state id="s2" symbol="1"/>
        <state id="s3" symbol="2"/>
        <state id="s4" symbol="3"/>
        <state id="s5" symbol="4"/>
        <state id="s6" symbol="5"/>
        <state id="s7" symbol="6"/>
        <state id="s8" symbol="7"/>
        <state id="s9" symbol="8"/>
        <state id="s10" symbol="9"/>
        <uncertain_state_set id="s11" symbol="-"></uncertain_state_set>
        <uncertain_state_set id="s12" symbol="?">
          <member state="s1"/>
          <member state="s2"/>
          <member state="s3"/>
          <member state="s4"/>
          <member state="s5"/>
          <member state="s6"/>
          <member state="s7"/>
          <member state="s8"/>
          <member state="s9"/>
          <member state="s10"/>
          <member state="s11"/>
        </uncertain_state_set>
      </states>
      <char id="c1" states="states10"/>
    </format>
    <matrix>
      <row id="row9" label="otuA" otu="otu2">
        <cell char="c1" state="s1"/>
      </row>
      <row id="row11" label="otuB" otu="otu3">
        <cell char="c1" state="s3"/>
      </row>
      <row id="row12" label="otuC" otu="otu4">
        <cell char="c1" state="s3"/>
      </row>
      <row id="row13" label="otuD" otu="otu5">
        <cell char="c1" state="s1"/>
      </row>
      <row id="row14" label="otuE" otu="otu6">
        <cell char="c1" state="s2"/>
      </row>
      <row id="row15" label="otuF" otu="otu7">
        <cell char="c1" state="s1"/>
      </row>
    </matrix>
  </characters>
</nex:nexml>
