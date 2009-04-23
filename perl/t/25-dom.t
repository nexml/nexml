use Test::More tests => 51;
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
ok( my $test = parse( -file=>'t/data/01_basic.xml', -format=>'nexml' ), 'parse 01_basic.xml');

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

$matrix->to_xml;
$matrix->to_dom;

# create LibXML elements
my %dom;
foreach (@elts) {
    ok( $dom{$_} = eval "\$$_->to_dom", "do \$$_->to_dom()" );
}

# enchilada
my $proj = $fac->create_project;
$proj->insert($taxa);
$proj->insert($forest);
$proj->insert($matrix);

ok( my $doc = $proj->dom, 'LibXML document from project');

SKIP: {
    skip 'BIO_PHYLO_TEST_NEXML not set', 3 unless $ENV{'BIO_PHYLO_TEST_NEXML'};
    # write to tempfile, run validation script (at ../script/nexvl.pl) on it
    ok( my $fh = File::Temp->new, 'make temp file' );
    my $fn = $fh->filename;
    ok( $doc->toFile($fn, 1), 'write XML from dom' );
    $fn =~ s/\\/\//g;
    is( (qx{ bash -c " if (./script/nexvl.pl -Q $fn) ; then echo -n 1 ; else echo -n 0 ; fi" })[0], 1, 'dom-generated XML is valid NeXML' );
}
# from here, want to check that all elements in the original file are 
# manifested in the $doc DOM

ok(my $rdr = XML::LibXML::Reader->new( location => 't/data/01_basic.xml' ), 
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
    my $b = $doc->getElementsByTagName($_);
    my $n = $a ? ($a->isa('XML::LibXML::NodeList') ? $a->size : 1) : 0;
    my $m = $b ? ($b->isa('XML::LibXML::NodeList') ? $b->size : 1) : 0;
    is($m, $n, "number of $_ elements correct ($n)");
}

1;
