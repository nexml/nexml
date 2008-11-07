if ( Phylo == null ) var Phylo = {};
Phylo.Initialize = function () {

	var files = [ 
		"Phylo/Util/CONSTANT.js",   
		"Phylo/Util/Exceptions.js",       
		"Phylo/Util/Logger.js",   
		"Phylo/Mediators/TaxaMediator.js",                
		"Phylo.js",
		"Phylo/Util/XMLWritable.js", 
		"Phylo/Listable.js",
		"Phylo/Taxa.js",  
		"Phylo/Taxa/TaxaLinker.js",                        
		"Phylo/Taxa/Taxon.js", 
		"Phylo/Taxa/TaxonLinker.js",                       
		"Phylo/Forest.js",
		"Phylo/Forest/Tree.js",        
		"Phylo/Forest/Node.js",        
		"Phylo/Forest/DrawTree.js",        
		"Phylo/Forest/DrawNode.js", 
		"Phylo/Matrices.js",
		"Phylo/Matrices/Datatype.js",
		"Phylo/Matrices/TypeSafeData.js",
		"Phylo/Matrices/Datum.js", 
		"Phylo/Matrices/Matrix.js", 
		"Phylo/Project.js",
		"Phylo/IO.js",
		"Phylo/Parsers/Nexml.js",	
	];
	
	var included = {};
	var include_once = function (file) {
		var header = document.getElementsByTagName('head')[0];
		var js;	
		if ( ! included[file] ) {
		    js = document.createElement('script');
		    js.setAttribute('type', 'text/javascript');
		    js.setAttribute('src', file);
		    header.appendChild(js);	    
		    included[file] = true;
		}	
		else {
			var scripts = document.getElementsByTagName('script');
			for ( var i = 0; i < scripts.length; i++ ) { 
				var src = scripts[i].getAttribute('src');
				if ( src == file ) {
					js = scripts[i];
					break;
				}
			}
			
		}
		return js;
	};

	for ( var i = 0; i < files.length; i++ ) {
		include_once(files[i]);
	}
}