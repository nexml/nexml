#! /usr/bin/env python

############################################################################
##  nexml.py
##
##  Part of the PhyloInfo phylogenetic data parsing library.
##
##  Copyright 2007 Jeet Sukumaran and Mark T. Holder.
##
##  This program is free software; you can redistribute it and/or modify
##  it under the terms of the GNU General Public License as published by
##  the Free Software Foundation; either version 3 of the License, or
##  (at your option) any later version.
##
##  This program is distributed in the hope that it will be useful,
##  but WITHOUT ANY WARRANTY; without even the implied warranty of
##  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##  GNU General Public License for more details.
##
##  You should have received a copy of the GNU General Public License along
##  with this program. If not, see <http://www.gnu.org/licenses/>.
##
############################################################################

"""
This module wraps routines needed for reading and writing trees in
NEXML format.
"""

import textwrap
from pynexml import datasets
from pynexml import taxa
from pynexml import characters
from pynexml import trees
from pynexml import xmlparser

def _to_nexml_indent_items(items, indent="", indent_level=0):
    """
    Renders list of items into a string of lines in which each line is
    indented appropriately.
    """
    return '\n'.join(["%s%s" % (indent * indent_level, str(item)) \
                     for item in items])

def _to_nexml_dict(annotes_dict, indent="", indent_level=0):
    """
    Composes a nexml dict entry, given a python dictionary.
    """
    main_indent = indent * indent_level    
    parts = []
    parts.append('%s<dict>' % main_indent)            
    keyvals = _to_nexml_dict_keyvalues(annotes_dict=annotes_dict,
                                    indent=indent,
                                    indent_level=indent_level+1)
    parts.append(_to_nexml_indent_items(keyvals, indent=indent, indent_level=0))
    parts.append('%s</dict>' % (main_indent))        
    return parts

def _to_nexml_dict_keyvalues(annotes_dict, indent="", indent_level=0):
    """
    Returns a list of lines corresponding to a nexml rendering of a
    dictionary.
    """
    parts = []
    subindent = indent * (indent_level + 0)
    for key, value in annotes_dict.items():
        parts.append('%s<key>%s</key>' % (subindent, key))
        anvalue = _to_nexml_dict_value(value=value[0],
                                       type_hint=value[1],
                                       indent=indent,
                                       indent_level=indent_level)
        parts.append(_to_nexml_indent_items(anvalue, indent, indent_level=0))
    return parts    
    
def _to_nexml_dict_value(value, type_hint=None, indent="", indent_level=0):
    """
    Returns a list of lines nexml representation of a value. Right now, only deals
    with lists/vector types vs 'others'. Which means dictionaries will
    not get returned properly, and thus client code must handle nested
    dictionaries themselves.
    """
    main_indent = indent * indent_level
    if type_hint is None:
        value_type = _to_nexml_dict_value_type(value)
    else:
        value_type = type_hint
    if value_type == 'boolean':
        value = str(value==True).lower()
    if isinstance(value, list):
        value_str = "%s<%s>%s</%s>" % (main_indent,
                                       value_type,
                                       ' '.join([str(item) for item in value]),
                                       value_type)
        return [value_str]
    elif isinstance(value, dict):
        return _to_nexml_dict(value, indent=indent, indent_level=indent_level)
    else:
        return ["%s<%s>%s</%s>" % (main_indent, value_type, str(value), value_type)]

def _to_nexml_dict_value_type(value):
    """
    Figures out the value type, and returns and appropriate nexml
    string corresponding to it.
    """
    value_type = 'any'
    if isinstance(value, list):
        # assumes rest of the vector is same type as the first element
        value_type = _to_nexml_dict_value_type(value[0]) + 'vector'
    elif isinstance(value, dict):
        value_type = 'dict'
    else:
        if type(value) == bool:
            value_type = 'boolean'
        elif type(value) == float:
            value_type = 'float'
        elif type(value) == int:
            value_type = 'integer'
        elif type(value) == str:
            value_type = 'string'
        else:
            value_type = 'any'
    return value_type

