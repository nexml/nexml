function Forest(args) {
	if (args==null) args = {};
	args["tag"] = "trees";
    this.Listable(args);
    this.TaxaLinker(args);
    this._type      = constant._FOREST_;
    this._container = constant._NONE_;
    return this;
}
copyPrototypeMI(Forest,[Listable,TaxaLinker]);

Forest.prototype.check_taxa = function () {};