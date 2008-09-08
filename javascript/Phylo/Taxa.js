function Taxa(args) {
	if (args==null) args = {};
	args["tag"] = "otus";	
    this.Listable(args);
    this._type      = Constant._TAXA_;
    this._container = Constant._NONE_;
    return this;
}
copyPrototype(Taxa,Listable);

Taxa.prototype.set_forest = function (forest) {
    if ( looks_like_object(forest,Constant._FOREST_) ) {
        forest.set_taxa(this);
    }
    return this;
};

Taxa.prototype.set_matrix = function (matrix) {
    if ( looks_like_object(matrix,Constant._MATRIX_) ) {
        matrix.set_taxa(this);
    }
    return this;
};

Taxa.prototype.unset_forest = function(forest) {
    if ( looks_like_object(forest,Constant._FOREST_) ) {
        forest.unset_taxa();
    }
    return this;
};

Taxa.prototype.unset_matrix = function(matrix) {
    if ( looks_like_object(matrix,Constant._MATRIX_) ) {
        matrix.unset_taxa();
    }
    return this;
};

Taxa.prototype.get_forests = function() {
    return TaxaMediator.get_link( {
        "source" : this,
        "type"   : Constant._FOREST_()
    } );
};

Taxa.prototype.get_matrices = function () {
    return TaxaMediator.get_link( {
        "source" : this,
        "type"   : Constant._MATRIX_()    
    } );
};

Taxa.prototype.get_ntax = function () {
    return this.get_entities().length;
};

Taxa.prototype._type = function () {
    return this._type;
};

Taxa.prototype._container = function() {
    return this._container;
};