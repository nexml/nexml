function XMLWritable (args) {
    this.Phylo(args);
    this.attributes = {};
    return this;
}
copyPrototype(XMLWritable,Phylo);

var logger = new Logger();

XMLWritable.prototype.set_tag = function(tag) {
    this.tag = tag;
    return this;
};

XMLWritable.prototype.set_attributes = function(attrs) {
	if (attrs==null) attrs = {};
	for ( var key in attrs ) {
		this.attributes[key] = attrs[key];
	}
};

XMLWritable.prototype.set_xml_id = function(xml_id) {
	this.xml_id = xml_id;	
	return this;
};

XMLWritable.prototype.get_tag = function() {
	return this.tag;
};

XMLWritable.prototype.get_xml_tag = function(close_me) {
	if (close_me==null) close_me = false;
	var tag_string = '<' + this.get_tag();
	var attrs = this.get_attributes();
	for ( var key in attrs ) {
		tag_string += " " + key + '="' + attrs[key] + '"';
	}
	var dict = this.get_generic("dict");
	if ( dict != null ) {
		tag_string += '><dict>';
		for ( var dictkey in dict ) {
			tag_string += '<key>' + dictkey + '</key>';
			var val = dict[dictkey];
			var valtag = val.shift();
			tag_string += '<' + valtag + '>' + val.join(' ') + '</' + valtag + '>';
		}
		tag_string += '</dict>';
		if ( close_me ) tag_string += '</' + this.tag + '>';
	}
	else {
		tag_string += close_me ? '/>' : '>';
	}
	return tag_string;
}

XMLWritable.prototype.get_root_open_tag = function () {
	var class_name = 'XMLWritable';
	var version = this.VERSION();
	var root_open_tag = '<nex:nexml version="1.0"';
	root_open_tag += ' generator="' + class_name + ' v.' + version + '"';
	root_open_tag += ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"';
	root_open_tag += ' xmlns:xml="http://www.w3.org/XML/1998/namespace"';
	root_open_tag += ' xsi:schemaLocation="http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd"'; 
	root_open_tag += ' xmlns:nex="http://www.nexml.org/1.0">';  
	return root_open_tag;
}

XMLWritable.prototype.get_root_close_tag = function () {
	return '</nex:nexml>';
}

XMLWritable.prototype.get_attributes = function () {
	var attrs = this.attributes;
	if ( attrs["label"] == null && this.get_name() != null ) {
		attrs["label"] = this.get_name();
	}
	if ( attrs["id"] == null ) {
		attrs["id"] = this.get_xml_id();
	}
	if ( this.get_taxa != null ) { // duck-typing!
		var taxa = this.get_taxa();
		if ( taxa != null ) {
			attrs["otus"] = taxa.get_xml_id();
		}
		else {
			throw new ObjectMismatch( this + "can link to a taxa element, but doesn't");
		}
	}
	if ( this.get_taxon != null ) { // duck-typing!
		var taxon = this.get_taxon();
		if ( taxon != null ) {
			attrs["otu"] = taxon.get_xml_id();
		}
		else {
			logger.info("No linked taxon found");
		}
	}
	return attrs;
}

XMLWritable.prototype.get_xml_id = function () {
	if ( this.xml_id != null ) {
		return this.xml_id;
	}
	else {
		return this.get_tag() + this.get_id();
	}
}

XMLWritable.prototype.to_xml = function () {
	var xml = '';
	if ( this.get_entities != null ) { // duck-typing!
		var ents = this.get_entities();
		for ( var i = 0; i < ents.length; i++ ) {
			xml += ents[i].to_xml();
		}
	}
	if ( xml != '' ) {
		xml = this.get_xml_tag(false) + xml + '</' + this.get_tag() + '>';
	}
	else {
		xml = this.get_xml_tag(true);
	}
	return xml;
}

