var NeXML = {};
(function() {
    var prefix;
    if ( typeof NeXMLAttributePrefix === 'undefined' ) {
	prefix = '@';
    }
    else {
        prefix = NeXMLAttributePrefix;
    }
    /*----------------------------------------------------------------*/    
    /* MIXINS */
    var Base = {
        getId : function () {
            return this[prefix + 'id'];
        },
        getAbout : function () {
            return this[prefix + 'about'];
        },
        getMeta : function () {
            return this.meta;
        }
    };
    var Labeled = {
        getLabel : function () {
            return this[prefix + 'label'];
        }   
    };
    var OTUsLinker = {
        getOTUsId : function () {
            return this[prefix + 'otus'];
        }
    };
    var OTULinker = {
        getOTUId : function () {
            return this[prefix + 'otu'];
        }
    };
    var Listable = {
        getThingById : function (property, id) {
            for (var i = 0; i < this[property].length; i++) {
                if (this[property][i].getId() === id) {
                    return this[property][i];
                }
            }
            return null;
        },
        getThingsByAttributeValue : function (property, attribute, value) {
            var results = [];
            for (var i = 0; i < this[property].length; i++) {
                if (this[property][i][attribute] === value) {
                    results.push(this[property][i]);
                }
            }
            return results;
        }    
    };
    var Polymorphic = {
        getXsiType : function () {
            return this[prefix + 'xsi:type'];
        }
    };
    /*----------------------------------------------------------------*/ 
    /* UTILITY FUNCTIONS FOR CONSTRUCTORS */
    function normalizeList(obj, prop, Constructor) {
        if (obj[prop] instanceof Array) {
            for (var i = 0; i < obj[prop].length; i++) {
                obj[prop][i] = new Constructor(obj[prop][i]);
            }
        }
        else if (obj[prop] instanceof Object) {
            obj[prop] = [new Constructor(obj[prop])];
        }
        else {
            obj[prop] = [];
        }      
    }    
    function copyFields(child, parents) {
        for (var i = 0; i < parents.length; i++) {
            for (var key in parents[i]) {
                child[key] = parents[i][key];
                if (key === 'meta') {
                    normalizeList(child, 'meta', Annotation);
                }
            }
        }
    }    
    /*----------------------------------------------------------------*/     
    /* METADATA-RELATED CLASS */   
    function Annotation(nexml) {
        copyFields(this, [nexml, Polymorphic]);
    }
    Annotation.prototype = {
        getProperty : function () {
            return this[prefix + 'property'];
        },
        getContent : function () {
            return this[prefix + 'content'];
        },
        getDatatype : function () {
            return this[prefix + 'datatype'];
        },
        getRel : function () {
            return this[prefix + 'rel'];
        }
    };     
    /*----------------------------------------------------------------*/ 
    /* OTU-RELATED CLASSES */
    function OTU(nexml) {
        copyFields(this, [nexml, Base, Labeled]);
    }
    
    function OTUs(nexml) {
        copyFields(this, [nexml, Base, Labeled, Listable]);
        normalizeList(this, 'otu', OTU);
    }
    OTUs.prototype = {
        getOTUList : function () {
            return this.otu;
        }
    };
    
    /*----------------------------------------------------------------*/
    /* TREE-RELATED CLASSES */
    function Node(nexml) {
        copyFields(this, [nexml, Base, Labeled, OTULinker]);
    }
    Node.prototype = {
        isRoot : function () {
            return this[prefix + 'root'] === 'true' ? true : false;
        }
    };
    
    function RootEdge(nexml) {
        copyFields(this, [nexml, Base, Labeled]);
    }
    RootEdge.prototype = { 
        getTargetId : function () {
            return this[prefix + 'target'];
        },
        getLength : function () {
            return this[prefix + 'length'];
        }
    };
    
    function Edge(nexml) {
        copyFields(this, [nexml, Base, Labeled, RootEdge.prototype]);
    }    
    Edge.prototype = {
        getSourceId : function () {
            return this[prefix + 'source'];
        }
    };
    
    function Tree(nexml) {
        copyFields(this, [nexml, Base, Labeled, Listable, Polymorphic]);
        normalizeList(this, 'node', Node);
        normalizeList(this, 'edge', Edge);
    }
    Tree.prototype = {
        getNodeList : function () {
            return this.node;
        },
        getEdgeList : function () {
            return this.edge;
        },
        getChildNodes : function (node) {
            var nodeId = node.getId();
            var result = [];
            var outEdges = this.getThingsByAttributeValue('edge', prefix + 'source', nodeId);
            if ( outEdges.length > 0 ) {
                for (var i = 0; i < outEdges.length; i++) {
                    var childId = outEdges[i].getTargetId();
                    result.push(this.getThingById('node', childId));
                }
            }
            return result;
        },
        getParentNode : function (child) {
            var childId = child.getId();
            var inEdges = this.getThingsByAttributeValue('edge', prefix + 'target', childId);
            if (inEdges.length > 0) {
                var parentId = inEdges[0].getSourceId();
                return this.getThingById('node', parentId);
            }
            return null;
        },
        getRootNode : function () {
            var nodes = this.getNodeList();
            var node = nodes[0];
            var rootNode = node;
            while (node !== null) {
                node = this.getParentNode(node);
                if (node !== null) {
                    rootNode = node;
                }
            }
            return rootNode;
        }   
    };
    
    function Network(nexml) {
        copyFields(this, [nexml, Base, Labeled, Listable, Polymorphic]);
        normalizeList(this, 'node', Node);
        normalizeList(this, 'edge', Edge);
    }
    Network.prototype = {
        getNodeList    : Tree.prototype.getNodeList,
        getEdgeList    : Tree.prototype.getEdgeList,
        getChildNodes  : Tree.prototype.getChildNodes,
        getParentNodes : function (child) {
            var childId = child.getId();
            var inEdges = this.getThingsByAttributeValue('edge', prefix + 'target', childId);
            var parents = [];
            if (inEdges.length > 0) {
                for (var i = 0; i < inEdges.length; i++) {
                    var parentId = inEdges[i].getSourceId();
                    parents.push(this.getThingById('node', parentId));
                }
            }
            return parents;
        }
    };
       
    function Trees(nexml) {
        copyFields(this, [nexml, Base, Labeled, OTUsLinker, Listable]);
        normalizeList(this, 'tree', Tree);
        normalizeList(this, 'network', Network);
    }    
    Trees.prototype = {
        getTreeList : function () {
            return this.tree;
        },
        getNetworkList : function () {
            return this.network;
        }
    };
    /*----------------------------------------------------------------*/   
    /* CHARACTERS-RELATED CLASS */
    function State(nexml) {
        copyFields(this, [nexml, Base, Labeled]);
    }
    State.prototype = {
        getSymbol : function () {
            return this[prefix + 'symbol'];
        }
    };
    
    function PolymorphicStateSet(nexml) {
        copyFields(this, [nexml, Base, Labeled]);
    }
    PolymorphicStateSet.prototype.getSymbol = State.prototype.getSymbol;
    
    function UncertainStateSet(nexml) {
        copyFields(this, [nexml, Base, Labeled]);
    }
    UncertainStateSet.prototype.getSymbol = State.prototype.getSymbol;    
    
    function States(nexml) {
        copyFields(this, [nexml, Base, Labeled, Listable]);
        normalizeList(this, 'state', State);
        normalizeList(this, 'uncertain_state_set', State);
        normalizeList(this, 'polymorphic_state_set', State);        
    }
    States.prototype = {
        getStateList : function () {
            return this.state;
        }
    };
    
    function Char(nexml) {
        copyFields(this, [nexml, Base, Labeled]);
    }
    Char.prototype = {
        getStatesId : function () {
            return this[prefix + 'states'];
        }
    };
    
    function Cell(nexml) {
        copyFields(this, [nexml]);
    }
    Cell.prototype = {
        getStateId : function () {
            return this[prefix + 'state'];
        },
        getCharId : function () {
            return this[prefix + 'char'];
        }
    };
    
    function Row(nexml) {
        copyFields(this, [nexml, Base, Labeled, OTULinker]);   
        normalizeList(this, 'cell', Cell);
    }
    Row.prototype = {
        getCellList : function () {
            return this.cell;
        }
    };
    
    function Characters(nexml) {
        copyFields(this, [nexml, Base, Labeled, Listable, Polymorphic, OTUsLinker]);
        normalizeList(this.format, 'states', States);
        normalizeList(this.format, 'char', Char);
        normalizeList(this.matrix, 'row', Row);
    }    
    Characters.prototype = {
        getCharList : function () {
            return this.format.char;
        },
        getStatesList : function () {
            return this.format.states;
        },
        getRowList : function () {
            return this.matrix.row;
        },
        getStatesForChar : function (col) {
            var statesId   = col.getStatesId();
            var statesList = this.getStatesList();
            for (var i = 0; i < statesList.length; i++) {
                if (statesList[i].getId() === statesId) {
                    return statesList[i];
                }
            } 
            return null;
        },
        getCharListForStates : function (states) {
            var statesId = states.getId();
            var result = [];
            var charList = this.getCharList();
            for (var i = 0; i < charList.length; i++) {
                if (charList[i].getStatesId() === statesId) {
                    result.push(charList[i]);
                }
            }
            return result;
        },
        getCharForCell : function (cell) {
            var charId = cell.getCharId();
            var charList = this.getCharList();
            for (var i = 0; i < charList.length; i++) {
                if (charList[i].getId() === charId) {
                    return charList[i];
                }
            }
            return null;
        },
        getSymbolForCell : function (cell) {
            var col = this.getCharForCell(cell);
            var statesId = col.getStatesId();
            if (statesId !== null) {
                var states = this.getStatesForChar(col);
                var stateId = cell.getStateId();
                var state = states.getThingById('state',stateId);
                if ( state === null ) {
                    state = states.getThingById('polymorphic_state_set',stateId);
                }
                if ( state === null ) {
                    state = states.getThingById('uncertain_state_set',stateId);            
                }
                return state.getSymbol();
            }
            else {
                return cell.getStateId();
            }
        },
        getStateForCell : function (cell) {
            var col = this.getCharForCell(cell);
            var statesId = col.getStatesId();
            if (statesId !== null) {
                var states = this.getStatesForChar(col);
                var stateId = cell.getStateId();
                var state = states.getThingById('state',stateId);
                if ( state === null ) {
                    state = states.getThingById('polymorphic_state_set',stateId);
                }
                if ( state === null ) {
                    state = states.getThingById('uncertain_state_set',stateId);            
                }
                return state;
            }
            else {
                return null;
            }
        }
    };        
    
    /*----------------------------------------------------------------*/   
    /* ROOT DOCUMENT CLASS */
    function Document(rootXml) {
        var nexml = rootXml.nex$nexml;
        copyFields(this, [nexml]);
        normalizeList(this, 'otus', OTUs);
        normalizeList(this, 'trees', Trees);
        normalizeList(this, 'characters', Characters);
    }    
    Document.prototype = {
        getMeta : function () {
            return this.meta;
        },    
        getVersion : function () {
            return this[prefix + 'version'];
        },
        getGenerator : function () {
            return this[prefix + 'generator'];
        },
        getOTUsList : function () {
            return this.otus;
        },
        getCharactersList : function () {
            return this.characters;
        },
        getTreesList : function () {
            return this.trees;
        }
    };
    NeXML.Document = Document;        
    /*----------------------------------------------------------------*/ 
})();
