function Forest(args) {
	if (args==null) args = {};
	args["tag"] = "trees";
    this.Listable(args);
    this.TaxaLinker(args);
    this._type      = Constant._FOREST_;
    this._container = Constant._NONE_;
    return this;
}
copyPrototypeMI(Forest,[Listable,TaxaLinker]);

Forest.prototype.check_taxa = function () {};