(function(){
Phylo.IO = {};

Phylo.Parsers = {
	'Nexml' : {}
};

function parse(args) {
	var format = args["format"];
	var standardized_format = format.toLowerCase();
	standardized_format = standardized_format.match(/(^.)(.+)/);
	standardized_format = standardized_format[1].toUpperCase() + standardized_format[2];
	if ( Phylo.Parsers[standardized_format] != null ) {
		return Phylo.Parsers[standardized_format].parse(args);
	}
	else {
		throw new Phylo.Util.Exceptions.BadFormat("Can't parse " + standardized_format);
	}

}
Phylo.IO.parse = parse;
})()