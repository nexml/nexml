Phylo.objects = {};

Phylo.id_pool = function () {
	var hidden_id = 0;
	function new_id () {
		return ++hidden_id;
	}
	return new_id;
}

Phylo.id_maker = Phylo.id_pool();

function Base (args){
	for ( var method in Base ) {
		if ( this[method] == null ) {
			this[method] = Base[method];
		}
	}
    if ( args != null ) {
        for ( var key in args ) {
            this[key] = args[key];
        }
    }
    this.id = Phylo.id_maker();
    if ( this.generic == null ) {
    	this.generic = {};
    }
    Phylo.objects[this.id] = this;
    Phylo.Mediators.TaxaMediator.register(this);
    return this;
}
Phylo.prototype = Base;

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

Phylo.prototype.get_internal_name = function () {
	return this.get_tag() + this.get_id();
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
    return Phylo.objects[id];
};

Phylo.prototype.VERSION = function () {
    var revision_string = '$Rev$';
    var regex = /(\d+)/i;
    var revision_number = revision_string.match(regex);
    return revision_number[0];
};
