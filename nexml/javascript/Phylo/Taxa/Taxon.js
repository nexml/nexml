function Taxon(args) {
	if (args==null) args = {};	
	args["tag"] = "otu";
    this.XMLWritable(args);
    this._type      = Constant._TAXON_;
    this._container = Constant._TAXA_;
    return this;
}
copyPrototype(Taxon,XMLWritable);

Taxon.prototype.set_data = function (datum) {
	if ( looks_like_object( datum, Constant._DATUM_ ) ) {
		TaxaMediator.set_link({
			'one'  : this,
			'many' : datum
		});
	}
	return this;
};

Taxon.prototype.set_nodes = function (node) {
	if ( looks_like_object( node, Constant._NODE_ ) ) {
		TaxaMediator.set_link({
			'one'  : this,
			'many' : node
		});
	}
	return this;
};

Taxon.prototype.unset_datum = function (datum) {
	TaxaMediator.remove_link({
		'one'  : this,
		'many' : datum
	});
	return this;
};

Taxon.prototype.unset_node = function (node) {
	TaxaMediator.remove_link({
		'one'  : this,
		'many' : node
	});
	return this;
};

Taxon.prototype.get_data = function () {
    return TaxaMediator.get_link({
        'source' : this, 
        'type'   : Constant._DATUM_()
    });
};

Taxon.prototype.get_nodes = function () {
    return TaxaMediator.get_link({
        'source' : this, 
        'type'   : Constant._NODE_()
    });
};

Taxon.prototype._type = function() {
    return this._type;
};

Taxon.prototype._container = function() {
    return this._container;
};
