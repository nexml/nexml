(function(){
function Listable (args) {
    this.XMLWritable(args);
    this.entities = [];
    this.index = 0;
    return this;
}
Phylo.Listable = Listable;
copyPrototype(Phylo.Listable,Phylo.Util.XMLWritable);
var proto = Phylo.Listable.prototype;

proto.get_entities = function() {
    return this.entities;
}

proto.first = function() {
    var i = 0;
    this.index = i;
    return this.entities[i];
}

proto.last = function() {
    var i = this.entities.length - 1;
    this.index = i;
    return this.entities[i];
}

proto.last_index = function() {
    return this.entities.length - 1;
}

proto.current = function() {
    return this.entities[ this.index ];
}

proto.current_index = function() {
    return this.index;
}

proto.next = function() {
    var i = this.index + 1;
    this.index = i;
    return this.entities[i];
}

proto.previous = function() {
    var i = this.index - 1;
    this.index = i;
    return this.entities[i];
}

proto.exists = function(i) {
    return Boolean(this.entities[i]);
}

proto.contains = function(obj) {
    var ents = this.get_entities();
    for ( var i = 0; i < ents.length; i++ ) {
        if ( ents[i].get_id == obj.get_id ) {
            return true;
        }
    }
    return false;
}

proto.can_contain = function(array) {
    if ( array instanceof Array ) {
        for ( var i = 0; i < array.length; i++ ) {
            if ( this._type != array[i]._container ) {
                return false;
            }
        }	
    }
    else {
        if ( this._type != array._container ) {
            return false;
        }
    }
    return true;
}

proto.insert = function(array) {
    if ( this.can_contain(array) ) {
        if ( array instanceof Array ) {
            for ( var i = 0; i < array.length; i++ ) {
                this.entities.push(array[i]);
            }		
        }
        else {
            this.entities.push(array);
        }
    }
    else {
        throw new Phylo.Util.Exceptions.ObjectMismatch("ObjectMismatch");
    }
}

proto.insert_at_index = function(obj,i) {
    if ( this.can_contain(obj) ) {
        this.entities[i] = obj;
    }
    else {
        throw new Phylo.Util.Exceptions.ObjectMismatch("ObjectMismatch");		
    }
}


// Listable.prototype.delete = function(obj) {
// 	var ents = this.entities;
// 	for ( var i = ents.length - 1; i >= 0; i-- ) {
// 		if ( ents[i].get_id == obj.get_id ) {
// 			ents.splice(i,1);
// 		}
// 	}
// 	this.entities = ents;
// }


proto.clear = function() {
    this.entities = [];
    this.index = 0;
}

proto.visit = function(func) {
    for ( var i = 0; i < this.entities.length; i++ ) {
        func( this.entities[i] );
    }
}

proto.get_by_index = function(i) {
    return this.entities[i];
}

proto.get_index_of = function(obj) {
    var ents = this.entities;
    for ( var i = 0; i < ents.length; i++ ) {
        if ( ents[i].get_id() == obj.get_id() ) {
            return i;
        }
    }
    return null;
}

proto.get_by_regular_expression = function(obj) {
    var regex  = obj["match"];
    var method = obj["value"];
    var result = [];
    var ents   = this.entities;
    for ( var i = 0; i < ents.length; i++ ) {
        var ent = ents[i];
        if ( regex.test( ent[method] ) ) {
            result.push( ent );
        }
    }
    return result;
}
})()
