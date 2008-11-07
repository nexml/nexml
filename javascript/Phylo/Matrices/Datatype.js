(function(){
var fields = {
	'gap' : {
		'Dna'     : '-',
		'Rna'     : '-',
		'Protein' : '-'
	},
	'missing' : {
		'Continuous' : '?',
		'Dna'        : '?',
		'Protein'    : '?',
		'Rna'        : '?',
		'Standard'   : '?'
	},
	'lookup' : {
		'Dna' : {
		    'A' : [ 'A'                ],
		    'C' : [ 'C'                ],
		    'G' : [ 'G'                ],
		    'T' : [ 'T'                ],
		    'M' : [ 'A', 'C'           ],
		    'R' : [ 'A', 'G'           ],
		    'W' : [ 'A', 'T'           ],
		    'S' : [ 'C', 'G'           ],
		    'Y' : [ 'C', 'T'           ],
		    'K' : [ 'G', 'T'           ],
		    'V' : [ 'A', 'C', 'G'      ],
		    'H' : [ 'A', 'C', 'T'      ],
		    'D' : [ 'A', 'G', 'T'      ],
		    'B' : [ 'C', 'G', 'T'      ],
		    'X' : [ 'G', 'A', 'T', 'C' ],
		    'N' : [ 'G', 'A', 'T', 'C' ]		
		},
		'Protein' : {
		    'A' : [ 'A'      ],
		    'B' : [ 'D', 'N' ],
		    'C' : [ 'C'      ],
		    'D' : [ 'D'      ],
		    'E' : [ 'E'      ],
		    'F' : [ 'F'      ],
		    'G' : [ 'G'      ],
		    'H' : [ 'H'      ],
		    'I' : [ 'I'      ],
		    'K' : [ 'K'      ],
		    'L' : [ 'L'      ],
		    'M' : [ 'M'      ],
		    'N' : [ 'N'      ],
		    'P' : [ 'P'      ],
		    'Q' : [ 'Q'      ],
		    'R' : [ 'R'      ],
		    'S' : [ 'S'      ],
		    'T' : [ 'T'      ],
		    'U' : [ 'U'      ],
		    'V' : [ 'V'      ],
		    'W' : [ 'W'      ],
		    'X' : [ 'X'      ],
		    'Y' : [ 'Y'      ],
		    'Z' : [ 'E', 'Q' ],
		    '*' : [ '*'      ]		
		},
		'Restriction' : {
		    '0' : [ '0' ],
		    '1' : [ '1' ]		
		},
		'Rna' : {
		    'A' : [ 'A'                ],
		    'C' : [ 'C'                ],
		    'G' : [ 'G'                ],
		    'U' : [ 'U'                ],
		    'M' : [ 'A', 'C'           ],
		    'R' : [ 'A', 'G'           ],
		    'W' : [ 'A', 'U'           ],
		    'S' : [ 'C', 'G'           ],
		    'Y' : [ 'C', 'U'           ],
		    'K' : [ 'G', 'U'           ],
		    'V' : [ 'A', 'C', 'G'      ],
		    'H' : [ 'A', 'C', 'U'      ],
		    'D' : [ 'A', 'G', 'U'      ],
		    'B' : [ 'C', 'G', 'U'      ],
		    'X' : [ 'G', 'A', 'U', 'C' ],
		    'N' : [ 'G', 'A', 'U', 'C' ]			
		},
		'Standard' : {
		    '0' : [ '0' ],
		    '1' : [ '1' ],
		    '2' : [ '2' ],
		    '3' : [ '3' ],
		    '4' : [ '4' ],
		    '5' : [ '5' ],
		    '6' : [ '6' ],
		    '7' : [ '7' ],
		    '8' : [ '8' ],
		    '9' : [ '9' ]			
		}
	}
};
	
function Datatype (args) {
	if (args==null) args = {};
	args["tag"] = "states";
	if ( args["type"] == null ) {
		args["type"] = 'Standard';
	}
	else {
		var type = args["type"];		
		var type = type.toLowerCase();
		type = type.match(/(^.)(.+)/);
		type = type[1].toUpperCase() + type[2];
		args["type"] = type;		
	}
    this.XMLWritable(args);
    var props = [ 'lookup', 'missing', 'gap' ];
    for ( var i = 0; i < props.length; i++ ) {
    	var field = props[i];
    	if ( ! args[field] ) {
    		this[field] = fields[field][args["type"]];    		
    	}
    }
    return this;
}
Phylo.Matrices.Datatype = Datatype;
Phylo.Util.CONSTANT.copyPrototype(Phylo.Matrices.Datatype,Phylo.Util.XMLWritable);

Phylo.Matrices.Datatype.prototype.set_lookup = function (lookup) {
	if ( fields.lookup[this.type] != null ) {
		this.lookup = lookup;
	}
	return this;
};

Phylo.Matrices.Datatype.prototype.set_missing = function (missing) {
	if ( this.missing != null ) {
		if ( this.gap != missing ) {
			this.missing = missing;
		}
		else {
			throw new Phylo.Util.Exceptions.BadArgs(
				"Missing character '"+missing+"' already in use as gap character"
			);
		}
	}
	else {
		throw new Phylo.Util.Exceptions.BadArgs(
			'Data type ' + this.type + " doesn't have missing character symbols"
		);		
	}
};

Phylo.Matrices.Datatype.prototype.set_gap = function (gap) {
	if ( this.gap != null ) {
		if ( this.missing != gap ) {
			this.gap = gap;
		}
		else {
			throw new Phylo.Util.Exceptions.BadArgs(
				"Gap character '"+gap+"' already in use as missing character"
			);
		}
	}
	else {
		throw new Phylo.Util.Exceptions.BadArgs(
			'Data type ' + this.type + " doesn't have gap character symbols"
		);		
	}
};

Phylo.Matrices.Datatype.prototype.get_type = function () {
	return this.type;
};

Phylo.Matrices.Datatype.prototype.get_ids_for_states = function (with_prefix) {
	if ( this.lookup ) {
		var i = 1;
		var ids_for_states = {};
		var states         = [];
		var tmp_cats       = [];
		var sort_on_second = function(a,b) {
			return a[1] - b[1];
		}
		var sortable = [];
		for ( var key in this.lookup ) {
			sortable.push([key, this.lookup[key].length])
		}
		var tmp = sortable.sort(sort_on_second);
		for ( var j = 0; j < tmp.length; j++ ) {
		    var state = tmp[j];
			var count = state[1];
			var sym = state[0];
			if (!tmp_cats[count]) {
				tmp_cats[count] = [];
			}
			tmp_cats[count].push(sym);
		}
		for ( var j = 0; j < tmp_cats.length; j++ ) {
		    var cat = tmp_cats[j];
			if ( cat ) {
				var sorted = cat.sort();
				sorted.map(function(n){states.push(n)});
			}
		}
		for ( var j = 0; j < states.length; j++ ) {
		    var state = states[j];
			var id = i++;
			ids_for_states[state] = with_prefix ? 's' + id : id;
		}
		return ids_for_states;
	}
	else {
		return {};
	}
};

Phylo.Matrices.Datatype.prototype.get_symbol_for_states = function (syms) {
    var lookup = this.get_lookup();
    if ( lookup ) {
        var lookup_syms = lookup.keys();
        SYM: for ( var k in lookup_syms ) {
            var sym = lookup_syms[k];
            var states = lookup[sym];
            if ( syms.length == states.length ) {
                var seen_all = 0;
                for ( var i in syms ) {
                    var seen = 0;
                    for ( var j in states ) {
                        if ( syms[i] == states[j] ) {
                            seen++;
                            seen_all++;
                        }
                    }
                    if (!seen) continue SYM;
                }
                // found existing symbol
                if (seen_all == syms.length) return sym;
            }
        }
        // create new symbol
        var sym;			
        if ( this.get_type() != 'Standard' ) {
            sym = 0;
            while ( lookup[sym] != null ) {
                sym++;
            }
        }
        else {
            var char_range = range( 'A', 'Z' );
            LETTER: for ( var i in char_range ) {
                var letter = char_range[i];
                if ( lookup[letter] == null ) {
                    sym = letter;
                    break LETTER;
                }
            }
        }
        
        lookup[sym] = syms;
        this.set_lookup(lookup);
        return sym;
    }
    else {
        Phylo.Util.Logger.info("No lookup table!");
        return null;
    }
};

Phylo.Matrices.Datatype.prototype.get_lookup = function () {
    if ( this.loookup ) {
        return this.lookup;
    }
    else {
    	this.set_lookup(fields.lookup[this.type]);
        return fields.lookup[this.type];
    }
};

Phylo.Matrices.Datatype.prototype.get_missing = function () {
	return this.missing ? this.missing : '?';
};

Phylo.Matrices.Datatype.prototype.get_gap = function () {
	return this.gap ? this.gap : '-';
};

Phylo.Matrices.Datatype.prototype.is_valid = function (arg) {//(args) {
	var data = [];
	//for ( var i in args ) {
	    //var arg = args[i];
		//if ( arg instanceof String ) {
			if ( arg['get_char'] ) {
				arg.get_char().map(function(n){data.push(n)});
			}
			else if ( arg instanceof Array ){
				data = arg;	
			}
		//}
		//else {
			else if ( arg.length > 1 ) {
				data = this.split(arg);
			}
			else {
				data.push(arg);
			}
		//}
	//}
	if (data.length == 0) return true;
	var lookup = this.get_lookup();
	var missing = this.get_missing();
	var gap = this.get_gap();
	CHAR_CHECK: for ( var i in data ) {
	    var character = data[i];
		if ( character == null ) continue CHAR_CHECK;
		var uc = character.toUpperCase();
		if ( lookup[uc] || ( missing && uc == missing ) || ( gap && uc ==gap ) ) {
			continue CHAR_CHECK;
		}
		else {
			return false;
		}
	}
	return true;
};	

function keys (obj) {
	var theKeys = [];
	if (obj==null) return theKeys;
	for ( var key in obj ) {
		theKeys.push(key);
	}
	return theKeys;
}

Phylo.Matrices.Datatype.prototype.is_same = function (model) {
	if ( this.get_id() == model.get_id() ) return true;
	if ( this.get_type() != model.get_type() ) return false;
	// check strings
	if ( this.get_missing() != model.get_missing() || this.get_gap() != model.get_gap() ) {
		return false;
	}
	var s_lookup = this.get_lookup();
	var m_lookup = model.get_lookup();
	// one has lookup, other hasn't        
	if ( s_lookup && ! m_lookup ) {
		return false;
	}
	// both don't have lookup -> are continuous
    if ( ! s_lookup && ! m_lookup ) {
    	return true
    }
    // get keys        
    var s_keys = keys(s_lookup);
    var m_keys = keys(m_lookup);      
    // different number of keys
    if ( s_keys.length != m_keys.length ) {
    	return false;
    }
    //compare keys
	for ( var i in s_keys ) {
	    var key = s_keys[i];
		if ( m_lookup[key] == null ) {
			return false;
		}
		else {
			// compare values
			var s_vals = {};
			var m_vals = {};
			var s_vals_a = s_lookup[key];
			var m_vals_a = m_lookup[key];
			// different number of vals
			if ( s_vals_a.length != m_vals_a.length ) {
				return false;
			}
			// make hashes to compare on vals
			s_vals_a.map(function(n){s_vals[n]=1});
			m_vals_a.map(function(n){m_vals[n]=1});
			var s_vals_keys = keys(s_vals);
			for ( var j in s_vals_keys ) {
			    var val = s_vals_keys[j];
				if ( m_vals[val] == null ) {
					return false;
				}
			}
		}		
	}    
	return true;       
};

Phylo.Matrices.Datatype.prototype.split = function (str) {
	if ( this.type == 'Continuous' ) {
		return str.split(' ');
	}
	else {
		if ( str != undefined ) {
			return str.split('');
		}
		else {			
			Phylo.Util.Logger.warn(str);
		}
	}
};

Phylo.Matrices.Datatype.prototype.join = function (array) {
	if ( this.type == 'Continuous' ) {
		return array.join(' ');
	}
	else {
		return array.join('');
	}
};

function range ( low, high, step ) {
    // http://kevin.vanzonneveld.net
    // +   original by: _argos
    // *     example 1: range ( 0, 12 );
    // *     returns 1: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
    // *     example 2: range( 0, 100, 10 );
    // *     returns 2: [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    // *     example 3: range( 'a', 'i' );
    // *     returns 3: ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i']
    // *     example 4: range( 'c', 'a' );
    // *     returns 4: ['c', 'b', 'a']
 
    var matrix = [];
    var inival, endval, plus;
    var walker = step || 1;
    var chars  = false;
 
    if ( !isNaN ( low ) && !isNaN ( high ) ) {
        inival = low;
        endval = high;
    } else if ( isNaN ( low ) && isNaN ( high ) ) {
        chars = true;
        inival = low.charCodeAt ( 0 );
        endval = high.charCodeAt ( 0 );
    } else {
        inival = ( isNaN ( low ) ? 0 : low );
        endval = ( isNaN ( high ) ? 0 : high );
    }
 
    plus = ( ( inival > endval ) ? false : true );
    if ( plus ) {
        while ( inival <= endval ) {
            matrix.push ( ( ( chars ) ? String.fromCharCode ( inival ) : inival ) );
            inival += walker;
        }
    } else {
        while ( inival >= endval ) {
            matrix.push ( ( ( chars ) ? String.fromCharCode ( inival ) : inival ) );
            inival -= walker;
        }
    }
 
    return matrix;
}

Phylo.Matrices.Datatype.prototype.to_xml = function(args) {
	var xml = '';
	var normalized = {};
	if (args != null) normalized = args;
	var lookup = this.get_lookup();
	if (lookup) {
		xml += "\n" + this.get_xml_tag();
		var id_for_state = this.get_ids_for_states(false);
		var states = keys(id_for_state).sort(
			function(a,b) {
				return id_for_state[a] - id_for_state[b];
			}
		);
		for ( var i in states ) {
		    var state = states[i];
			var state_id = id_for_state[state];
			id_for_state[state_id] = 's' + state_id;
		}
		for ( var i in states ) {
		    var state = states[i];
			var state_id = id_for_state[state];
			var mapping = lookup[state];
			var symbol = normalized[state] != null ? normalized[state] : state;
			// has ambiguity mappings
			if ( mapping.length > 1 ) {
				xml += "\n" + '<state id="s' + state_id + '" symbol="' + symbol + '">';
				for ( var j in mapping ) {
				    var map = mapping[j];
					xml += "\n" + '<mapping state="s' + id_for_state[map] + '" mstaxa="uncertainty"/>';
				}
				xml += "\n" + '</state>';
			}
			// no ambiguity
			else {
				xml += "\n" + '<state id="s' + state_id + '" symbol="' + symbol + '"/>';
			}
		}
		xml += "\n" + '</states>';
	}
	return xml;
};

})()