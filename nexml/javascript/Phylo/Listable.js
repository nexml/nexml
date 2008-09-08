function Listable (args) {
    this.XMLWritable(args);
    this.entities = [];
    this.index = 0;
    return this;
}
copyPrototype(Listable,XMLWritable);

Listable.prototype.get_entities = function() {
    return this.entities;
}

Listable.prototype.first = function() {
    var i = 0;
    this.index = i;
    return this.entities[i];
}

Listable.prototype.last = function() {
    var i = this.entities.length - 1;
    this.index = i;
    return this.entities[i];
}

Listable.prototype.last_index = function() {
    return this.entities.length - 1;
}

Listable.prototype.current = function() {
    return this.entities[ this.index ];
}

Listable.prototype.current_index = function() {
    return this.index;
}

Listable.prototype.next = function() {
    var i = this.index + 1;
    this.index = i;
    return this.entities[i];
}

Listable.prototype.previous = function() {
    var i = this.index - 1;
    this.index = i;
    return this.entities[i];
}

Listable.prototype.exists = function(i) {
    return Boolean(this.entities[i]);
}

Listable.prototype.contains = function(obj) {
    var ents = this.get_entities();
    for ( var i = 0; i < ents.length; i++ ) {
        if ( ents[i].get_id == obj.get_id ) {
            return true;
        }
    }
    return false;
}

Listable.prototype.can_contain = function(array) {
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

Listable.prototype.insert = function(array) {
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
        throw new ObjectMismatch("ObjectMismatch");
    }
}

Listable.prototype.insert_at_index = function(obj,i) {
    if ( this.can_contain(obj) ) {
        this.entities[i] = obj;
    }
    else {
        throw new ObjectMismatch("ObjectMismatch");		
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


Listable.prototype.clear = function() {
    this.entities = [];
    this.index = 0;
}

Listable.prototype.visit = function(func) {
    for ( var i = 0; i < this.entities.length; i++ ) {
        func( this.entities[i] );
    }
}

Listable.prototype.get_by_index = function(i) {
    return this.entities[i];
}

Listable.prototype.get_index_of = function(obj) {
    var ents = this.entities;
    for ( var i = 0; i < ents.length; i++ ) {
        if ( ents[i].get_id() == obj.get_id() ) {
            return i;
        }
    }
    return null;
}

Listable.prototype.get_by_regular_expression = function(obj) {
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
