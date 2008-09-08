function copyPrototype( descendant, parent ) {
    var sConstructor = parent.toString();
    var aMatch = sConstructor.match( /\s*function (.*)\(/ );
    if ( aMatch != null ) { descendant.prototype[aMatch[1]] = parent; }
    for (var m in parent.prototype) {
        descendant.prototype[m] = parent.prototype[m];
    }
}

function copyPrototypeMI( descendant, parents ) {
	for ( var i = 0; i < parents.length; i++ ) {
		var parent = parents[i];
	    var sConstructor = parent.toString();
	    var aMatch = sConstructor.match( /\s*function (.*)\(/ );
	    if ( aMatch != null ) { descendant.prototype[aMatch[1]] = parent; }
	    for (var m in parent.prototype) {
    	    descendant.prototype[m] = parent.prototype[m];
    	}
	}
}

function invoke( obj, method, args ) {
	var request_hash = {};
	var url = '/cgi-bin/phylormi';
	if ( obj != null ) {
		if ( obj instanceof String ) {
			request_hash["class"] = obj;
		}
		else {
			request_hash["obj"] = obj.get_id();
		}
	}
	request_hash["method"] = method;
	request_hash["args"] = args;
	//var req = new Ajax.Request( url, request_hash );
}


    var id      = 0;
    var objects = {};
    
    function Phylo(args){
        if ( args != null ) {
            for ( var key in args ) {
                this[key] = args[key];
            }
        }
        this.id = id++;
        this.generic = {};
        objects[this.id] = this;
        TaxaMediator.register(this);
        return this;
    }
    
    Phylo.prototype.set_name = function (name) {
        this.name = String(name);
        return this;
    };
    
    Phylo.prototype.set_desc = function (desc) {
        this.desc = String(desc);
        return this;
    };
    
    Phylo.prototype.set_score = function(score) {
        this.score = Number(score);
        return this;
    };
    
    Phylo.prototype.set_generic = function(generic) {
    	for ( var key in generic ) {
        	this.generic[key] = generic[key];
    	}
        return this;
    };
    
    Phylo.prototype.get_name = function() {
        return this.name;
    };
    
    Phylo.prototype.get_desc = function() {
        return this.desc;
    };
    
    Phylo.prototype.get_score = function() {
        return this.score;	
    };
    
    Phylo.prototype.get_generic = function(key) {
        if ( key != null ) {
        	if ( this.generic[key] != null ) {
            	return this.generic[key];
        	}
        	else {
        		return null;
        	}
        }
        else {
            return this.generic;
        }
    };
    
    Phylo.prototype.get_id = function() {
        return this.id;
    };
    
    Phylo.prototype.get_obj_by_id = function(id) {
        return objects[id];
    };
    
    Phylo.prototype.VERSION = function () {
    	var revision_string = '$Rev$';
    	var regex = /(\d+)/i;
		var revision_number = revision_string.match(regex);
		return revision_number;
    };
