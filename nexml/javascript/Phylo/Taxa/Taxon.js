Phylo.Taxa.Taxon = function (args) {
	if (args==null) args = {};	
	args["tag"] = "otu";
    this.XMLWritable(args);
    this._type      = Phylo.Util.CONSTANT._TAXON_;
    this._container = Phylo.Util.CONSTANT._TAXA_;
    return this;
}
copyPrototype(Phylo.Taxa.Taxon,Phylo.Util.XMLWritable);

Phylo.Taxa.Taxon.prototype.set_data = function (datum) {
	if ( looks_like_object( datum, Phylo.Util.CONSTANT._DATUM_ ) ) {
		Phylo.Mediators.TaxaMediator.set_link({
			'one'  : this,
			'many' : datum
		});
	}
	return this;
};

Phylo.Taxa.Taxon.prototype.set_nodes = function (node) {
	if ( looks_like_object( node, Phylo.Util.CONSTANT._NODE_ ) ) {
		Phylo.Mediators.TaxaMediator.set_link({
			'one'  : this,
			'many' : node
		});
	}
	return this;
};

Phylo.Taxa.Taxon.prototype.unset_datum = function (datum) {
	Phylo.Mediators.TaxaMediator.remove_link({
		'one'  : this,
		'many' : datum
	});
	return this;
};

Phylo.Taxa.Taxon.prototype.unset_node = function (node) {
	Phylo.Mediators.TaxaMediator.remove_link({
		'one'  : this,
		'many' : node
	});
	return this;
};

Phylo.Taxa.Taxon.prototype.get_data = function () {
    return Phylo.Mediators.TaxaMediator.get_link({
        'source' : this, 
        'type'   : Phylo.Util.CONSTANT._DATUM_()
    });
};

Phylo.Taxa.Taxon.prototype.get_nodes = function () {
    return Phylo.Mediators.TaxaMediator.get_link({
        'source' : this, 
        'type'   : Phylo.Util.CONSTANT._NODE_()
    });
};

Phylo.Taxa.Taxon.prototype._type = function() {
    return this._type;
};

Phylo.Taxa.Taxon.prototype._container = function() {
    return this._container;
};
