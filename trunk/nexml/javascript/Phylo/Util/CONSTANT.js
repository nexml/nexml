var Phylo = {
	'Util' : {
		'CONSTANT' : {}
	}	
};

Phylo.Util.CONSTANT._NONE_         = function() { return 1;  }
Phylo.Util.CONSTANT._NODE_         = function() { return 2;  }
Phylo.Util.CONSTANT._TREE_         = function() { return 3;  }
Phylo.Util.CONSTANT._FOREST_       = function() { return 4;  }
Phylo.Util.CONSTANT._TAXON_        = function() { return 5;  }
Phylo.Util.CONSTANT._TAXA_         = function() { return 6;  }
Phylo.Util.CONSTANT._DATUM_        = function() { return 7;  }
Phylo.Util.CONSTANT._MATRIX_       = function() { return 8;  }
Phylo.Util.CONSTANT._MATRICES_     = function() { return 9;  }
Phylo.Util.CONSTANT._SEQUENCE_     = function() { return 10; }
Phylo.Util.CONSTANT._ALIGNMENT_    = function() { return 11; }
Phylo.Util.CONSTANT._CHAR_         = function() { return 12; }
Phylo.Util.CONSTANT._CHARSTATE_    = function() { return 13; }
Phylo.Util.CONSTANT._CHARSTATESEQ_ = function() { return 14; }
Phylo.Util.CONSTANT._MATRIXROW_    = function() { return 15; }

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