var mediator = new TaxaMediator();
var constant = new Constant();

function TaxonLinker() {
    return this;
}

TaxonLinker.prototype.set_taxon = function(taxon) {
    if ( taxon != null && looks_like_object( taxon, constant._TAXON_ ) ) {
        mediator.set_link( {
            "one"  : taxon, 
            "many" : this
        } );
    }
    else {
        mediator.remove_link( { "many" : this } );
    }
    return this;
};

TaxonLinker.prototype.unset_taxon = function () {
    this.set_taxon();
    return this;
};

TaxonLinker.prototype.get_taxon = function () {
    return mediator.get_link( { "source" : this } );
};