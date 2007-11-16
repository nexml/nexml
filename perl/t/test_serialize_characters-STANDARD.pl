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
	<characters id="Evolve_Standard" otus="Taxa" xsi:type="STANDARD">
		<dict>
			<key>html</key>
			<any xmlns:h="http://www.w3.org/1999/xhtml">
				<h:h1>Categorical characters</h:h1>
				<h:p class="Level1">
					This is a categorical matrix with two characters, four states each, 
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
							<h:h2>Character description</h:h2>
							<h:p class="Level2">
								The &lt;def&gt; element defines a character. It is
								somewhat similar in spirit to the "CHARLABELS" token
								in nexus, but more powerful in that it can take
								attachments, and it defines the parameter space for
								the character. Since this characters block contains
								"STANDARD" data, we define for each character the
								states it can occupy.
							</h:p>
						</any>
					</dict>
					<val id="s1" sym="1">
						<dict>
							<key>html</key>
							<any xmlns:h="http://www.w3.org/1999/xhtml">
								<h:h3>State description</h:h3>
								<h:p class="Level3">								
									The &lt;val&gt; element defines a character state.
									In its simplest form it's just a place holder for
									an id that observations refer to, but it can be
									annotated. It's similar in spirit to the 
									"CHARSTATELABELS" token in nexus, but richer. For
									example, by attaching a base64 encoded image, a 
									character state can be a picture.
								</h:p>								
							</any>
						</dict>
					</val>
					<val id="s2" sym="2"/>
					<val id="s3" sym="3"/>
					<val id="s4" sym="4"/>
				</def>
				<def id="c2">
					<val id="s1" sym="1"/>
					<val id="s2" sym="2"/>
					<val id="s3" sym="3"/>
					<val id="s4" sym="4"/>				
				</def>
			</definitions>	
			<row id="r1" otu="t1" label="taxon_1">
				<observations>
					<obs def="c1" val="s1">
						<dict>
							<key>html</key>
							<any xmlns:h="http://www.w3.org/1999/xhtml">
								<h:h2>Standard character observation</h:h2>
								<h:p class="Level2">
									Unlike DNA data, a STANDARD observation must refer
									to a defined character using the "def" attribute,
									and a defined state using the "val" attribute.
								</h:p>
							</any>
						</dict>
					</obs>
					<obs def="c2" val="s2"/>
				</observations>
			</row>
			<row id="r2" otu="t2" label="taxon_2">
				<observations>
					<obs def="c1" val="s2"/>
					<obs def="c2" val="s2"/>
				</observations>
			</row>
			<row id="r3" otu="t3" label="taxon_3">
				<observations>
					<obs def="c1" val="s3"/>
					<obs def="c2" val="s4"/>
				</observations>
			</row>
			<row id="r4" otu="t4" label="taxon_4">
				<observations>
					<obs def="c1" val="s2"/>
					<obs def="c2" val="s3"/>
				</observations>
			</row>
			<row id="r5" otu="t5" label="taxon_5">
				<observations>
					<obs def="c1" val="s4"/>
					<obs def="c2" val="s1"/>
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
