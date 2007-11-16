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
	<characters id="Evolve_Continuous" otus="Taxa" xsi:type="CONTINUOUS">
		<dict>
			<key>nexus</key>
			<string>
[! Simulated Matrices on Current Tree:  Matrix #1; Simulator: Evolve Continuous Characters; most recent tree: Tree # 1 simulated by Uniform speciation (Yule) [seed for matrix sim. 1179690104191]
     Evolve Continuous Characters:  Markovian evolution using model: Brownian default (rate 1.0)
     Tree of context:  Tree(s) used from Tree Window 3 showing Stored Trees. Last tree used: Tree # 1 simulated by Uniform speciation (Yule)  [tree: (1:10.0,((3:2.3934066938158405,2:2.3934066938158405):5.831311968613758,(5:0.564686865239255,4:0.564686865239255):7.660031797190343):1.7752813375704022);] 
]				
			</string>
			<key>html</key>
			<any xmlns:h="http://www.w3.org/1999/xhtml">
				<h:h1>Continuous characters</h:h1>
				<h:p class="Level1">					
					This is a continuous matrix with five characters, 
					for four taxa. Some of the rules are similar to those for "DNA":
					the concrete data subtype is specified using the xsi:type attribute,
					the root element must contain a reference to an "otus" element.
				</h:p>
			</any>
		</dict>
		<matrix>
			<definitions>
				<def id="c1">
					<dict>
						<key>html</key>
						<any xmlns:h="http://www.w3.org/1999/xhtml">
							<h:h2>Continuous character descriptions</h:h2>
							<h:p class="Level2">
								Because this is a CONTINUOUS characters block, we
								define (and describe?) the characters - but obviously
								not the states that can be occupied (which I suppose
								would be an infinite number).
							</h:p>
						</any>
					</dict>
				</def>
				<def id="c2"/>
				<def id="c3"/>
				<def id="c4"/>
				<def id="c5"/>
			</definitions>
			<row id="r1" otu="t1">
				<observations>
					<obs def="c1" val="-1.545414144070023">
						<dict>
							<key>html</key>
							<any xmlns:h="http://www.w3.org/1999/xhtml">
								<h:h2>Continuous character observations</h:h2>
								<h:p class="Level2">
									Similar to STANDARD observations, we must refer
									to a predefined character using the "def" attribute;
									but unlike it we specify the actual continuous value
									of the observation using the "val" attribute, which
									is a floating point value (included "scientific"
									notation). 
								</h:p>
							</any>
						</dict>
					</obs>
					<obs def="c2" val="-2.3905621575431044"/>
					<obs def="c3" val="-2.9610221833467265"/>
					<obs def="c4" val="0.7868662069161243"/>
					<obs def="c5" val="0.22968509237534918"/>
				</observations>
			</row>
			<row id="r2" otu="t2">
				<observations>
					<obs def="c1" val="-1.6259836379710066"/>
					<obs def="c2" val="3.649352410850134"/>
					<obs def="c3" val="1.778885099660406"/>
					<obs def="c4" val="-1.2580877968480846"/>
					<obs def="c5" val="0.22335354995610862"/>
				</observations>					
			</row>
			<row id="r3" otu="t3">
				<observations>
					<obs def="c1" val="-1.5798979984134964"/>
					<obs def="c2" val="2.9548251411133157"/>
					<obs def="c3" val="1.522005675256233"/>
					<obs def="c4" val="-0.8642016921755289"/>
					<obs def="c5" val="-0.938129801832388"/>
				</observations>
			</row>
			<row id="r4" otu="t4">
				<observations>
					<obs def="c1" val="2.7436692306788086"/>
					<obs def="c2" val="-0.7151148143399818"/>
					<obs def="c3" val="4.592207937774776"/>
					<obs def="c4" val="-0.6898841440534845"/>
					<obs def="c5" val="0.5769509574453064"/>
				</observations>
			</row>
			<row id="r5" otu="t5">
				<observations>
					<obs def="c1" val="3.1060827493657683"/>
					<obs def="c2" val="-1.0453787389160105"/>
					<obs def="c3" val="2.67416332763427"/>
					<obs def="c4" val="-1.4045634106692808"/>
					<obs def="c5" val="0.019890469925520196"/>
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
