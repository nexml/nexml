function Constant () {
}

Constant.prototype._NONE_         = function() { return 1;  }
Constant.prototype._NODE_         = function() { return 2;  }
Constant.prototype._TREE_         = function() { return 3;  }
Constant.prototype._FOREST_       = function() { return 4;  }
Constant.prototype._TAXON_        = function() { return 5;  }
Constant.prototype._TAXA_         = function() { return 6;  }
Constant.prototype._DATUM_        = function() { return 7;  }
Constant.prototype._MATRIX_       = function() { return 8;  }
Constant.prototype._MATRICES_     = function() { return 9;  }
Constant.prototype._SEQUENCE_     = function() { return 10; }
Constant.prototype._ALIGNMENT_    = function() { return 11; }
Constant.prototype._CHAR_         = function() { return 12; }
Constant.prototype._CHARSTATE_    = function() { return 13; }
Constant.prototype._CHARSTATESEQ_ = function() { return 14; }
Constant.prototype._MATRIXROW_    = function() { return 15; }

function looks_like_object (obj,constant) {
    if ( obj._type() == constant() ) {
        return true;
    }
    else {
        throw new ObjectMismatch("ObjectMismatch");
    }
}