(function(){

var TYPE_CONSTANT = Phylo.Util.CONSTANT._DATUM_();
var CONTAINER_CONSTANT = Phylo.Util.CONSTANT._MATRIX_();

function Datum (args) {
	if(args==null) args = {};
	args["tag"] = "row";
	this.TypeSafeData(args);
	return this;
}	
Phylo.Matrices.Datum = Datum;
copyPrototypeMI(
	Phylo.Matrices.Datum,[
		Phylo.Matrices.TypeSafeData,
		Phylo.Taxa.TaxonLinker
	]
);

Phylo.Matrices.Datum.prototype._type = function() {
	return TYPE_CONSTANT;
};

Phylo.Matrices.Datum.prototype._container = function() {
	return CONTAINER_CONSTANT;
};

Phylo.Matrices.Datum.prototype.set_weight = function(weight) {
	if(weight==null) weight = 1;
	this.weight = weight;
	return this;
};

Phylo.Matrices.Datum.prototype.set_char = function(args) {
	var data = [];
	for ( var i = 0; i < args.length; i++ ) {
	    var arg = args[i];
		if ( arg instanceof Array ) {
			arg.map(function(n){data.push(n)});
		}
		else {
			var splitchars = this.get_type_object().split(arg);
			for each ( splitchar in splitchars ) {
				data.push(splitchar);
			}
		}
	}
	var missing = this.get_missing();
	var position = this.get_position();
	for ( var i = 1; i < position; i++ ) {
		data.unshift(missing);
	}
	var oldchar = this.get_entities();
	try {
		this.clear();
		this.insert(data);
	} catch (e) {
		this.clear();
		try {
			this.insert(oldchar);
		} catch (e) {
		}
		throw new Phylo.Util.Exceptions.InvalidData(
			'Invalid data for row ' 
				+ this.get_internal_name() 
				+ '(type: ' 
				+ this.get_type()
				+ ': ' 
				+ data.join(' ')
				+ ')'
		);
	}
	this.set_annotations();
	return this;
};

Phylo.Matrices.Datum.prototype.set_position = function(pos) {
	if(pos==null) pos = 1;
	this.position = parseInt(pos);
	return this;
};

Phylo.Matrices.Datum.prototype.set_annotation = function(opt) {
	if (opt != null) {
		if ( ! opt['char'] ) {
			throw new Phylo.Util.Exceptions.BadArgs("No character to annotate specified!");
		}
		var i = opt['char'];
		var pos = this.get_position();
		var len = this.get_length();
		if ( i > (pos+len) || i < pos ) {
			throw new Phylo.Util.Exceptions.OutOfBounds(
				"Specified char ("+i+") does not exist!"
			);
		}
		if(this.annotations==null) this.annotations = [];
		if ( opt['annotation'] ) {
			var note = opt['annotation'];
			if (!this.annotations[i]) this.annotations[i] = {};
			for ( var k in note ) {
				this.annotations[i][k] = v;
			} 
		}
		else {
			this.annotations[i] = null;
		}
	}
	else {
		throw new Phylo.Util.Exceptions.BadArgs("No character to annotate specified!");
	}
	return this;
};

Phylo.Matrices.Datum.prototype.set_annotations = function(anno){
	if (anno) {
		var max_index = this.get_length() - 1;
		for ( var i in anno.length ) {
			if ( i > max_index ) {
				throw new Phylo.Util.Exceptions.OutOfBounds(
					"Specified char ("+i+") does not exist!"
				);				
			}
			else {
				if ( anno[i] ) {
					if(!this.annotations) this.annotations=[];
					if(!this.annotations[i]) this.annotations[i]={};
					for ( var k in anno[i] ) {
						this.annotations[i][k] = anno[k];
					}
				}
			}
		}
	}
	else {
		this.annotations = []
	}
	return this;
};

Phylo.Matrices.Datum.prototype.get_weight = function () {
	return this.weight ? this.weight : 1;
};

Phylo.Matrices.Datum.prototype.get_char = function () {
	return this.get_entities();
};

Phylo.Matrices.Datum.prototype.get_position = function () {
	return this.position ? this.position : 1;
};

Phylo.Matrices.Datum.prototype.get_annotation = function (opt) {
	if (opt){
		if ( ! opt["char"] ) {
			throw new Phylo.Util.Exceptions.BadArgs("No character to return annotation for specified!");
		}
		var i = opt["char"];
		var pos = this.get_position();
		var len = this.get_length();
		if ( i < pos || i > (pos+len)) {
			throw new Phylo.Util.Exceptions.OutOfBounds(
				"Specified char ("+i+") does not exist!"
			);
		}
		if ( opt["key"] ) {
			if ( this.annotations && this.annotations[i] ) {
				return this.annotations[i][opt["key"]];
			}
			else {
				return this.annotations[i];
			}
		}
	}
	else {
		return this.annotations;
	}
 };

Phylo.Matrices.Datum.prototype.get_annotations = function () {
	return this.annotations ? this.annotations : [];
};

Phylo.Matrices.Datum.prototype.get_length = function () {
	if ( this['_get_container'] ) {
		var matrix = this._get_container();
		if ( matrix ) {
			return matrix.get_nchar();
		}
		else {
			return this.entities.length + this.get_position() - 1;
		}
	}
	else {
		return this.entities.length + this.get_position() - 1;
	}
};

Phylo.Matrices.Datum.prototype.get_by_index = function(index) {
    var offset = this.get_position() - 1;
    if (offset > index) return this.get_type_object().get_missing();
    var val = this.entities[index-offset];
	return val != null ? val : this.get_type_object().get_missing();
};

Phylo.Matrices.Datum.prototype.can_contain = function(data) {
	return this.get_type_object().is_valid(data);
};


Phylo.Matrices.Datum.prototype.calc_state_counts = function (args) {
	var counts = {};
	if (args) {
		var tfocus = {};
		args.map(function(n){tfocus[n]=1});
		var chars = this.get_char();
		for each ( var c in chars ) {
			if ( tfocus[c] != null ) {
				if ( ! counts[c] ) {
					counts[c] = 1;
				}
				else {
					counts[c]++;
				}
			}
		}
	}
	else {
		var chars = this.get_char();
		for each ( var c in chars ) {
			if ( ! counts[c] ) {
				counts[c] = 1;
			}
			else {
				counts[c]++
			}
		}
	}
	return counts;		
};

Phylo.Matrices.Datum.prototype.reverse = function() {
	var chars = this.get_chars();
	this.clear();
	this.insert(chars.reverse());
};

Phylo.Matrices.Datum.prototype.concat = function(data) {
	var newchars = [];
	var self_chars = this.get_char();
	var self_i = this.get_position() - 1;
	var self_j = this.get_length() - 1 + self_i;
	for ( var i = self_i; i <= self_j; i++ ) {
		newchars[i] = self_chars[i];
	}
    for each ( var datum in data ) {
    	var chars = datum.get_char();
    	var i = datum.get_position() - 1;
    	var j = datum.get_length() - 1 + i;
    	for ( var k = i; k <= j; k++ ) {
    		newchars[k] = chars[k];
    	}
    }
	var missing = this.get_missing();
	for ( var i = 0; i < newchars.length; i++ ) {
		if (newchars[i] == null) newchars[i] = missing ;
	}
    this.set_char(newchars);
};

Phylo.Matrices.Datum.prototype.validate = function () {
	if ( !this.get_type_object().is_valid(this) ) {
		throw new Phylo.Util.Exceptions.InvalidData('Invalid data!');
	}
};

Phylo.Matrices.Datum.prototype.to_xml = function (args) {
	if(args==null) args = {};
	var char_ids  = args["chars"];
	var state_ids = args["states"];
	var taxon = this.get_taxon();
	if ( taxon ) {
		this.set_attributes({ 'otu' : taxon.get_xml_id() });
	}
	var chars = this.get_char();
	var missing = this.get_missing();
	var gap = this.get_gap();
	var xml = this.get_xml_tag();
	if ( ! args['compact'] ) {
		for ( var i = 0; i < chars.length; i++ ) {
			if ( missing != chars[i] && gap != chars[i] ) {
				var c, s;
				if ( char_ids && char_ids[i] ) {
					c = char_ids[i];
				}
				else {
					c = i;
				}
				if ( state_ids && state_ids[chars[i].toUpperCase()] ) {
					s = state_ids[chars[i].toUpperCase()]
				}
				else {
					s = chars[i].toUpperCase();
				}
				xml += '<cell char="'+c+'" state="'+s+'" />';
			}
		}
	}
	else {
		var tmp = [];
		chars.map(function(n){tmp.push(n.toUpperCase())});
		var seq = this.get_type_object().join(tmp);
		xml += '<seq>'+seq+'</seq>';
	}
	xml += '</' + this.get_tag() + '>';
	return xml;
};

})()