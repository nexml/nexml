use strict;
use warnings;
use XML::Twig;
use Bio::Phylo::Matrices;
use Bio::Phylo::Matrices::Matrix;
use Bio::Phylo::Matrices::Datum;
use lib '../lib'; # TODO delete me, eventually
use Bio::Phylo::IO 'parse';

# a simple xml string, only with otus element
my $characters_xml = <<'!NO!SUBS!';
<?xml version="1.0" encoding="ISO-8859-1"?>
<nexml
	version="1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"	
	xsi:noNamespaceSchemaLocation="nexml.xsd">
		<characters id="Evolve_DNA" otus="Taxa" xsi:type="DNA" label="Matrix_#1_simulated_by_Evolve_DNA_Characters">
		<dict>
			<key>html</key>
			<any xmlns:h="http://www.w3.org/1999/xhtml">
				<h:h1>The "characters" element</h:h1>
				<h:p class="Level1">
					Characters blocks are defined as an abstract type in the
					schema. This is to allow for polymorphism depending on
					data type in instance documents. To specify the concrete
					subclass for a block, the xsi:type attribute is used, as
					shown above. In this case, the characters block contains
					DNA data. This means that only tokens as recognized by
					the IUPAC single character symbols for nucleotide ambiguity
					are allowed in the subsequent matrix rows. For some data
					types, the &lt;matrix&gt; element contains a &lt;definitions&gt;
					element that precedes any rows. This element contains, per
					character (i.e. column in the matrix) which states it can
					occupy. This way, greater granularity is achieved than in
					nexus, where only the allowed symbols for the whole matrix
					can be specified, but not per character.
				</h:p>
				<h:p class="Level1">
					&lt;characters&gt; can exist zero or more times in a document,
					in an order mixed with &lt;trees&gt; elements - and preceded
					by one or more &lt;otus&gt; elements. Characters and trees
					block must contain an "otus" id reference that refers to an
					existing otus block's id. This is analogous to the way mesquite
					links characters blocks, trees blocks and taxa blocks using the 
					'TITLE' and 'LINK' tokens.
				</h:p>
			</any>
			<key>nexus</key>
			<string>[! Simulated Matrices on Current Tree:  Matrix #1; Simulator: Evolve DNA Characters; most recent tree: Tree # 1 simulated by Uniform speciation (Yule) [seed for matrix sim. 1179690076101]
		Evolve DNA Characters:  Simulated evolution using model Jukes-Cantor with the following parameters:
        Root states model (Equal Frequencies): Equal Frequencies
        Equilibrium states model (Equal Frequencies): Equal Frequencies
        Character rates model (Equal Rates): Equal Rates
        Rate matrix model (Single Rate): single rate

        Stored Probability Model for Simulation:  Current model "Jukes-Cantor":         Root states model (Equal Frequencies): Equal Frequencies
        Equilibrium states model (Equal Frequencies): Equal Frequencies
        Character rates model (Equal Rates): Equal Rates
        Rate matrix model (Single Rate): single rate

        Stored Matrices:  Character Matrices from file: Project of "example.nex"
		Tree of context:  Tree(s) used from Tree Window 3 showing Stored Trees. Last tree used: Tree # 1 simulated by Uniform speciation (Yule)  [tree: (1:10.0,((3:2.3934066938158405,2:2.3934066938158405):5.831311968613758,(5:0.564686865239255,4:0.564686865239255):7.660031797190343):1.7752813375704022);] 
		]</string>
		</dict>
		<matrix aligned="1">
			<row id="r1" otu="t1" label="taxon_1">
				<dict>
					<key>html</key>
					<any xmlns:h="http://www.w3.org/1999/xhtml">
						<h:h2>The characters matrix "row"</h:h2>
						<h:p class="Level2">
							A row in a matrix can either contain an &lt;observations&gt;
							element (containing individually marked up "cells") or a
							&lt;seq&gt; element that contains a vector or string of
							tokens (for a more compact representation). Every row must
							refer to an "otu" in the "otus" block.
						</h:p>
					</any>
				</dict>
				<observations> 
					<obs def="c1" val="A">
						<dict>
							<key>html</key>
							<any xmlns:h="http://www.w3.org/1999/xhtml">
								<h:h3>A matrix cell</h:h3>
								<h:p class="Level3">
									An &lt;obs&gt; element comprises a single "observation"
									(a more neutral term than "character" which implies
									the data is homologized). The observation must contain
									a "def" attribute, which links (loosely or strictly,
									depending on data type) to a definition of what information
									the observation captures, and its parameter space. Since
									DNA data in nexml is encoded using IUPAC symbols, an
									explicit definition is not necessary, and so the "def"
									attribute can be interpreted as "tagged site that holds
									a nucleotide". The value for that site is specified using
									the "val" attribute, so in this case the "A" symbol.
								</h:p>
							</any>
						</dict>
					</obs>
					<obs def="c2" val="A"/>
					<obs def="c3" val="C"/>
					<obs def="c4" val="A"/>
					<obs def="c5" val="T"/>
					<obs def="c6" val="A"/>
					<obs def="c7" val="T"/>
					<obs def="c8" val="C"/>
					<obs def="c9" val="T"/>
					<obs def="c10" val="C"/>
				</observations>
			</row>
			<row id="r2" otu="t2">
				<observations>
					<obs def="c1" val="A"/> 
					<obs def="c2" val="T"/>
					<obs def="c3" val="A"/>
					<obs def="c4" val="C"/>
					<obs def="c5" val="C"/>
					<obs def="c6" val="A"/>
					<obs def="c7" val="G"/>
					<obs def="c8" val="C"/>
					<obs def="c9" val="A"/>
					<obs def="c10" val="T"/>
				</observations>
			</row>
			<row id="r3" otu="t3">
				<observations>
					<obs def="c1" val="G"/>
					<obs def="c2" val="A"/>
					<obs def="c3" val="G"/>
					<obs def="c4" val="G"/>
					<obs def="c5" val="G"/>
					<obs def="c6" val="T"/>
					<obs def="c7" val="A"/>
					<obs def="c8" val="T"/>
					<obs def="c9" val="G"/>
					<obs def="c10" val="G"/>
				</observations>
			</row>
			<row id="r4" otu="t4">
				<observations>
					<obs def="c1" val="G"/>
					<obs def="c2" val="G"/>
					<obs def="c3" val="T"/>
					<obs def="c4" val="C"/>
					<obs def="c5" val="T"/>
					<obs def="c6" val="T"/>
					<obs def="c7" val="A"/>
					<obs def="c8" val="G"/>
					<obs def="c9" val="A"/>
					<obs def="c10" val="G"/>
				</observations>
			</row>
			<row id="r5" otu="t5">
				<observations>
					<obs def="c1" val="C"/>
					<obs def="c2" val="G"/>
					<obs def="c3" val="T"/>
					<obs def="c4" val="C"/>
					<obs def="c5" val="A"/>
					<obs def="c6" val="C"/>
					<obs def="c7" val="A"/>
					<obs def="c8" val="G"/>
					<obs def="c9" val="T"/>
					<obs def="c10" val="G"/>
				</observations>
			</row>
		</matrix>
	</characters>

