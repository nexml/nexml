(function(){
function TaxaLinker () {
    return this;
}
Phylo.Taxa.TaxaLinker = TaxaLinker;

Phylo.Taxa.TaxaLinker.prototype.set_taxa = function(taxa) {
    if ( taxa != null && Phylo.Util.CONSTANT.looks_like_object( taxa, Phylo.Util.CONSTANT._TAXA_ ) ) {
        Phylo.Mediators.TaxaMediator.set_link( {
            "one"  : taxa, 
            "many" : this
        } );
    }
    else {
        Phylo.Mediators.TaxaMediator.remove_link( { "many" : this } );
    }
    this.check_taxa();
    return this;
};

Phylo.Taxa.TaxaLinker.prototype.unset_taxa = function () {
    this.set_taxa();
    return this;
};

Phylo.Taxa.TaxaLinker.prototype.get_taxa = function () {
    return Phylo.Mediators.TaxaMediator.get_link( { "source" : this } );
};

Phylo.Taxa.TaxaLinker.prototype.check_taxa = function () {
    throw new Phylo.Util.Exceptions.NotImplemented("Not implemented!");
};
})()