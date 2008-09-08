function TaxonLinker() {
    return this;
}

TaxonLinker.prototype.set_taxon = function(taxon) {
    if ( taxon != null && looks_like_object( taxon, Constant._TAXON_ ) ) {
        TaxaMediator.set_link( {
            "one"  : taxon, 
            "many" : this
        } );
    }
    else {
        TaxaMediator.remove_link( { "many" : this } );
    }
    return this;
};

TaxonLinker.prototype.unset_taxon = function () {
    this.set_taxon();
    return this;
};

TaxonLinker.prototype.get_taxon = function () {
    return TaxaMediator.get_link( { "source" : this } );
};