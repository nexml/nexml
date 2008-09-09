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
	resolve_tree_taxa(taxa_blocks,tree_blocks);
	for ( var key in taxa_blocks ) {
		result.push(taxa_blocks[key]);
	}
	for ( var i = 0; i < tree_blocks.length; i++ ) {
		result.push(tree_blocks[i]);
	}
	return result;
}
Phylo.Parsers.Nexml.parse = parse;

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
		var bl     = attrs.getNamedItem("length").value;
		var source = attrs.getNamedItem("source").value;		
		source_of[target] = source;
		target_of[source] = target;
		length_of[target] = bl;
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
		if ( children[i].tagName == 'DICT' ) {
			args["generic"] = { 'dict' : parse_dict(children[i]) };
			break;
		}
	}
	var tag_name = elt.nodeName.toLowerCase();
	switch(tag_name) {
		case 'otus' : return new Phylo.Taxa(args);
		case 'otu'  : return new Phylo.Taxa.Taxon(args);
		case 'trees': return new Phylo.Forest(args);
		case 'tree' : return new Phylo.Forest.Tree(args);
		case 'node' : return new Phylo.Forest.Node(args);
		default : throw new Phylo.Util.Exceptions.API("Can't create object from element " + tag_name);
	}
}
Phylo.Parsers.Nexml.obj_from_elt = obj_from_elt;

function parse_dict (elt) {
	var result = {};
	var children = elt.childNodes;
	for ( var i = 0; i < children.length; i = i + 2 ) {
		var key = children[i].firstChild.nodeValue;
		var value_elt = children[i+1];
		var regex = /vector/;
		var vector = value_elt.tagName.match(regex);
		if ( value_elt.tagName == 'DICT' ) {
			result[key] = parse_dict(value_elt);
		} 
		else if ( vector != null ) {
			var value = new Array();
			value.push(value_elt.tagName.toLowerCase());
			var value_array = value_elt.firstChild.nodeValue.split(/\s+/);
			for ( var i = 0; i < value_array.length; i++ ) {
				value.push(value_array[i]);
			}
			result[key] = value_array;
		}
		else if ( value_elt.tagName == 'ANY' ) {
			result[key] = value_elt;
		}
		else {
			result[key] = value_elt.firstChild.nodeValue;
		}
	}
	return result;	
}
Phylo.Parsers.Nexml.parse_dict = parse_dict;
})()