def _to_nexml_chartype(chartype):
    """
    Returns nexml characters element attribute corresponding to given
    chartype.
    """
    if chartype == characters.DNA_CHARTYPE:
        return "nex:DnaSeqs"
    if chartype == characters.RNA_CHARTYPE:
        return "nex:RnaSeqs"
    return None

def _to_nexml_tree_weight_type(weight_type):
    """
    Returns attribute string for nexml tree type depending on whether
    `weight_type` is an int or a float.
    """
    if weight_type == int:
        return "nex:IntTree"
    elif weight_type == float:
        return "nex:FloatTree"
    else:
        raise Exception('Unrecognized value class %s' % weight_type)

def _from_nexml_tree_weight_type(type_attr):
    """
    Given an attribute string read from a nexml tree element, returns
    the python class of the edge weight attribute.
    """
    if type_attr == "nex:IntTree":
        return int
    else:
        return float

def _from_nexml_chartype(chartype):
    """
    Returns Phyloplex characters type corresponding to nexml chartype.
    """
    if chartype == "nex:RestrictionSeqs":
        return None
    if chartype == "nex:StandardCells":
        return None
    if chartype == "nex:ContinuousCells":
        return None
    if chartype == "nex:DnaSeqs":
        return characters.DNA_CHARTYPE
    if chartype == "nex:RnaSeqs":
        return characters.RNA_CHARTYPE
    if chartype == "nex:ContinuousSeqs":
        return None
    if chartype == "nex:StandardSeqs":
        return None
    
def _from_nexml_dict_value(value, value_type):
    """
    A text representation of a value of type `type`, where `type`
    is specified in terms of an nexml element, returns the Python
    representation of the value.
    """
    parsed_value = None
    value = value.strip()
    if value_type == "integer":
        try:
            parsed_value = int(value)
        except ValueError:
            raise Exception("Could not parse integer value")
    elif value_type == "float":
        try:
            parsed_value = float(value)
        except ValueError:
            raise Exception("Could not parse float value")
    elif value_type == "boolean":
        try:
            parsed_value = bool(value)
        except ValueError:
            raise Exception("Could not parse boolean value")
    elif value_type == "string":
        try:
            parsed_value = str(value)
        except ValueError:
            raise Exception("Could not parse string value")
    else:
        # what else to do?
        parsed_value = value
    return parsed_value

class NexmlReader(datasets.Reader):
    """
    Implements thinterface for handling NEXML files.
    """

    def __init__(self):
        """
        `tree_factory` is a DendroPy TreeFactory class or derived
        object.
        """
        datasets.Reader.__init__(self)

    ## Implementation of the datasets.Reader interface ##

    def read_dataset(self, fileobj, dataset=None):
        """
        Instantiates and returns a DataSet object based on the
        NEXML-formatted contents read from the file descriptor object
        `fileobj`. If `dataset` is given, its factory methods will be
        used to instantiate objects.
        """
        xmldoc = xmlparser.XmlDocument(filesrc=fileobj)
        return self.parse_dataset(xmldoc, dataset)

    ## Following methods are class-specific ###

    def parse_dataset(self, xml_doc, dataset):
        """
        Given an XMLDocument, parses the XmlElement representation of
        taxon sets, character matrices, and trees into a DataSet object.
        """
        if dataset is None:
            dataset = datasets.Dataset()
        self.parse_taxa_blocks(xml_doc, dataset)
        self.parse_char_blocks(xml_doc, dataset)
        self.parse_tree_blocks(xml_doc, dataset)
        return dataset
        
    def parse_taxa_blocks(self, xml_doc, dataset):
        """
        Given an XMLDocument, parses the XmlElement representation of
        taxon sets into a TaxaBlocks objects.
        """
        nxt = _NexmlTaxaParser()
        for taxa_element in xml_doc.getiterator('otus'):
            taxa_block = nxt.parse_taxa(taxa_element, dataset) 
        
    def parse_char_blocks(self, xml_doc, dataset):
        """
        Given an XMLDocument, parses the XmlElement representation of
        character sequences into a list of CharacterMatrix objects.
        """
        nxc = _NexmlCharBlockParser()
        for char_block_element in xml_doc.getiterator('characters'):
            nxc.parse_char_block(char_block_element, dataset)

    def parse_tree_blocks(self, xml_doc, dataset):
        """
        Given an XmlDocument object, parses the XmlElement structural
        representations of a set of NEXML treeblocks (`nex:trees`) and
        returns a TreesBlocks object corresponding to the NEXML.
        """
        nxt = _NexmlTreesParser()
        for trees_idx, trees_element in enumerate(xml_doc.getiterator('trees')):
            nxt.parse_trees(trees_element, dataset, trees_idx)

