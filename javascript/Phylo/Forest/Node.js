(function(){
function Node (args) {
	if (args==null) args = {};
	args["tag"] = "node";
    this.XMLWritable(args);
    this.children   = [];
    this._type      = Phylo.Util.CONSTANT._NODE_;
    this._container = Phylo.Util.CONSTANT._TREE_;
    return this;
}
Phylo.Forest.Node = Node;
copyPrototypeMI(Phylo.Forest.Node,[Phylo.Util.XMLWritable,Phylo.Taxa.TaxonLinker]);
var proto = Phylo.Forest.Node.prototype;

proto.set_parent = function (parent) {
    this.parent = parent;
    return this;
};

proto.set_child = function (child) {
    this.children.push(child);
    return this;
};

proto.set_first_daughter = function (child) {
    this.children.unshift(child);
    return this;
};

proto.set_last_daughter = function (child) {
    this.children.push(child);
    return this;
};

proto.set_previous_sister = function (sister) {
    var parent = this.get_parent();
    if ( parent != null ) {
        var children = parent.get_children();
        for ( var i = 0; i < children.length; i++ ) {
            if ( children[i].get_id == this.get_id ) {
                if ( i > 0 ) {
                    for ( var j = children.length; j >= i; j-- ) {
                        children[j] = children[j-1];
                    }
                    children[i-1] = sister;
                }
                else {
                    children.unshift(sister);
                }
            }
        }
    }
    return this;
};

proto.set_next_sister = function (sister) {
    var parent = this.get_parent();
    if ( parent != null ) {
        var children = parent.get_children();
        for ( var i = 0; i < children.length; i++ ) {
            if ( children[i].get_id == this.get_id ) {
                for ( var j = children.length; j > i; j-- ) {
                    children[j+1] = children[j];
                }
                children[i+1] = sister;
            }
        }
    }
    return this;
};

proto.set_branch_length = function (branch_length) {
    this.branch_length = Number(branch_length);
    return this;
};

proto.set_node_below = function () {
    var new_parent = new Phylo.Forest.Node();
    var parent = this.get_parent();
    if ( parent != null ) {
        parent.set_child(new_parent);
        new_parent.set_child(this);
        new_parent.set_parent(parent);
        this.set_parent(new_parent);
        parent.prune_child(this);
    }
    else {
        this.set_parent(new_parent);
        new_parent.set_child(this);
    }
    return new_parent;   
};

proto.prune_child = function (child) {
    for ( var i = 0; i < this.children.length; i++ ) {
        if ( this.children[i].get_id() == child.get_id() ) {
            this.children.splice(i,1);
            return this;
        }
    }
    return this;
};

proto.get_parent = function () {
    return this.parent;
};

proto.get_children = function () {
    return this.children;
};

proto.get_first_daughter = function () {
    if ( this.children[0] != null ) {
    	return this.children[0];
    }
    else {
    	return null;
    }
};

proto.get_last_daughter = function () {
    for ( var i = 0; i < this.children.length; i++ ) {
        if ( this.children[i+1] == null ) {
            return this.children[i];
        }
    }
    return null;
};

proto.get_next_sister = function () {
    var parent = this.get_parent();
    if ( parent != null ) {
        var siblings = parent.get_children();
        for ( var i = 0; i < siblings.length; i++ ) {
            if ( siblings[i].get_id() == this.get_id() ) {
                if ( i+1 <= siblings.length ) {
                	if ( siblings[i+1] != null ) {
                    	return siblings[i+1];
                	}
                	else {
                		return null;
                	}
                }
            }
        }
    }
    return null;
};

proto.get_previous_sister = function () {
    var parent = this.get_parent();
    if ( parent != null ) {
        var siblings = parent.get_children();
        for ( var i = 0; i < siblings.length; i++ ) {
            if ( siblings[i].get_id() == this.get_id() ) {
                if ( i > 0 ) {
                	if ( siblings[i-1] != null ) {
                    	return siblings[i-1];
                	}
                	else {
                		return null;
                	}
                }
            }
        }
    }
    return null;
};

proto.get_branch_length = function () {
    return this.branch_length;
};

proto.get_descendants = function () {
	var descendants = new Array();
	this.visit_depth_first( {
		"pre" : function(node) {
			descendants.push(node);
		}
	} );
	descendants.shift(); // remove starting node
	return descendants;
};

proto.get_ancestors = function () {
	var ancestors = new Array();
	var node = this.get_parent();
	while ( node != null ) {
		ancestors.push(node);
		node = node.get_parent();
	}
	return ancestors;
};

proto.is_terminal = function () {
    return this.children.length == 0 ? true : false;
};

proto.is_internal = function () {
    return this.children.length == 0 ? false : true;
};

proto.is_root = function () {  
    return this.get_parent() == null ? true : false;
};

proto.is_first = function () { 
    return this.get_previous_sister() == null ? true : false;
};

proto.is_last = function () { 
    return this.get_next_sister() == null ? true : false;
};

proto.is_descendant_of = function (ancestor) {
    var node = this;
    while ( node != null ) {
        node = node.get_parent();
        if ( node.get_id() == ancestor.get_id() ) {
            return true;
        }
    }
    return false;
};

proto.is_ancestor_of = function (descendant) {
    while( descendant != null ) {
        descendant = descendant.get_parent();
        if ( descendant.get_id() == this.get_id() ) {
            return true;
        }
    }
    return false;
};

proto.calc_nodes_to_root = function () {
	var n = 0;
	var node = this;
	while(node) {
		node = node.get_parent();
		n++;
	}
	return n;
};

proto.calc_max_path_to_tips = function () {
	var maxpath = 0;
	var root = this;
	this.visit_depth_first({
		'pre' : function (n) {
			if ( n.is_terminal() ) {
				var node = n;
				var path = 0;
				while( node.get_id() != root.get_id() ) {
					path += node.get_branch_length();
					node = node.get_parent();
				}
				if ( path > maxpath ) maxpath = path;
			}
		}
	});
	return maxpath;
};

proto.calc_max_nodes_to_tips = function () {
	var maxnodes = 0;
	var root = this;
	this.visit_depth_first({
		'pre' : function (n) {
			if ( n.is_terminal() ) {
				var node = n;
				var nodes = 0;
				while( node.get_id() != root.get_id() ) {
					nodes++;
					node = node.get_parent();
				}
				if ( nodes > maxnodes ) maxnodes = nodes;
			}
		}
	});
	return maxnodes;
};

proto.visit_depth_first = function (args) {
    if (args==null) args = {};
    if (args["pre"]!=null) args["pre"](this);
    
    // daughter related code refs
    var fd = this.get_first_daughter();
    if ( fd != null ) {
        if (args["pre_daughter"]!=null) args["pre_daughter"](this);
        fd.visit_depth_first(args);
        if (args["post_daughter"]!=null) args["post_daughter"](this);
    }
    else {
        if (args["no_daughter"]!=null) args["no_daughter"](this);
    }
    
    // in-order code ref
    if (args["in"]!=null) args["in"](this);
    
    // sister related code refs
    var ns = this.get_next_sister();
    if ( ns != null ) {
        if (args["pre_sister"]!=null) args["pre_sister"](this);
        ns.visit_depth_first(args);
        if (args["post_sister"]!=null) args["post_sister"](this);
    }
    else {
        if (args["no_sister"]!=null) args["no_sister"](this);
    }    
        
    if (args["post"]!=null) args["post"](this);
};

var newick_string = new String();
proto.to_newick = function (args) {
    var name = this.get_name();
    var branch_length = this.get_branch_length();
    if ( this.is_internal() ) {
        newick_string += '(';
        this.get_first_daughter().to_newick(args);
        newick_string += ')';
    }
    if ( this.get_name() != null ) {
    	newick_string += this.get_name();
    }
    else if ( this.get_taxon() != null && this.get_taxon().get_name() != null ) {
    	newick_string += this.get_taxon().get_name();
    }
    if ( this.get_branch_length() != null ) {
    	newick_string += ':' + this.get_branch_length();
    }
    if ( args != null && args['nhxkeys'] != null ) {
    	var nhx = '[&&NHX:';
    	for ( var i = 0; i < args['nhxkeys'].length; i++ ) {
    		nhx += args['nhxkeys'][i] + '=';
    		nhx += this.get_generic(args['nhxkeys'][i]) + ':';
    	}
    	nhx += ']';
    	newick_string += nhx;
    }
    if ( this.is_root() ) {
        var result = newick_string + ';';
        newick_string = new String();
        return result;
    }
    else if ( this.get_next_sister() != null ) {
        newick_string += ',';
        this.get_next_sister().to_newick(args);
    }
    return null;
};

proto.to_xml = function () {
	var nodes = new Array();
	var desc = this.get_descendants();
	nodes.push(this);
	for ( var i = 0; i < desc.length; i++ ) {
		nodes.push(desc[i]);
	}
	var xml = '';
	
	//first write out the node elements
	for ( var i = 0; i < nodes.length; i++ ) {
		var taxon = nodes[i].get_taxon();
		if ( taxon != null ) {
			nodes[i].set_attributes( { 'otu' : taxon.get_xml_id() } );
		}
		if ( nodes[i].is_root() ) {
			nodes[i].set_attributes( { 'root' : true } );
		}
		xml += nodes[i].get_xml_tag(true);
	}
	
	//then the rootedge?
	var root_length = nodes.shift().get_branch_length();
	if ( root_length != null && root_length != 0 ) {
		var target = this.get_xml_id();
		var id = "edge" + this.get_id();
		xml += '<rootedge target="' + target + '" id="' + id + '" length="' + root_length + '"/>';
	}
	
	//then the subtended edges
	for ( var i = 0; i < nodes.length; i++ ) {
		var source = nodes[i].get_parent().get_xml_id();
		var target = nodes[i].get_xml_id();
		var id = "edge" + nodes[i].get_id();
		var branch_length = nodes[i].get_branch_length();
		xml += '<edge source="' + source;
		xml += '" target="' + target;
		xml += '" id="' + id;
		if ( branch_length != null ) xml += '" length="' + branch_length;
		xml += '"/>';
	}		
		
	return xml;
}
})()