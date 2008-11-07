Phylo.Taxa = function (args) {
	if (args==null) args = {};
	args["tag"] = "otus";	
    this.Listable(args);
    this._type      = Phylo.Util.CONSTANT._TAXA_;
    this._container = Phylo.Util.CONSTANT._PROJECT_;
    return this;
}
Phylo.Util.CONSTANT.copyPrototype(Phylo.Taxa,Phylo.Listable);

Phylo.Taxa.prototype.set_forest = function (forest) {
    if ( Phylo.Util.CONSTANT.looks_like_object(forest,Phylo.Util.CONSTANT._FOREST_) ) {
        forest.set_taxa(this);
    }
    return this;
};

Phylo.Taxa.prototype.set_matrix = function (matrix) {
    if ( Phylo.Util.CONSTANT.looks_like_object(matrix,Phylo.Util.CONSTANT._MATRIX_) ) {
        matrix.set_taxa(this);
    }
    return this;
};

Phylo.Taxa.prototype.unset_forest = function(forest) {
    if ( Phylo.Util.CONSTANT.looks_like_object(forest,Phylo.Util.CONSTANT._FOREST_) ) {
        forest.unset_taxa();
    }
    return this;
};

Phylo.Taxa.prototype.unset_matrix = function(matrix) {
    if ( Phylo.Util.CONSTANT.looks_like_object(matrix,Phylo.Util.CONSTANT._MATRIX_) ) {
        matrix.unset_taxa();
    }
    return this;
};

Phylo.Taxa.prototype.get_forests = function() {
    return Phylo.Mediators.TaxaMediator.get_link( {
        "source" : this,
        "type"   : Phylo.Util.CONSTANT._FOREST_()
    } );
};

Phylo.Taxa.prototype.get_matrices = function () {
    return TaxaMediator.get_link( {
        "source" : this,
        "type"   : Phylo.Util.CONSTANT._MATRIX_()    
    } );
};

Phylo.Taxa.prototype.get_ntax = function () {
    return this.get_entities().length;
};

Phylo.Taxa.prototype._type = function () {
    return this._type;
};

Phylo.Taxa.prototype._container = function() {
    return this._container;
};