class _NexmlElementParser(object):
    """
    Base parser class: wraps around annotations/dictionary element handling.
    """
    
    def __init__(self):
        """
        Right now, does nothing ...
        """
        pass

    def parse_annotations(self, annotated, nxelement):
        """
        Given an nexml element, this looks for a 'dict' child element
        and passes it to the dictionary parse if found. Results are
        placed as attributes of `annotated`.
        """
        xml_dict = nxelement.find('dict')
        if xml_dict:
            return self.parse_dict(annotated=annotated, xml_dict=xml_dict)

    def parse_dict(self, annotated, xml_dict):
        """
        This parses an xml_dict and sets the attributes of annotable
        correspondingly.
        """
        xml_keys = []
        xml_values = []            
        for child in xml_dict.getchildren():
            if child.tag == 'key':
                xml_keys.append(child)
            else:
                xml_values.append(child)
        if len(xml_keys) > 0 or len(xml_values) > 0:
            if len(xml_keys) == len(xml_values):
                xml_keyvals = dict(zip(xml_keys, xml_values))
                self.parse_keyvals(annotated, xml_keyvals)
            else:
                raise Exception("Unequal numbers of keys and values in annotations")                    

    def parse_keyvals(self, annotated, xml_keyvals):
        """
        Given a dictionary where the keys are nexml dict key
        XmlElements and the values are nexl dict value XmlElements
        corresponding to those keys, this will parse the elements into
        the attributes of an Annotable object.
        """
        for xml_key, xml_value in xml_keyvals.items():
            an_key = xml_key.text
            an_value = None
            if xml_value.tag == 'dict':
                subannotable = annotable.Annotable()
                self.parse_dict(annotable, xml_value)
                an_value = subannotable
            elif xml_value.tag.count('vector'):
                an_value = []
                vector_text = xml_value.text
                vector_text = vector_text.strip('\n').strip('\r').strip()
                vector_type = xml_value.tag.replace('vector', '')
                if vector_type == 'dict':
                    ## must handle it here:
                    ## loop through child elements of xml_value,
                    ## parsing the dicts and building up a list of
                    ## Annotable objects
                    raise NotImplementedError
                else:
                    vector_items = vector_text.split()
                    for item in vector_items:
                        an_value.append(_from_nexml_dict_value(item, vector_type))
            else:
                an_value = _from_nexml_dict_value(xml_value.text, xml_value.tag)
            if an_key is not None and an_value is not None:
                setattr(annotated, an_key, an_value)
                annotated.annotate(an_key)

