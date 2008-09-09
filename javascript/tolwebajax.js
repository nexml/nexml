        	var status_element;
        	var tree;        	
            var xmlHttp;        	
        	var logger   = Phylo.Util.Logger;
        	var base     = 'http://' + top.location.host + '/nexml/phylows/tolweb/';
        	var constant = Phylo.Util.CONSTANT;
        	logger.VERBOSE(3);
            logger.set_listener( 
                function( msg ) { 
                    if ( status_element == null ) {
                        status_element = document.getElementById('status');
                    }
                    status_element.innerHTML += msg + "\n";
                }        	
            );   
            function initialize_ajax() {
                try {
                    // Firefox, Opera 8.0+, Safari
                    xmlHttp = new XMLHttpRequest();
                } catch (e) {
                    // Internet Explorer
                    try {
                        xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
                    } catch (e) {
                        try {
                            xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
                        } catch (e) {
                            alert("Your browser does not support AJAX!");
                            return false;
                        }
                    }
                }
                xmlHttp.onreadystatechange = function() {
                    if ( xmlHttp.readyState == 4 ) {
                        var nexml = xmlHttp.responseText;
                        var blocks = Phylo.IO.parse({
                            'format':'nexml',
                            'string':nexml
                        });
                        for ( var i = 0; i < blocks.length; i++ ) {
                            if ( blocks[i]._type() == constant._FOREST_() ) {                                
                                tree = blocks[i].first();
                                Phylo.Util.Logger.info(tree.to_newick());
                            }                            
                        }
                    }
                };           
                return xmlHttp;
            }                 	
        	function fetch_tree () {
                var id = document.getElementById('tree_id').value;
                var ajax = initialize_ajax();
                Phylo.Util.Logger.debug(ajax);
                var url = base + id;
                ajax.open("GET",url,true);
                ajax.send(null);
        	}