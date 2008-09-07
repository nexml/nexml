var mediator = new TaxaMediator();
var constant = new Constant();

function TaxaLinker() {
    return this;
}

TaxaLinker.prototype.set_taxa = function(taxa) {
    if ( taxa != null && looks_like_object( taxa, constant._TAXA_ ) ) {
        mediator.set_link( {
            "one"  : taxa, 
            "many" : this
        } );
    }
    else {
        mediator.remove_link( { "many" : this } );
    }
    this.check_taxa();
    return this;
};

TaxaLinker.prototype.unset_taxa = function () {
    this.set_taxa();
    return this;
};

TaxaLinker.prototype.get_taxa = function () {
    return mediator.get_link( { "source" : this } );
};

TaxaLinker.prototype.check_taxa = function () {
    throw new NotImplemented("Not implemented!");
};