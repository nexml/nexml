var singletonMediator;
var object = new Array();
var relationship = new Array();

function TaxaMediator(){
    if ( singletonMediator == null ) {
        singletonMediator = this;
    }
    return singletonMediator;
}

TaxaMediator.prototype.register = function(obj) {
    var id = obj.get_id();
    object[id] = obj;
    return this;
};

TaxaMediator.prototype.unregister = function(obj) {
    var id = obj.get_id();
    if ( object[id] != null ) {
        // one-to-many relationship
        if ( relationship[id] != null ) {
            delete relationship[id];
        }
        // one-to-one relationship
        else {
            for ( var i = 0; i < relationship.length; i++ ) {
                var relation = relationship[i];
                if ( relation[id] != null ) {
                    delete relation[id];
                    break;
                }
            }
        }
        delete object[id];
    }
    return this;
};

TaxaMediator.prototype.set_link = function(args) {
    var one  = args["one"];
    var many = args["many"];
    var one_id  = one.get_id();
    var many_id = many.get_id();
    for ( var i = 0; i < relationship.length; i++ ) {
        if ( relationship[i] != null && relationship[i][many_id.toString()] != null ) {
            delete relationship[i][many_id];
            break;
        }
    }
    if ( relationship[one_id] == null ) {
        relationship[one_id] = {};
    }
    relationship[one_id][many.get_id()] = many._type();
    return this;
};

TaxaMediator.prototype.get_link = function (args) {
    var id = args["source"].get_id();
    
    // have to get many objects of the same type
    if ( args["type"] != null ) {
        if ( relationship[id] == null ) {
            return null;
        }
        var result = new Array();
        for ( var key in relationship[id] ) {
            if ( relationship[id][key] == args["type"] ) {
                result.push(object[key]);
            }
        }
        return result;
    }
    
    // have to get just one
    else {
        for ( var i = 0; i < relationship.length; i++ ) {
            if ( relationship[i] != null && relationship[i][id] != null ) {
                return object[i];
            }
        }
    }
    return null;
};

TaxaMediator.prototype.remove_link = function(args) {
    var one = args["one"];
    var many = args["many"];
    if ( one != null ) {
        var one_id = one.get_id();
        var relation = relationship[one_id];
        if ( relation == null ) {
            return this;
        }
        var many_id = many.get_id();
        delete relation[many_id];
        return this;
    }
    else {
        var id = many.get_id();
        for ( var i = 0; i < relationship.length; i++ ) {
            var found_relation = relationship[i];
            if ( found_relation[id] != null ) {
                delete found_relation[id];
                break;
            }
        }
        return this;
    }
};