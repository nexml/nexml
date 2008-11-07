(function(){
function TypeSafeData(args) {
	if (args==null) args = {};
	if ( ! args["type"] && ! args["type_object"] ) {
		args["type"] = 'Standard';
	}
	if ( args["type"] ) {
		var type = args["type"];		
		var type = type.toLowerCase();
		type = type.match(/(^.)(.+)/);
		type = type[1].toUpperCase() + type[2];
		args["type"] = type;
		var obj = new Phylo.Matrices.Datatype({'type':type});	
		args["type_object"] = obj;	
	}
	this.Listable(args);
	return this;	
}
//alert(TypeSafeData);
Phylo.Matrices['TypeSafeData'] = TypeSafeData;
Phylo.Util.CONSTANT.copyPrototype(Phylo.Matrices.TypeSafeData,Phylo.Listable);

Phylo.Matrices.TypeSafeData.prototype.set_type = function(arg) {
	var args;
	if ( arg instanceof Object ) {
		args = arg;
	}
	else {
		args = { 'type' : arg };
	}
	var obj = new Phylo.Matrices.Datatype(args);
	this.set_type_object(obj);
	if ( this._type() == Phylo.Util.CONSTANT._MATRIX_() ) {
	    var ents = this.get_entities();
		for ( var i = 0; i < ents.length; i++ ) {
			ents[i].set_type_object(obj);
		}
	}
	return this;	
};

Phylo.Matrices.TypeSafeData.prototype.set_missing = function (missing) {
	if ( this['get_matchchar'] && missing == this.get_matchchar() ) {
		throw new Phylo.Util.Exceptions.BadArgs(
			"Missing character '"+missing+"' already in use as match character"
		);
	}
	this.get_type_object().set_missing(missing);
	this.validate();
	return this;
};

Phylo.Matrices.TypeSafeData.prototype.set_gap = function (gap) {
	if ( this['get_matchchar'] && gap == this.get_matchchar() ) {
		throw new Phylo.Util.Exceptions.BadArgs(
			"Gap character '"+gap+"' already in use as match character"
		);
	}
	this.get_type_object().set_gap(gap);
	this.validate();
	return this;
};

Phylo.Matrices.TypeSafeData.prototype.set_lookup = function(lookup) {
	this.get_type_object().set_lookup(lookup);
	this.validate();
	return this;
};

Phylo.Matrices.TypeSafeData.prototype.set_type_object = function(type_object) {
	this.type_object = type_object;
	try {
		this.validate();
	}
	catch (e) {
		if ( this['get_char'] ) {
			this.clear();
			Phylo.Util.Logger.warn(
				"Data contents of "+this+" were invalidated by new type object."
			);
		}
	}
	return this;
};

Phylo.Matrices.TypeSafeData.prototype.get_type = function() {
	return this.get_type_object().get_type();
};

Phylo.Matrices.TypeSafeData.prototype.get_missing = function() {
	return this.get_type_object().get_missing();
};

Phylo.Matrices.TypeSafeData.prototype.get_gap = function() {
	return this.get_type_object().get_gap();
};

Phylo.Matrices.TypeSafeData.prototype.get_lookup = function() {
	return this.get_type_object().get_lookup();
};

Phylo.Matrices.TypeSafeData.prototype.get_type_object = function() {
	return this.type_object;
};

Phylo.Matrices.TypeSafeData.prototype.validate = function() {
	throw new Phylo.Util.Exceptions.NotImplemented('Not implemented!');
};

})()