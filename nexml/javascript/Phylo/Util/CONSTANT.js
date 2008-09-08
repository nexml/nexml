var Constant = {};

Constant._NONE_         = function() { return 1;  }
Constant._NODE_         = function() { return 2;  }
Constant._TREE_         = function() { return 3;  }
Constant._FOREST_       = function() { return 4;  }
Constant._TAXON_        = function() { return 5;  }
Constant._TAXA_         = function() { return 6;  }
Constant._DATUM_        = function() { return 7;  }
Constant._MATRIX_       = function() { return 8;  }
Constant._MATRICES_     = function() { return 9;  }
Constant._SEQUENCE_     = function() { return 10; }
Constant._ALIGNMENT_    = function() { return 11; }
Constant._CHAR_         = function() { return 12; }
Constant._CHARSTATE_    = function() { return 13; }
Constant._CHARSTATESEQ_ = function() { return 14; }
Constant._MATRIXROW_    = function() { return 15; }

function looks_like_object (obj,constant) {
    if ( obj._type() == constant() ) {
        return true;
    }
    else {
        throw new ObjectMismatch("ObjectMismatch");
    }
}