class _NexmlTreesParser(_NexmlElementParser):
    """
    Parses an XmlElement representation of NEXML format tree blocks.
    """

    def __init__(self):
        """
        Must be given tree factory to create trees.
        """
        super(_NexmlTreesParser, self).__init__()

    def parse_trees(self, nxtrees, dataset, trees_idx=None, tree_factory=None):
        """
        Given an XmlElement object representing a NEXML treeblock,
        self.nxtrees (corresponding to a `nex:trees` element), this
        will construct and return a TreeBlock object defined by the
        underlying NEXML. 
        """
        elem_id = nxtrees.get('id', "Trees" + str(trees_idx))
        label = nxtrees.get('label', None)
        taxa_id = nxtrees.get('otus', None)
        if taxa_id is None:
            raise Exception("Taxa block not specified for trees block \"%s\"" % tree_block.elem_id)
        taxa_block = dataset.find_taxa_block(elem_id = taxa_id)
        if not taxa_block:
            raise Exception("Taxa block \"%s\" not found" % taxa_id)
        taxa_block = taxa_block
        tree_block = dataset.new_tree_block(elem_id=elem_id, label=label, taxa_block=taxa_block)
        tree_counter = 0
        for tree_element in nxtrees.getiterator('tree'):
            tree_counter = tree_counter + 1
            elem_id = tree_element.get('id', tree_counter)
            label = tree_element.get('label', '')
            if tree_factory is not None:
                treeobj = tree_factory(elem_id=elem_id, label=label)
            else:
                treeobj = dataset.tree_factory(elem_id=elem_id, label=label)
            tree_type_attr = tree_element.get('{http://www.w3.org/2001/XMLSchema-instance}type')
            treeobj.weight_type = _from_nexml_tree_weight_type(tree_type_attr)
            nodes = self.parse_nodes(tree_element, taxa_block=tree_block.taxa_block, node_factory=dataset.node_factory)
            edges = self.parse_edges(tree_element, weight_type=treeobj.weight_type, edge_factory=dataset.edge_factory)
            for edge in edges.values():
                # EDGE-ON-ROOT:
                # allow "blank" tail nodes: so we only enforce
                # this check if tail node id is specified
                if edge.tail_node_id and edge.tail_node_id not in nodes:
                    msg = 'Edge "%s" specifies a non-defined ' \
                          'source node ("%s")' % (edge.elem_id,
                                                  edge.tail_node_id)
                    raise Exception(msg)
                if edge.head_node_id not in nodes:
                    msg = 'Edge "%s" specifies a non-defined ' \
                          'target node ("%s")' % (edge.elem_id,
                                                  edge.head_node_id)
                    raise Exception(msg)

                if edge.head_node_id and edge.tail_node_id:
                    head_node = nodes[edge.head_node_id]
                    head_node.edge = edge
                    tail_node = nodes[edge.tail_node_id]                    
                    tail_node.add_child(head_node)
                elif edge.head_node_id and not edge.tail_node_id:
                    head_node = nodes[edge.head_node_id]
                    head_node.edge = edge

            # find node(s) without parent
            parentless = []
            for node in nodes.values():
                if node.parent_node == None:
                    parentless.append(node)

            # If one parentless node found, this is the root: we use
            # it as the tree head node. If multiple parentless nodes
            # are found, then we add them all as children of the
            # existing head node. If none, then we have some sort of
            # cyclicity, and we are not dealing with a tree.
            if len(parentless) == 1:
                treeobj.seed_node = parentless[0]
            elif len(parentless) > 1:
                for node in parentless:
                    treeobj.seed_node.add_child(node)
            else:
                raise Exception("Structural error: tree must be acyclic.")
                
            rootedge = self.parse_root_edge(tree_element, weight_type=treeobj.weight_type, edge_factory=dataset.edge_factory)
            if rootedge:
                if rootedge.head_node_id not in nodes:
                    msg = 'Edge "%s" specifies a non-defined ' \
                          'target node ("%s")' % (edge.elem_id,
                                                  edge.head_node_id)
                    raise Exception(msg)
                else:
                    nodes[rootedge.head_node_id].edge = rootedge
                    ### should we make this node the seed node by rerooting the tree here? ###
            tree_block.append(treeobj)

    def parse_nodes(self, tree_element, taxa_block, node_factory):
        """
        Given an XmlElement representation of a NEXML tree element,
        (`nex:tree`) this will return a dictionary of DendroPy Node
        objects created with the node factory method, self.new_node,
        with the node_id as the key.
        """
        nodes = {}
        for nxnode in tree_element.getiterator('node'):
            node_id = nxnode.get('id', None)
            nodes[node_id] = node_factory()
            nodes[node_id].elem_id = node_id
            nodes[node_id].label = nxnode.get('label', None)
            taxon_id = nxnode.get('otu', None)
            if taxon_id is not None:
                taxon = taxa_block.find_taxon(elem_id=taxon_id, update=False)
                if not taxon:
                    raise Exception('Taxon with id "%s" not defined in taxa block "%s"' % (taxon_id, taxa.elem_id))
                nodes[node_id].taxon = taxon
            self.parse_annotations(annotated=nodes[node_id], nxelement=nxnode)
        return nodes
        
    def parse_root_edge(self, tree_element, weight_type, edge_factory):
        """
        Returns the edge subtending the root node, or None if not defined.
        """
        rootedge = tree_element.find('rootedge')
        if rootedge:
            edge = edge_factory()
            edge.head_node_id = rootedge.get('target', None)
            edge.elem_id = rootedge.get('id', 'e' + str(id(edge)))
            edge_weight_str = weight_type(rootedge.get('length', '0.0'))
            edge_weight = None
            try:
                edge_weight = weight_type(edge_weight_str)
            except:
                msg = 'Edge %d ("%s") `length` attribute is not a %s' \
                      % (edge_counter, edge.elem_id, str(weight_type))
                raise Exception(msg)
            edge.weight = edge_weight
            self.parse_annotations(annotated=edge, nxelement=rootedge)            
            return edge
        else:
            return None

    def parse_edges(self, tree_element, weight_type, edge_factory):
        """
        Given an XmlElement representation of a NEXML tree element
        this will return a dictionary of DendroPy Edge objects created with
        the edge factory method, self.new_edge, with the elem_id as
        key. As at this stage, this method knows nothing about defined
        nodes, the Edge tail_node and head_node properties of the
        Edge are not set, but the tail_node_id and head_node_id are.
        """
        edges = {}
        edge_counter = 0        
        for nxedge in tree_element.getiterator('edge'):
            edge = edge_factory()
            edge_counter = edge_counter + 1
            edge.tail_node_id = nxedge.get('source', None)
            edge.head_node_id = nxedge.get('target', None)
            edge.elem_id = nxedge.get('id', 'e' + str(edge_counter))
            edge_weight_str = weight_type(nxedge.get('length', '0.0'))

            if not edge.tail_node_id:
                msg = 'Edge %d ("%s") does not have a source' \
                      % (edge_counter, edge.elem_id)
                raise Exception(msg)

            if not edge.head_node_id:
                msg = 'Edge %d ("%s") does not have a target' \
                      % (edge_counter, edge.elem_id)
                raise Exception(msg)
            edge_weight = None
            try:
                edge_weight = weight_type(edge_weight_str)
            except:
                msg = 'Edge %d ("%s") `length` attribute is not a %s' \
                      % (edge_counter, edge.elem_id, str(weight_type))
                raise Exception(msg)
            edge.weight = edge_weight
            self.parse_annotations(annotated=edge, nxelement=nxedge)            
            edges[edge.elem_id] = edge
        return edges

