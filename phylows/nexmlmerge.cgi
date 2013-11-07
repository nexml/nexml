#!/usr/bin/perl
use strict;
use warnings;
use CGI;
use JSON;
use Getopt::Long;
use Bio::Phylo::Factory;
use Bio::Phylo::IO 'parse';
use Bio::Phylo::Util::CONSTANT ':objecttypes';
use CGI::Carp 'fatalsToBrowser';

# required parameters
my $dataformat = 'fasta';
my $datatype   = 'dna';
my $treeformat = 'newick';
my $header     = '';
my ( $datafh, $treefh, $jsonfh, %namespaces );

# process command line arguments
if ( @ARGV ) {
	GetOptions(
		'treeformat=s' => \$treeformat,
		'dataformat=s' => \$dataformat,
		'tree=s'       => sub { open $treefh, '<', pop or die $! },
		'data=s'       => sub { open $datafh, '<', pop or die $! },
		'meta=s'       => sub { open $jsonfh, '<', pop or die $! },
		'xmlns=s'      => \%namespaces,
	);
}

# process CGI arguments
else {
	my $cgi = CGI->new;
	$datatype   = $cgi->param('datatype')   || 'dna';	
	$dataformat = $cgi->param('dataformat') || 'fasta';
	$treeformat = $cgi->param('treeformat') || 'newick';
	$treefh     = $cgi->upload('tree');
	$datafh     = $cgi->upload('data');	
	$jsonfh     = $cgi->upload('meta');
	$header     = "Content-type: application/xml\n\n";
	%namespaces = map { substr($_,8) => $cgi->param($_) } grep { /^xmlns:/ } $cgi->param;
}

# parse alignments if we have an open handle, otherwise initialize empty array
my @matrices = $datafh ? @{
	parse(
		'-format' => $dataformat,
		'-type'   => $datatype,
		'-handle' => $datafh,
		'-as_project' => 1,
	)->get_items(_MATRIX_)
} : ();

# parse trees if we have an open handle, otherwise initialize empty array
my @trees = $treefh ? @{
	parse(
		'-format' => $treeformat,
		'-handle' => $treefh,
		'-as_project' => 1,
	)->get_items(_TREE_)
} : ();

# start populating the output project
my $fac  = Bio::Phylo::Factory->new;
my $proj = $fac->create_project( '-namespaces' => \%namespaces );
my ( @taxa, @blocks );

# create a forest object if needed
if ( @trees ) {
	my $forest = $fac->create_forest;
	$forest->insert($_) for @trees;
	$proj->insert($forest);
	push @taxa, $forest->make_taxa;
	push @blocks, $forest;
}

# insert data objects
for my $matrix ( @matrices ) {
	$proj->insert($matrix);
	push @taxa, $matrix->make_taxa;
	push @blocks, $matrix;	
}

# do we need to do a taxon merge?
if ( my $taxa = shift @taxa ) {
	$taxa = $taxa->merge_by_name(@taxa) if @taxa;
	$proj->insert($taxa);
	$_->set_taxa($taxa) for @blocks;
}

# fold the json (if any) into the project
if ( $jsonfh ) {

	# we allow json data structures of arbitrary depth, which we 
	# process by recursive traversal using this sub reference
	my $recurse;
	$recurse = sub {
		my ( $subject, $ref ) = @_;
		
		# reference is a hash
		if ( ref $ref eq 'HASH' ) {
			for my $predicate ( keys %{ $ref } ) {
				my $object = $ref->{$predicate};
				my $meta   = $fac->create_meta( '-triple' => { $predicate => $object } );
				$subject->add_meta($meta);
				$recurse->( $meta, $object ) if ref $object;
			}
		}
		
		# reference is an array
		if ( ref $ref eq 'ARRAY' ) {
			for my $triple ( @{ $ref } ) {
				$recurse->( $subject, $triple );
			}
		}
	};
	
	# here we start the recursion
	$recurse->( $proj, decode_json( do { local $/; <$jsonfh> } ) );
}

# print output
print $header, $proj->to_xml;

