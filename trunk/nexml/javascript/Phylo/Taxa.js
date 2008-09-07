var constant = new Constant();
var mediator = new TaxaMediator();

function Taxa(args) {
	if (args==null) args = {};
	args["tag"] = "otus";	
    this.Listable(args);
    this._type      = constant._TAXA_;
    this._container = constant._NONE_;
    return this;
}
copyPrototype(Taxa,Listable);

Taxa.prototype.set_forest = function (forest) {
    if ( looks_like_object(forest,constant._FOREST_) ) {
        forest.set_taxa(this);
    }
    return this;
};

Taxa.prototype.set_matrix = function (matrix) {
    if ( looks_like_object(matrix,constant._MATRIX_) ) {
        matrix.set_taxa(this);
    }
    return this;
};

Taxa.prototype.unset_forest = function(forest) {
    if ( looks_like_object(forest,constant._FOREST_) ) {
        forest.unset_taxa();
    }
    return this;
};

Taxa.prototype.unset_matrix = function(matrix) {
    if ( looks_like_object(matrix,constant._MATRIX_) ) {
        matrix.unset_taxa();
    }
    return this;
};

Taxa.prototype.get_forests = function() {
    return mediator.get_link( {
        "source" : this,
        "type"   : constant._FOREST_()
    } );
};

Taxa.prototype.get_matrices = function () {
    return mediator.get_link( {
        "source" : this,
        "type"   : constant._MATRIX_()    
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