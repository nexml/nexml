Phylo.Project = function (args) {
	if (args==null) args = {};
	args["tag"] = "nex:nexml";
	args["attributes"] = {
		'version'   : '1.0',
		'generator' : 'Phylo.Project $Rev: $',
		'xmlns:xsi' : 'http://www.w3.org/2001/XMLSchema-instance',
		'xmlns:xml' : 'http://www.w3.org/XML/1998/namespace',
		'xmlns:nex' : 'http://www.nexml.org/1.0',			
		'xsi:schemaLocation' : 'http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd'	
	};
    this.Listable(args);
    this._type      = Phylo.Util.CONSTANT._PROJECT_;
    this._container = Phylo.Util.CONSTANT._NONE_;
    return this;
}
Phylo.Util.CONSTANT.copyPrototype(Phylo.Project,Phylo.Listable);

Phylo.Project.prototype.get_taxa = function () {
	var ents = this.get_entities();
	var result = new Array();
	for ( var i = 0; i < ents.length; i++ ) {
		if ( ents[i]._type == Phylo.Util.CONSTANT._TAXA_ ) {
			result.push(ents[i]);
		}
	}
	return result;
};

Phylo.Project.prototype.get_matrices = function () {
	var ents = this.get_entities();
	var result = new Array();
	for ( var i = 0; i < ents.length; i++ ) {
		if ( ents[i]._type == Phylo.Util.CONSTANT._MATRIX_ ) {
			result.push(ents[i]);
		}
	}
	return result;
};

Phylo.Project.prototype.get_forests = function () {
	var ents = this.get_entities();
	var result = new Array();
	for ( var i = 0; i < ents.length; i++ ) {
		if ( ents[i]._type == Phylo.Util.CONSTANT._FOREST_ ) {
			result.push(ents[i]);
		}
	}
	return result;
};

Phylo.Project.prototype.to_xml = function (args) {
	var xml = this.get_root_open_tag();
	var blocks = new Array();
	var taxa     = this.get_taxa();
	var matrices = this.get_matrices();
	var forests  = this.get_forests();
	blocks = blocks.concat(taxa);
	blocks = blocks.concat(matrices);
	blocks = blocks.concat(forests);
	for ( var i = 0; i < blocks.length; i++ ) {
		xml += blocks[i].to_xml(args);
	}
	xml += this.get_root_close_tag();
	return xml;
};