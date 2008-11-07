(function(){
function Matrices (args) {
	if (args==null) args = {};
    this.Listable(args);
    this._type      = Phylo.Util.CONSTANT._MATRICES_;
    this._container = Phylo.Util.CONSTANT._NONE_;
    return this;
}
Phylo.Matrices = Matrices;
Phylo.Util.CONSTANT.copyPrototype(Phylo.Matrices,Phylo.Listable);
})()