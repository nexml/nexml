Phylo.Taxa.TaxonLinker = function () {
    return this;
}

Phylo.Taxa.TaxonLinker.prototype.set_taxon = function(taxon) {
    if ( taxon != null && Phylo.Util.CONSTANT.looks_like_object( taxon, Phylo.Util.CONSTANT._TAXON_ ) ) {
        Phylo.Mediators.TaxaMediator.set_link( {
            "one"  : taxon, 
            "many" : this
        } );
    }
    else {
        Phylo.Mediators.TaxaMediator.remove_link( { "many" : this } );
    }
    return this;
};

Phylo.Taxa.TaxonLinker.prototype.unset_taxon = function () {
    this.set_taxon();
    return this;
};

Phylo.Taxa.TaxonLinker.prototype.get_taxon = function () {
    return Phylo.Mediators.TaxaMediator.get_link( { "source" : this } );
};