function Matrix(args) {
    this.TaxaLinker(args);
    this._type      = constant._MATRIX_;
    this._container = constant._NONE_;
    return this;
}
copyPrototype(Matrix,TaxaLinker);

Matrix.prototype.check_taxa = function () {};