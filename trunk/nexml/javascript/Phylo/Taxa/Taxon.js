var constant = new Constant();
var mediator = new TaxaMediator();

function Taxon(args) {
	if (args==null) args = {};	
	args["tag"] = "otu";
    this.XMLWritable(args);
    this._type      = constant._TAXON_;
    this._container = constant._TAXA_;
    return this;
}
copyPrototype(Taxon,XMLWritable);

Taxon.prototype.set_data = function (datum) {
	if ( looks_like_object( datum, constant._DATUM_ ) ) {
		mediator.set_link({
			'one'  : this,
			'many' : datum
		});
	}
	return this;
};

Taxon.prototype.set_nodes = function (node) {
	if ( looks_like_object( node, constant._NODE_ ) ) {
		mediator.set_link({
			'one'  : this,
			'many' : node
		});
	}
	return this;
};

Taxon.prototype.unset_datum = function (datum) {
	mediator.remove_link({
		'one'  : this,
		'many' : datum
	});
	return this;
};

Taxon.prototype.unset_node = function (node) {
	mediator.remove_link({
		'one'  : this,
		'many' : node
	});
	return this;
};

Taxon.prototype.get_data = function () {
    return mediator.get_link({
        'source' : this, 
        'type'   : constant._DATUM_()
    });
};

Taxon.prototype.get_nodes = function () {
    return mediator.get_link({
        'source' : this, 
        'type'   : constant._NODE_()
    });
};

Taxon.prototype._type = function() {
    return this._type;
};

Taxon.prototype._container = function() {
    return this._container;
};
