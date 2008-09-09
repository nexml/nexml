Phylo.Forest = function (args) {
	if (args==null) args = {};
	args["tag"] = "trees";
    this.Listable(args);
    this.TaxaLinker(args);
    this._type      = Phylo.Util.CONSTANT._FOREST_;
    this._container = Phylo.Util.CONSTANT._NONE_;
    return this;
}
copyPrototypeMI(Phylo.Forest,[Phylo.Listable,Phylo.Taxa.TaxaLinker]);

Phylo.Forest.prototype.check_taxa = function () {};