class _NexmlTaxaParser(_NexmlElementParser):
    """
    Parses an XmlElement representation of NEXML taxa blocks.
    """

    def __init__(self):
        """
        Does nothing too useful right now.
        """
        super(_NexmlTaxaParser, self).__init__()

    def parse_taxa(self, nxtaxa, dataset):
        """
        Given an XmlElement representing a nexml taxa block, this
        instantiates and returns a corresponding DendroPy Taxa object.
        """
        elem_id = nxtaxa.get('id', None)
        label = nxtaxa.get('label', None)
        taxa_block = taxa.TaxaBlock(elem_id=elem_id, label=label)
        for idx, nxtaxon in enumerate(nxtaxa.getiterator('otu')):
            taxon = taxa.Taxon(nxtaxon.get('id', "s" + str(idx) ), nxtaxon.get('label', "Taxon" + str(idx)))
            self.parse_annotations(annotated=taxon, nxelement=nxtaxon)
            taxa_block.append(taxon)
        dataset.taxa_blocks.append(taxa_block)
        
class _NexmlCharBlockParser(_NexmlElementParser):
    """
    Parses an XmlElement representation of NEXML taxa blocks.
    """

    def __init__(self):
        """
        Does nothing too useful right now.
        """
        super(_NexmlCharBlockParser, self).__init__()

    def parse_char_block(self, nxchars, dataset):
        """
        Given an XmlElement representing a nexml characters block, this
        instantiates and returns a corresponding DendroPy CharacterMatrix object.
        """
        elem_id = nxchars.get('id', None)
        label = nxchars.get('label', None)
        char_block = characters.CharBlock(elem_id=elem_id, label=label)
        char_block.elem_id = elem_id
        char_block.label = label        
        taxa_id = nxchars.get('otus', None)
        if taxa_id is None:
            raise Exception("Taxa block not specified for trees block \"%s\"" % char_block.elem_id)
        taxa_block = dataset.find_taxa_block(elem_id = taxa_id)
        if not taxa_block:
            raise Exception("Taxa block \"%s\" not found" % taxa_id)
        char_block.taxa_block = taxa_block
        nx_chartype = nxchars.get('{http://www.w3.org/2001/XMLSchema-instance}type', None)
        chartype = _from_nexml_chartype(nx_chartype)
        if chartype is None:
            ## handle unknown character formats here ##
            pass
        else:
            char_block.chartype = chartype
            matrices = [matrix for matrix in nxchars.getiterator('matrix')]
            matrix = nxchars.find('matrix')
            for row in matrix.getiterator('row'):
                elem_id = row.get('id', None)
                taxon_id = row.get('otu', None)
                taxon = taxa_block.find_taxon(elem_id=taxon_id, update=False)
                if not taxon:
                    raise Exception('Taxon with id "%s" not defined in taxa block "%s"' % (taxon_id, taxa.elem_id))                
                seq = row.findtext('seq')
                if seq is not None:
                    seq = seq.replace(' ', '').replace('\n', '').replace('\r','')
                    state_names = [seqchar for seqchar in seq]
                else:
                    state_names = []
                char_seq = characters.CharSequence(chartype=chartype)
                char_seq.elem_id = elem_id
                char_seq.state_names = state_names
                char_seq.taxon = taxon
                char_block[taxon] = char_seq
            dataset.char_blocks.append(char_block)
                
