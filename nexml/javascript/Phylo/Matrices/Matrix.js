(function(){
function Matrix (args) {
	if(args==null) args = {};
	args["tag"] = "characters";	
    this.TypeSafeData(args);
    this._type      = Phylo.Util.CONSTANT._MATRIX_;
    this._container = Phylo.Util.CONSTANT._PROJECT_;
    return this;
}
Phylo.Matrices.Matrix = Matrix;
Phylo.Util.CONSTANT.copyPrototypeMI(
	Phylo.Matrices.Matrix,[
		Phylo.Matrices.TypeSafeData,
		Phylo.Taxa.TaxaLinker
	]
);

Phylo.Matrices.Matrix.prototype.set_state_labels = function(statelabels) {
	// it's an array ref, but what about its contents?
	if ( statelabels instanceof Array ) {
		for ( var i in statelabels ) {
			if ( ! statelabels[i] instanceof Array ) {
				throw new Phylo.Util.Exceptions.BadArgs(
					"statelabels must be a two dimensional array ref"
				);
			}
		}
	}
	// it's defined but not an array ref
	else if ( statelabels != null && ! statelabels instanceof Array ) {
		throw new Phylo.Util.Exceptions.BadArgs(
			"statelabels must be a two dimensional array ref"
		);
	}
	// it's either a valid array ref, or nothing, i.e. a reset
	this.statelabels = statelabels || [];
	return this;
};

Phylo.Matrices.Matrix.prototype.set_charlabels = function(charlabels) {
	if ( charlabels != null && ! charlabels instanceof Array ) {
		throw new Phylo.Util.Exceptions.BadArgs(
			"charlabels must be an array"
		);
	}
	// it's either a valid array ref, or nothing, i.e. a reset
	this.charlabels = charlabels || [];
	return this;
};

Phylo.Matrices.Matrix.prototype.set_gapmode = function(gapmode) {
	this.gapmode = gapmode;
	return this;
};

Phylo.Matrices.Matrix.prototype.set_matchchar = function(match) {
	var missing = this.get_missing();
	var gap = this.get_gap();
	if ( match == missing ) {
		throw new Phylo.Util.Exceptions.BadArgs(
			"Match character '"+match+"' already in use as missing character"
		);		
	}
	else if ( match == gap ) {
		throw new Phylo.Util.Exceptions.BadArgs(
			"Match character '"+match+"' already in use as gap character"
		);			
	}
	else {
		this.matchchar = match;
	}
	return this;
};

Phylo.Matrices.Matrix.prototype.set_polymorphism = function(polymorphism) {
	this.polymorphism = polymorphism;
	return this;
};

Phylo.Matrices.Matrix.prototype.set_respectcase = function (respectcase) {
	this.respectcase = respectcase;
	return this;
};

Phylo.Matrices.Matrix.prototype.get_statelabels = function() {
	return this.statelabels || [];
};

Phylo.Matrices.Matrix.prototype.get_charlabels = function () {
	return this.charlabels || [];	
};

Phylo.Matrices.Matrix.prototype.get_gapmode = function () {
	return this.gapmode;
};

Phylo.Matrices.Matrix.prototype.get_matchchar = function () {
	return this.matchchar || '.';
};

Phylo.Matrices.Matrix.prototype.get_nchar = function() {
	var nchar = 0;
	var rows = this.get_entities();
	for ( var i in rows ) {
		var row = rows[i];
		var rowlength = row.get_entities().length + row.get_position() - 1;
		if (rowlength>nchar) nchar = rowlength;
	}
	return nchar;
};

Phylo.Matrices.Matrix.prototype.get_ntax = function() {
	return this.get_entities().length;
};

Phylo.Matrices.Matrix.prototype.get_polymorphism = function(){
	return this.polymorphism;
};

Phylo.Matrices.Matrix.prototype.get_respectcase = function () {
	return this.respectcase;
};

Phylo.Matrices.Matrix.prototype.check_taxa = function () {
	var taxa = this.get_taxa();
	if ( taxa ) {
		var taxa_names = {};
		taxa.get_entities().map(
			function ( taxon ) {
				taxa_names[taxon.get_name()] = taxon;
			}		
		);
		var rows = this.get_entities();
		ROW_CHECK: for ( var i in rows ) {
			var row = rows[i];
			var taxon = row.get_taxon();
			if ( taxon ) {
				if ( taxa_names[taxon.get_name()] ) {
					continue ROW_CHECK;
				}
			}
			var row_name = row.get_name();
			if ( taxa_names[row_name] ) {
				row.set_taxon(taxa_names[row_name]);
			}
			else {
				var taxon = new Phylo.Taxa.Taxon({'name':row_name});
				taxa_names[row_name] = taxon;
				taxa.insert(taxon);
				row.set_taxon(taxon);
			}
		}
	}
	else {
		var rows = this.get_entities();
		for ( var i in rows ) {
			rows[i].set_taxon();
		}
	}
	return this;
};

Phylo.Matrices.Matrix.prototype.validate = function () {
	var rows = this.get_entities();
	for ( var i in rows ) {
		rows[i].validate();
	}
};

Phylo.Matrices.Matrix.prototype.insert = function(obj) {
	if ( obj._container() != this._type() ) {
		throw new Phylo.Util.Exceptions.ObjectMismatch('object not a datum object!');
	}
	if ( !this.get_type_object().is_same( obj.get_type_object() ) ) {
		throw new Phylo.Util.Exceptions.ObjectMismatch('object is of wrong data type!');
	}
	var taxon1 = obj.get_taxon();
	this.visit(
		function(ents) {			
			if ( obj.get_id() == ents.get_id() ) {
				throw new Phylo.Util.Exceptions.ObjectMismatch('row already inserted');
			}	
			if (taxon1) {
				var taxon2 = ents.get_taxon();
				if ( taxon2 && taxon1.get_id() == taxon2.get_id() ) {
					ents.concat(obj);
					return this;
				}
			}
		}
	);
	Phylo.Listable.prototype.insert.apply(this,arguments);
	return this;	
};

function keys (obj) {
	var theKeys = [];
	if (obj==null) return theKeys;
	for ( var key in obj ) {
		theKeys.push(key);
	}
	return theKeys;
}

Phylo.Matrices.Matrix.prototype.to_xml = function(args) {
	if(args==null) args = {};
	var ids_for_states;
	var type = this.get_type();
	var verbosity = args["compact"] ? 'Seqs' : 'Cells';
	var xsi_type = 'nex:' + type + verbosity;
	this.set_attributes( { 'xsi:type' : xsi_type } );
	var xml = this.get_xml_tag();
	var normalized = _normalize_symbols(this);
	
	// skip <format/> block in compact mode
	if ( ! args["compact"] ) {
		
		// the format block
		xml += '<format>';
		var to = this.get_type_object();
		ids_for_states = to.get_ids_for_states(true);
		
		// write state definitions
		xml += to.to_xml(normalized);
		
		// write column definitions
		if ( keys(ids_for_states).length ) {
			xml += _write_char_labels(this,to.get_xml_id());
		}
		else {
			xml += _write_char_labels(this);
		}
		xml += '</format>';
	}
	
	// the matrix block
	xml += '<matrix>';
	var char_ids = [];
	for ( var i = 0; i <= this.get_nchar(); i++ ) {
		char_ids.push( 'c' + (i+1) );
	}
	
	// write rows
	var rows = this.get_entities();
	for ( var i in rows ) {
		var row = rows[i];
		var row_args = {
			'states' : ids_for_states,
			'chars'  : char_ids,	
			'symbols': normalized,			
		};
		for ( var key in args ) {
			row_args[key] = args[key];
		}
		xml += row.to_xml(row_args);
	}
	xml += '</matrix>';
	xml += '</' + this.get_tag() + '>';
	return xml;
}


function _normalize_symbols (selff) {
	if ( selff.get_type() == 'Standard' ) {
		var to = selff.get_type_object();
		var lookup = selff.get_lookup();
		var states = keys(lookup);
		var letters = states.grep(/[a-z]/i).sort();
		var numbers = states.grep(/^\d+$/).sort();
		if ( letters.length ) {
			var i = numbers.last();
			var theMap = {};
			letters.map(
				function(sym) {
					theMap[sym] = ++i;
				}
			);
			return theMap;
		}
		else {
			return {};
		}
	}
	else {
		return {};
	}
};

function _write_char_labels (selff,states_id) {
	var xml = '';
	var labels = selff.get_charlabels();
	for ( var i = 0; i < selff.get_nchar(); i++ ) {
		var char_id = 'c' + (i+1);
		var labell = labels[i-1];
		
		//have state definitions (categorical data)
		if ( states_id ) {
			if ( labell ) {
				xml += '<char id="' + char_id + '" label="' + labell + '" states="' + states_id + '"/>';
			}
			else {
				xml += '<char id="' + char_id + '" states="' + states_id + '"/>';
			}
		}
		//must be continuous characters (because no state definitions)
		else {
			if ( labell ) {
				xml += '<char id="' + char_id + '" label="' + labell + '"/>';
			}
			else {
				xml += '<char id="' + char_id + '"/>';
			}
		}
	}
	return xml;
};


})()