(function(){
function parse(args) {
	var xml = args["string"];
	var xmlDoc;
	var result = new Array();
	try { //Internet Explorer
  		xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
  		xmlDoc.async = "false";
  		xmlDoc.loadXML(text);
  	}
	catch(e) {
  		try { //Firefox, Mozilla, Opera, etc.
  			var parser = new DOMParser();
  			xmlDoc = parser.parseFromString(xml,"text/xml");
  		}
  		catch(e) {
  			alert(e.message);
  			return result;
  		}
	}
	var taxa_blocks = process_otus(xmlDoc.getElementsByTagName("otus"));
	var tree_blocks = process_trees(xmlDoc.getElementsByTagName("trees"));
	var char_blocks = process_characters(xmlDoc.getElementsByTagName("characters"));
	resolve_tree_taxa(taxa_blocks,tree_blocks);
	resolve_matrix_taxa(taxa_blocks,char_blocks);
	for ( var key in taxa_blocks ) {
		result.push(taxa_blocks[key]);
	}
	for ( var i in char_blocks ) {
		result.push(char_blocks[i]);
	}
	for ( var i in tree_blocks ) {
		result.push(tree_blocks[i]);
	}
	return result;
}
Phylo.Parsers.Nexml.parse = parse;

function resolve_matrix_taxa(taxa_blocks,char_blocks) {
	for ( var i in char_blocks ) {
		var taxa_id = char_blocks[i].get_generic('otus');
		var taxa = taxa_blocks[taxa_id];
		var taxon = {};
		taxa.visit(
			function(t) {
				var taxon_id = t.get_xml_id();
				taxon[taxon_id] = t;
			}
		);
		var rows = char_blocks[i].get_entities();
		for ( var j in rows ) {
			var taxon_id_ref = rows[j].get_generic('otu');
			rows[j].set_taxon(taxon[taxon_id_ref]);
		}
		char_blocks[i].set_taxa(taxa);
	}
}

function process_characters(char_elts) {
	var char_blocks = [];
	if(char_elts.length==0) return char_blocks;
	for ( var i = 0; i < char_elts.length; i++ ) {
		var char_obj = obj_from_elt(char_elts[i]);
		var type = char_elts[i].attributes.getNamedItem('xsi:type').value;
		type = type.match(/^nex:([A-Z][a-z]+)/)[1];
		char_obj.set_type(type);
		var otus = char_elts[i].attributes.getNamedItem('otus').value;
		char_obj.set_generic({ 'otus' : otus });
		var format_elt = char_elts[i].getElementsByTagName('format')[0];
		/*
		 * state_set = {
		 *     'states1' : { // state set ID
		 *         'symbols' : {
		 *             's1' : 1, // i.e. state ID to state symbol mapping
		 *             's2' : 2
		 *         }
		 *         lookup : {
		 *             1 : [ 1 ], // ambiguity mapping
		 *             2 : [ 2 ],
		 *         }
		 *     },
		 *     'states2' : {
		 *         // etc.
		 *     }
		 * }
		 */
		var state_set = {};
		var state_set_of_char = {}; // char id to state set mapping
		var col_indices = {};
		if ( format_elt ) {			
			var states_elts = format_elt.getElementsByTagName('states');
			for ( var j = 0; j < states_elts.length; j++ ) {
				var id = states_elts[j].attributes.getNamedItem('id').value;
				var lookup = {};
				var symbol_of_id = {};
				var state_elts = states_elts[j].getElementsByTagName('state');
				var pss = states_elts[j].getElementsByTagName('polymorphic_state_set');
				var uss = states_elts[j].getElementsByTagName('uncertain_state_set');
				resolve_mapping(state_elts,symbol_of_id,lookup);
				resolve_mapping(pss,symbol_of_id,lookup);
				resolve_mapping(uss,symbol_of_id,lookup);
				state_set[id] = { 'lookup' : lookup, 'symbols' : symbol_of_id };
				char_obj.set_lookup(lookup);
			}
			var col_elts = format_elt.getElementsByTagName('char');
			for ( var j = 0; j < col_elts.length; j++ ) {
				var id = col_elts[j].attributes.getNamedItem('id').value;
				var states = col_elts[j].attributes.getNamedItem('states').value;
				state_set_of_char[id] = states;
				col_indices[id] = j;
			}
		}
		var matrix_elt = char_elts[i].getElementsByTagName('matrix')[0];
		process_matrix(char_obj,matrix_elt,state_set_of_char,state_set,col_indices);
		char_blocks.push(char_obj);
	}	
	return char_blocks;
}

function process_matrix(char_obj,matrix_elt,state_set_for_char,state_set,col_indices) {
	var rows = matrix_elt.getElementsByTagName('row');
	for ( var i = 0; i < rows.length; i++ ) {
		var row_obj = obj_from_elt(rows[i]);
		var otu = rows[i].attributes.getNamedItem('otu').value;
		row_obj.set_type_object(char_obj.get_type_object());		
		row_obj.set_generic({'otu':otu});
		var seq_elt = rows[i].getElementsByTagName('seq')[0];
		if ( seq_elt ) {
			row_obj.set_char([seq_elt.textContent]);
		}
		else {
			var cell_elts = rows[i].getElementsByTagName('cell');
			var characters = [];
			for ( var j = 0; j < cell_elts.length; j++ ) {
				var char_id = cell_elts[j].attributes.getNamedItem('char').value;
				var col_index;
				if ( col_indices[char_id] != null ) {
					col_index = col_indices[char_id]; 
				}
				else {
					col_index = char_id;
				}
				var state_id = cell_elts[j].attributes.getNamedItem('state').value;
				var state_set_id = state_set_for_char[char_id];
				var state;
				if ( state_set_id != null ) {
					state = state_set[state_set_id]['symbols'][state_id];
				}
				else {
					state = state_id;
				}
				characters[col_index] = state;
			}
			var missing = row_obj.get_missing();
			for ( var j in characters ) {
				if ( characters[j] == null ) {
					characters[j] = missing;
				}
			}
			row_obj.set_char(characters);
		}
	}	
}

function resolve_mapping(state_elts,symbol_of_id,lookup) {
	for ( var i = 0; i < state_elts.length; i++ ) {
		var state_elt = state_elts[i];
		var symbol = state_elt.attributes.getNamedItem('symbol').value;
		var id = state_elt.attributes.getNamedItem('id').value;
		symbol_of_id[id] = symbol;
		var member_elts = state_elt.getElementsByTagName('member');
		if ( member_elts.length ) {
			var symbols = [];
			for ( var i in member_elts ) {
				var member_id = member_elts[i].attributes.getNamedItem('state').value;
				symbols.push(symbol_of_id[member_id]);
			}
			lookup[symbol] = symbols;
		}
		else {
			lookup[symbol] = [symbol];
		}
	}
}

function resolve_tree_taxa(taxa_blocks,tree_blocks) {
	for ( var i = 0; i < tree_blocks.length; i++ ) {
		var otus_id = tree_blocks[i].get_generic('otus');
		var taxa_block = taxa_blocks[otus_id];
		tree_blocks[i].set_taxa(taxa_block);
		var taxa = taxa_block.get_entities();
		var taxa_by_id = {};
		for ( var j = 0; j < taxa.length; j++ ) {
			taxa_by_id[taxa[j].get_xml_id()] = taxa[j];
		}
		var trees = tree_blocks[i].get_entities();
		for ( var j = 0; j < trees.length; j++ ) {
			var nodes = trees[j].get_entities();
			for ( var k = 0; k < nodes.length; k++ ) {
				var id = nodes[k].get_generic('otu');
				if ( id != null ) {
					nodes[k].set_taxon(taxa_by_id[id]);
				}
			}
		}
	}
}
Phylo.Parsers.Nexml.resolve_tree_taxa = resolve_tree_taxa;

function process_trees(trees_elt) {
	var result = new Array();
	if (!trees_elt) return result;
	for ( var i = 0; i < trees_elt.length; i++ ) {
		var forest_obj = obj_from_elt(trees_elt[i]);
		var otus_id = trees_elt[i].attributes.getNamedItem("otus").value;
		forest_obj.set_generic({'otus':otus_id});
		var tree_elt = trees_elt[i].getElementsByTagName("tree");
		for ( var j = 0; j < tree_elt.length; j++ ) {
			forest_obj.insert(process_tree(tree_elt[j]));
		}
		result.push(forest_obj);
	}
	return result;
}
Phylo.Parsers.Nexml.process_trees = process_trees;

function process_tree(tree_elt) {
	var tree_obj = obj_from_elt(tree_elt);
	var source_of  = {};
	var target_of  = {};
	var length_of  = {};
	var otu_of     = {};
	var node_by_id = {};
	var node_elt = tree_elt.getElementsByTagName("node");
	for ( var i = 0; i < node_elt.length; i++ ) {
		var node_obj = obj_from_elt(node_elt[i]);
		var id = node_obj.get_xml_id();
		tree_obj.insert(node_obj);
		node_by_id[id] = node_obj;
		var otuAttr = node_elt[i].attributes.getNamedItem("otu");
		if (otuAttr != null) {
			var otu = otuAttr.value;
			otu_of[id] = otu;
			node_obj.set_generic({ 'otu' : otu });
		}
	}
	var edge_elt = tree_elt.getElementsByTagName("edge");
	for ( var i = 0; i < edge_elt.length; i++ ) {
		var attrs  = edge_elt[i].attributes;
		var target = attrs.getNamedItem("target").value;
		var source = attrs.getNamedItem("source").value;		
		source_of[target] = source;
		target_of[source] = target;
		if ( attrs.getNamedItem("length") != null ) {
			length_of[target] = attrs.getNamedItem("length").value;
		}
	}
	var entities = tree_obj.get_entities();
	for ( var i = 0; i < entities.length; i++ ) {
		var id = entities[i].get_xml_id();
		if ( source_of[id] != null ) {
			var parent_node = node_by_id[source_of[id]];
			entities[i].set_parent(parent_node);
			parent_node.set_child(entities[i]);
		}
		if ( length_of[id] != null ) {
			entities[i].set_branch_length(length_of[id]);
		}
		if ( otu_of[id] != null ) {
			entities[i].set_generic({ 'otu' : otu_of[id] });
		}
	}
	var root_edge = tree_elt.getElementsByTagName("rootedge")[0];
	if ( root_edge != null ) {
		var target = root_edge.attributes.getNamedItem("target").value;
		var bl = root_edge.attributes.getNamedItem("length").value;
		node_by_id[target].set_branch_length(bl);
	}	
	return tree_obj;
}
Phylo.Parsers.Nexml.process_tree = process_tree;

function process_otus (otus) {
	var taxa_blocks = {};
	for ( var i = 0; i < otus.length; i++ ) {
		var taxa = obj_from_elt(otus[i]);
		var otu = otus[i].getElementsByTagName("otu");
		for ( var j = 0; j < otu.length; j++ ) {
			var taxon = obj_from_elt(otu[j]);
			taxa.insert(taxon);
		}
		taxa_blocks[taxa.get_xml_id()] = taxa;
	}
	return taxa_blocks;	
}
Phylo.Parsers.Nexml.process_otus = process_otus;

function obj_from_elt (elt) {
	var attrs = elt.attributes;
	var args = {};
	for ( var j = 0; j < attrs.length; j++ ) {
		if ( attrs[j].name == 'label' && attrs[j].value != '' ) {
			args["name"] = attrs[j].value;
		}
		else if ( attrs[j].name == 'id' && attrs[j].value != '' ) {
			args["xml_id"] = attrs[j].value;
		}
	}
	var children = elt.childNodes;
	for ( var i = 0; i < children.length; i++ ) {
		if ( children[i].nodeType == 1 && children[i].tagName == 'dict' ) {
			args["generic"] = { 'dict' : parse_dict(children[i]) };
			break;
		}
	}
	var tag_name = elt.nodeName.toLowerCase();
	switch(tag_name) {
		case 'otus'       : return new Phylo.Taxa(args);
		case 'otu'        : return new Phylo.Taxa.Taxon(args);
		case 'trees'      : return new Phylo.Forest(args);
		case 'tree'       : return new Phylo.Forest.Tree(args);
		case 'node'       : return new Phylo.Forest.Node(args);
		case 'characters' : return new Phylo.Matrices.Matrix(args);
		case 'row'        : return new Phylo.Matrices.Datum(args);
		default : throw new Phylo.Util.Exceptions.API("Can't create object from element " + tag_name);
	}
}
Phylo.Parsers.Nexml.obj_from_elt = obj_from_elt;

function parse_dict (elt) {
	var result = {};
	var children = elt.childNodes;
	for ( var i = 0; i < children.length; i++ ) {		
		if ( children[i].nodeType == 1 && children[i].tagName == 'key' ) {
			var key = children[i].textContent;
			var value_elt;
			for ( var j = i+1; j < children.length; j++ ) {
				if ( children[j].nodeType == 1 ) {
					value_elt = children[j];
					break;
				} 
			}
			var regex = /vector/;
			var vector = value_elt.tagName.match(regex);
			if ( value_elt.tagName == 'dict' ) {
				result[key] = parse_dict(value_elt);
			} 
			else if ( vector != null ) {
				var value = new Array();
				value.push(value_elt.tagName);
				var value_array = value_elt.textContent.split(/\s+/);
				for ( var i = 0; i < value_array.length; i++ ) {
					value.push(value_array[i]);
				}
				result[key] = value_array;
			}
			else if ( value_elt.tagName == 'any' ) {
				result[key] = [ 'any', value_elt ];
			}
			else {
				result[key] = [ value_elt.tagName, value_elt.textContent ];
			}
		}
	}
	return result;	
}
Phylo.Parsers.Nexml.parse_dict = parse_dict;
})()