class NexmlWriter(datasets.Writer):
    """
    Implements the DataWriter interface for handling NEXML files.
    """

    def __init__(self):
        """
        Calls the base class constructor.
        """
        datasets.Writer.__init__(self)
        self.indent = "    "

    ### datasets.Writer interface  ###

    def write_dataset(self, dataset, dest):
        """
        Writes a list of DendroPy Tree objects to a full NEXML
        document.
        """
        self.write_to_nexml_open(dest, indent_level=0)
        self.write_taxa_blocks(taxa_blocks=dataset.taxa_blocks, dest=dest)
        self.write_char_blocks(char_blocks=dataset.char_blocks, dest=dest)
        self.write_tree_blocks(tree_blocks=dataset.tree_blocks, dest=dest)
        self.write_to_nexml_close(dest, indent_level=0)

    ### class-specific  ###

    def write_taxa_blocks(self, taxa_blocks, dest, indent_level=1):
        """
        Writes out TaxaBlocks.
        """
        for idx, taxa_block in enumerate(taxa_blocks):
            dest.write(self.indent * indent_level)
            parts = []
            parts.append('otus')
            if taxa_block.elem_id is not None:
                parts.append('id="%s"' % taxa_block.elem_id)
            else:
                raise Exception("Taxa block given without ID")
            if taxa_block.label:
                parts.append('label="%s"' % taxa_block.label)
            dest.write("<%s>\n" % ' '.join(parts))
            for taxon in taxa_block:
                dest.write(self.indent * (indent_level+1))
                parts = []
                parts.append('otu')
                if taxon.elem_id is not None:
                    parts.append('id="%s"' % taxon.elem_id)
                else:
                    raise Exception("Taxon without ID")
                if taxon.label:
                    parts.append('label="%s"' % taxon.label)
                dest.write("<%s />\n" % ' '.join(parts))
            dest.write(self.indent * indent_level)                
            dest.write('</otus>\n')

    def write_tree_blocks(self, tree_blocks, dest, indent_level=1):
        """
        Writes out TreeBlocks.
        """
        for idx, tree_block in enumerate(tree_blocks):
            dest.write(self.indent * indent_level)
            parts = []
            parts.append('trees')
            if tree_block.elem_id is not None:
                parts.append('id="%s"' % tree_block.elem_id)
            else:
                raise Exception("Tree block given without ID")
            if tree_block.label:
                parts.append('label="%s"' % tree_block.label)
            parts.append('otus="%s"' % tree_block.taxa_block.elem_id)
            dest.write("<%s>\n" % ' '.join(parts))
            for tree in tree_block:
                self.write_tree(tree=tree, dest=dest, indent_level=2)
            dest.write(self.indent * indent_level)                
            dest.write('</trees>\n')
                            
    def write_char_blocks(self, char_blocks, dest, indent_level=1):
        """
        Writes out character matrices.
        """
        for idx, char_block in enumerate(char_blocks):
            dest.write(self.indent * indent_level)
            parts = []
            parts.append('characters')
            if char_block.elem_id is not None:
                parts.append('id="%s"' % char_block.elem_id)
            else:
                raise Exception("Character block without ID")
            if char_block.label:
                parts.append('label="%s"' % char_block.label)
            parts.append('otus="%s"' % char_block.taxa_block.elem_id)                
            parts.append('xsi:type="%s"' % str(_to_nexml_chartype(char_block.chartype)))
            dest.write("<%s>\n" % ' '.join(parts))
            dest.write(self.indent * (indent_level+1))
            dest.write("<matrix>\n")            
            for row in char_block.values():
                dest.write(self.indent*(indent_level+2))
                parts = []
                parts.append('row')
                if row.elem_id is not None:
                    parts.append('id="%s"' % row.elem_id)
                else:
                    raise Exception("Row without ID")
                if row.taxon:
                    parts.append('otu="%s"' % row.taxon.elem_id)
                dest.write("<%s>\n" % ' '.join(parts))

                ### actual sequences get written here ###
                seqlines = textwrap.fill(''.join(row.state_names),
                                       width=70,
                                       initial_indent=self.indent*(indent_level+3) + "<seq>",
                                       subsequent_indent=self.indent*(indent_level+4),
                                       break_long_words=True)
                seqlines = seqlines + "</seq>\n"
                dest.write(seqlines)
                dest.write(self.indent * (indent_level+2))
                dest.write('</row>\n')
            dest.write(self.indent * (indent_level+1))
            dest.write("</matrix>\n")
            dest.write(self.indent * indent_level)                
            dest.write('</characters>\n')
        
    def write_tree(self, tree, dest, indent_level=0):
        """
        Writes a single DendroPy Tree object as a NEXML nex:tree
        element.
        """
        parts = []
        parts.append('tree')
        if hasattr(tree, 'elem_id') and tree.elem_id is not None:
            parts.append('id="%s"' % tree.elem_id)
        else:
            parts.append('id="%s"' % ("Tree" + str(id(tree))))
        if hasattr(tree, 'label') and tree.label:
            parts.append('label="%s"' % tree.label)
        if hasattr(tree, 'weight_type'):
            parts.append('xsi:type="%s"' % _to_nexml_tree_weight_type(tree.weight_type))
        else:
            parts.append('xsi:type="nex:FloatTree"')
        parts = ' '.join(parts)
        dest.write('%s<%s>\n'
                   % (self.indent * indent_level, parts))        
        for node in tree.preorder_node_iter():
            self.write_node(node=node, dest=dest, indent_level=indent_level+1)
        for edge in tree.preorder_edge_iter():
            self.write_edge(edge=edge, dest=dest, indent_level=indent_level+1)
        dest.write('%s</tree>\n' % (self.indent * indent_level))            
        
    def write_to_nexml_open(self, dest, indent_level=0):
        """
        Writes the opening tag for a nexml element.
        """
        parts = []
        parts.append('<?xml version="1.0" encoding="ISO-8859-1"?>')
        parts.append('<nex:nexml')
        parts.append('%sversion="1.0"' % (self.indent * (indent_level+1)))
        parts.append('%sxmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
                     % (self.indent * (indent_level+1)))
        parts.append('%sxmlns:xml="http://www.w3.org/XML/1998/namespace"' \
                     % (self.indent * (indent_level+1)))
        parts.append('%sxsi:schemaLocation="http://www.nexml.org/1.0 nexml.xsd"'
                     % (self.indent * (indent_level+1)))
        parts.append('%sxmlns:nex="http://www.nexml.org/1.0">\n'
                     % (self.indent * (indent_level+1)))
        dest.write('\n'.join(parts))

    def write_to_nexml_close(self, dest, indent_level=0):
        """
        Closing tag for a nexml element.
        """
        dest.write('%s</nex:nexml>' % (self.indent*indent_level))

    def write_node(self, node, dest, indent_level=0):
        """
        Writes out a NEXML node element.
        """
        parts = []
        parts.append('<node')
        parts.append('id="%s"' % node.elem_id)
        if hasattr(node, 'label') and node.label:
            parts.append('label="%s"' % node.label)
        if hasattr(node, 'taxon') and node.taxon:
            parts.append('otu="%s"' % node.taxon.elem_id)
        parts = ' '.join(parts)
        dest.write('%s%s' % ((self.indent * indent_level), parts))
        if node.has_annotations():
            dest.write('>\n')
            self.write_annotations(node, dest, indent_level=indent_level+1)
            dest.write('%s</node>\n' % (self.indent * indent_level))
        else:
            dest.write(' />\n')
        
    def write_edge(self, edge, dest, indent_level=0):
        """
        Writes out a NEXML edge element.
        """
        if edge.head_node:
            parts = []
            if edge.tail_elem_id != None:
                tag = "edge"
                parts.append('<%s' % tag)
                parts.append('source="%s"' % edge.tail_elem_id)
            else:
                # EDGE-ON-ROOT:
                tag = "rootedge"
                parts.append('<%s' % tag) 
            if edge.head_elem_id != None:
                parts.append('target="%s"' % edge.head_elem_id)
            if hasattr(edge, 'elem_id') and edge.elem_id:
                parts.append('id="%s"' % edge.elem_id)
            if hasattr(edge, 'weight') and edge.weight != None:
                parts.append('length="%s"' % edge.weight)

            # only write if we have more than just the 'edge' and '/' bit
            if len(parts) > 2:
                parts = ' '.join(parts)
                dest.write('%s%s' % ((self.indent * indent_level), parts))
                if edge.has_annotations():
                    dest.write('>\n')
                    self.write_annotations(edge, dest,
                                           indent_level=indent_level+1)
                    dest.write('%s</%s>\n' % ((self.indent * indent_level), tag))
                else:
                    dest.write(' />\n')

    def write_annotations(self, annotated, dest, indent_level=0):
        """
        Writes out annotations for an Annotable object.
        """
        annotes_dict = annotated.annotations()
        if len(annotes_dict) > 0:
            parts = _to_nexml_dict(annotes_dict, self.indent, indent_level)
            parts = '\n'.join(parts)
            dest.write(parts + '\n')

def basic_test():
    source = "tests/sources/comprehensive.xml"
    target = "tests/output/comprehensive_parsed.xml"
    nexmlr = NexmlReader()
    dataset = nexmlr.get_dataset(source)
    for taxa_block in dataset.taxa_blocks:
        print taxa_block
    for tree_block in dataset.tree_blocks:
        print "\n***" + tree_block.elem_id + "/" + tree_block.label + "***"
        for tree in tree_block:
            print
            for node in tree.preorder_node_iter():
                print node, ' ',
    nexmlw = NexmlWriter()
    print
    print
    print nexmlw.compose_dataset(dataset)
    output = open(target, 'w')
    nexmlw.store_dataset(dataset=dataset, destination=output)
    
if __name__ == "__main__":
    basic_test()
    