</nexml>
!NO!SUBS!
my $matrices = parse( -format => 'nexml', -string => $characters_xml )->[0];


#Create new twig for base XML document
my $nexml_tree = XML::Twig::Elt -> new(nexml => {'version' => "1.0",
											 'xmlns:xsi' => "http://www.w3.org/2001/XMLSchema-instance",
											 'xsi:noNamespaceSchemaLocation' => "nexml.xsd:"});
_serialize_characters ($matrices);

sub _serialize_characters
{
	my @matrix_list = @_;

	foreach my $matrix (@matrix_list)
	{
   	my $xsi_type = $matrix -> get_type;
		my $characters_block = XML::Twig::Elt -> new('characters');
		$characters_block -> set_att ('xsi:type' => $xsi_type);
		my $generics_ref = $matrix -> get_generic;
		my %generics = %$generics_ref;
		if (exists ($generics{id}))
		{
			$characters_block -> set_att('id' => $generics{id});
		}		
		if ($matrix -> get_name)
		{
			$characters_block -> set_att ('label' => $matrix -> get_name);
		}		
		my $lookup_table = {};
		if (exists ($generics{lookup}))
		{
   			$lookup_table = $generics{lookup};
		}
#***  FIX "TAXA" DATA FOR CHARACTERS PARSER!

		my $matrix_block = XML::Twig::Elt -> new('matrix');
#TODO: is this (aligned) stored anywhere? 
		#$matrix -> set_att ('aligned' => '1');
		
		# Create Definitions block for STANDARD and CONTINUOUS
		if (defined ($lookup_table))
		{
	   	   my $definitions_block = XML::Twig::Elt -> new('definitions');
	   	   foreach my $def (sort(keys(%$lookup_table)))
	   	   {
	      	   my $def_block = XML::Twig::Elt -> new('def');
	      	   $def_block -> set_att ('id' => $def);
	      	   foreach my $val (sort (keys(%{$lookup_table->{$def}})))
	      	   {
	         	   my $val_block = XML::Twig::Elt -> new('val');
	         	   $val_block -> set_att ('id' => $val);
	         	   $val_block -> set_att ('sym' => $lookup_table -> {$def} -> {$val});
	         	   $val_block-> paste ('last_child',$def_block);
	      	   }
	      	   $def_block-> paste ('last_child',$definitions_block);
	   	   }
	   	   $definitions_block -> paste ('last_child',$matrix_block);
	   }
	   		
		my @data = @{$matrix -> get_entities()};

	   # Re-build rows
	   my %rows = ();
	   my @index2char = ();
	   my %char2index = %{$generics{char2index}};
	   foreach my $char (keys (%char2index))
	   {
   	   $index2char[$char2index{$char}] = $char;
   	   }

	   foreach my $datum (@data)
	   {
 	  		#my $taxon = $datum -> get_taxon;
 	  		
# Temp code until taxon linking is in
			my $taxon = $datum -> get_name;
					
	   		if (!(exists ($rows{$taxon})))
	   		{
	      		$rows{$taxon} = [];
	   		}
	   		my $obs_block = XML::Twig::Elt -> new('obs');
	   		my $pos = $datum -> get_position;
	   		$obs_block -> set_att('def' => $index2char[$pos]);
	   		my $val = '';
	   		if ($xsi_type eq 'STANDARD')
	   		{
		   		foreach my $key (keys(%{$lookup_table -> {$index2char[$pos]}}))
		   		{
			   		if ($lookup_table -> {$index2char[$pos]} -> {$key} eq $datum -> get_char)
			   		{
				   		$val = $key;
			   		}
	   			}
   			}
	   		else
	   		{
		   		$val = $datum -> get_char;
	   		}
	   		$obs_block -> set_att('val' => $val);	   		
	   		$rows{$taxon}[$pos] = $obs_block;
				
		}
		
		foreach my $row (sort (keys (%rows)))
		{
			my $row_block = XML::Twig::Elt -> new('row');
			$row_block -> set_att ('otu' => $row);
			my $observations_block = XML::Twig::Elt -> new('observations');
			for( my $i=1; $i < @{$rows{$row}}; $i++)
			{
				$rows{$row}[$i] -> paste ('last_child',$observations_block);
			}
			$observations_block -> paste ('last_child', $row_block);
			$row_block -> paste ('last_child', $matrix_block);
		}
		$matrix_block -> print ('indented');		
	}
}
