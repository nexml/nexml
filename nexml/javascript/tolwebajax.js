        	var status_element;
        	var tree;        	
            var xmlHttp;        	
        	var logger   = Phylo.Util.Logger;
        	var base     = 'http://' + top.location.host + '/nexml/phylows/tolweb/Tree/ToLWeb:';
        	var constant = Phylo.Util.CONSTANT;   
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
                                annotate_tree(tree);
                                var newick = tree.to_newick({
                                	'nhxkeys' : [ 'img', 'G' ]
                                });
                                Phylo.Util.Logger.info(newick);
                                PhyloWidget.changeSetting('tree',newick);
                            }                            
                        }
                    }
                };           
                return xmlHttp;
            }  
            function annotate_tree(t) {
            	var image_dir = 'http&colon;//' + top.location.host + '/nexml/html/include/';
            	t.visit(
            		function(n) {
            			var dict = n.get_generic('dict');
            			var nhx = {};
            			if ( dict != null ) {
	            			if ( dict['EXTINCT'] != null && dict['EXTINCT'][1] != 0 ) {
	            				nhx['img'] = image_dir + 'cross.png';
	            			}
	            			if ( dict['ID'] != null ) {
	            				nhx['G'] = dict['ID'][1];
	            			}
	            			if ( dict['HASPAGE'] != null && dict['HASPAGE'][1] != 0 ) {
	            				nhx['img'] = image_dir + 'world_link.png';
	            			}
	            			n.set_generic(nhx);
            			}
            		}
            	);
            }               	
        	function fetch_tree () {
                var id = document.getElementById('tree_id').value;
                var ajax = initialize_ajax();
                Phylo.Util.Logger.debug(ajax);
                var url = base + id;
                ajax.open("GET",url,true);
                ajax.send(null);
        	}