Phylo.Mediators = {
	'TaxaMediator' : {
		'object'       : [],
		'relationship' : []
	}
};

Phylo.Mediators.TaxaMediator.register = function(obj) {
    var id = obj.get_id();
    Phylo.Mediators.TaxaMediator.object[id] = obj;
    return this;
};

Phylo.Mediators.TaxaMediator.unregister = function(obj) {
    var id = obj.get_id();
    if ( Phylo.Mediators.TaxaMediator.object[id] != null ) {
        // one-to-many relationship
        if ( Phylo.Mediators.TaxaMediator.relationship[id] != null ) {
            delete Phylo.Mediators.TaxaMediator.relationship[id];
        }
        // one-to-one relationship
        else {
            for ( var i = 0; i < Phylo.Mediators.TaxaMediator.relationship.length; i++ ) {
                var relation = Phylo.Mediators.TaxaMediator.relationship[i];
                if ( relation[id] != null ) {
                    delete relation[id];
                    break;
                }
            }
        }
        delete Phylo.Mediators.TaxaMediator.object[id];
    }
    return this;
};

Phylo.Mediators.TaxaMediator.set_link = function(args) {
    var one  = args["one"];
    var many = args["many"];
    var one_id  = one.get_id();
    var many_id = many.get_id();
    for ( var i = 0; i < Phylo.Mediators.TaxaMediator.relationship.length; i++ ) {
        if ( Phylo.Mediators.TaxaMediator.relationship[i] != null && Phylo.Mediators.TaxaMediator.relationship[i][many_id.toString()] != null ) {
            delete Phylo.Mediators.TaxaMediator.relationship[i][many_id];
            break;
        }
    }
    if ( Phylo.Mediators.TaxaMediator.relationship[one_id] == null ) {
        Phylo.Mediators.TaxaMediator.relationship[one_id] = {};
    }
    Phylo.Mediators.TaxaMediator.relationship[one_id][many.get_id()] = many._type();
    return this;
};

Phylo.Mediators.TaxaMediator.get_link = function (args) {
    var id = args["source"].get_id();
    
    // have to get many objects of the same type
    if ( args["type"] != null ) {
        if ( Phylo.Mediators.TaxaMediator.relationship[id] == null ) {
            return null;
        }
        var result = new Array();
        for ( var key in Phylo.Mediators.TaxaMediator.relationship[id] ) {
            if ( Phylo.Mediators.TaxaMediator.relationship[id][key] == args["type"] ) {
                result.push(Phylo.Mediators.TaxaMediator.object[key]);
            }
        }
        return result;
    }
    
    // have to get just one
    else {
        for ( var i = 0; i < Phylo.Mediators.TaxaMediator.relationship.length; i++ ) {
            if ( Phylo.Mediators.TaxaMediator.relationship[i] != null && Phylo.Mediators.TaxaMediator.relationship[i][id] != null ) {
                return Phylo.Mediators.TaxaMediator.object[i];
            }
        }
    }
    return null;
};

Phylo.Mediators.TaxaMediator.remove_link = function(args) {
    var one = args["one"];
    var many = args["many"];
    if ( one != null ) {
        var one_id = one.get_id();
        var relation = Phylo.Mediators.TaxaMediator.relationship[one_id];
        if ( relation == null ) {
            return this;
        }
        var many_id = many.get_id();
        delete relation[many_id];
        return this;
    }
    else {
        var id = many.get_id();
        for ( var i = 0; i < Phylo.Mediators.TaxaMediator.relationship.length; i++ ) {
            var found_relation = Phylo.Mediators.TaxaMediator.relationship[i];
            if ( found_relation[id] != null ) {
                delete found_relation[id];
                break;
            }
        }
        return this;
    }
};