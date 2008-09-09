var Phylo = {
	'Util' : {
		'Constant' : {}
	}	
};

Phylo.Util.Constant._NONE_         = function() { return 1;  }
Phylo.Util.Constant._NODE_         = function() { return 2;  }
Phylo.Util.Constant._TREE_         = function() { return 3;  }
Phylo.Util.Constant._FOREST_       = function() { return 4;  }
Phylo.Util.Constant._TAXON_        = function() { return 5;  }
Phylo.Util.Constant._TAXA_         = function() { return 6;  }
Phylo.Util.Constant._DATUM_        = function() { return 7;  }
Phylo.Util.Constant._MATRIX_       = function() { return 8;  }
Phylo.Util.Constant._MATRICES_     = function() { return 9;  }
Phylo.Util.Constant._SEQUENCE_     = function() { return 10; }
Phylo.Util.Constant._ALIGNMENT_    = function() { return 11; }
Phylo.Util.Constant._CHAR_         = function() { return 12; }
Phylo.Util.Constant._CHARSTATE_    = function() { return 13; }
Phylo.Util.Constant._CHARSTATESEQ_ = function() { return 14; }
Phylo.Util.Constant._MATRIXROW_    = function() { return 15; }

function looks_like_object (obj,constant) {
    if ( obj._type() == constant() ) {
        return true;
    }
    else {
        throw new Phylo.Util.Exceptions.ObjectMismatch("ObjectMismatch");
    }
}

function copyPrototype( descendant, parent ) {
	try {
	    var sConstructor = parent.toString();
	    var aMatch = sConstructor.match( /\s*function (.*)\(/ );
    	if ( aMatch != null ) { 
    		descendant.prototype[aMatch[1]] = parent; 
	    }
    	for (var m in parent.prototype) {
        	descendant.prototype[m] = parent.prototype[m];
    	}
	} catch (e) {
		alert(e.stack);
	}
}

function copyPrototypeMI( descendant, parents ) {
	try {
		for ( var i = 0; i < parents.length; i++ ) {
			var parent = parents[i];
	    	var sConstructor = parent.toString();
		    var aMatch = sConstructor.match( /\s*function (.*)\(/ );
		    if ( aMatch != null ) { descendant.prototype[aMatch[1]] = parent; }
	    	for (var m in parent.prototype) {
    	    	descendant.prototype[m] = parent.prototype[m];
	    	}
		}
	} catch(e) {
		alert(e.stack);
	}
}