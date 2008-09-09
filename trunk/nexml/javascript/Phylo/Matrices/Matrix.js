Phylo.Matrices.Matrix = function (args) {
    this.Phylo.Taxa.TaxaLinker(args);
    this._type      = Phylo.Util.Constant._MATRIX_;
    this._container = Phylo.Util.Constant._NONE_;
    return this;
}
copyPrototype(Phylo.Matrices.Matrix,Phylo.Taxa.TaxaLinker);

Phylo.Matrices.Matrix.prototype.check_taxa = function () {};