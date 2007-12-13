#! /usr/bin/env python

############################################################################
##  newick.py
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
This module handles the reading and writing of trees in NEWICK format.
"""

from pynexml import datasets
from pynexml import trees

class NewickTreeReader():
    """
    Implementation of TreeReader for NEWICK files and strings.
    """
    
    def __init__(self):
        """
        `tree_class` is a DendroPy TreeFactory object.
        """
        pass

    def read_trees(self, fileobj=None, text=None, tree_block=None):
        """
        Instantiates and returns a TreeBlock object based
        on the Newick-formatted contents read from the file
        descriptor object `fileobj`.
        """
        if tree_block is None:
            tree_block = trees.TreeBlock()
        if fileobj:
            return self.parse_trees(fileobj.read(), tree_block)
        else:
            return self.parse_trees(text, tree_block)

    ## Following methods are class-specific ##

    def parse_trees(self, statement_block, tree_block):
        """
        Given a string block which defines trees in Newick format,
        this parses the Newick strings and adds the trees found to
        dataset.
        """
        statement_block = statement_block.replace('\n','').replace('\r','')
        tree_statements = []
        for statement in statement_block.split(';'):
            statement = statement.strip()
            if statement:
                tree_statements.append(statement + ';')
        newick_parser = NewickTreeParser()
        trees = []
        for tree_statement in tree_statements:
            newick_parser.parse_tree_statement(tree_statement, tree_block)
        return tree_block

class NewickTreeWriter():
    """
    Handles representation and serialization of a DendroPy Tree object
    in NEWICK format.
    """

    def __init__(self, **kwargs):
        """
        Instantiates the object, setting default for various
        formatting/representation options.
        """
        self.edge_weights = True
        self.internal_labels = True
        self.support_as_edge_weights = False
        self.support_as_labels = False
        self.support_as_percentages = False
        self.support_decimals = None

    ### treeio.TreeWriter interface  ###

    def write_tree_document(self, trees, dest):
        """
        Writes a list of DendroPy Tree objects to a full NEWICK
        document.
        """
        return self.write_trees(trees, dest)

    def write_trees(self, trees, dest):
        """
        Writes a list of DendroPy Tree objects as a list of NEWICK
        tree statements.
        """
        for tree in trees:
            self.write_tree(tree, dest)

    def write_tree(self, tree, dest):
        """
        Writes a single DendroPy Tree object as a NEWICK
        tree.
        """
        dest.write(self.compose_node(tree.seed_node) + ';\n')

    ### Derived-class specific methods ###

    def choose_display_tag(self, node):
        """
        Based on current settings, the attributes of a node, and
        whether or not the node is a leaf, returns an appropriate tag.
        """
        if hasattr(node, 'taxon') and node.taxon:
            return node.taxon
        elif hasattr(node, 'label') and node.label:
            return node.label
        elif len(node.children()) == 0:
            # force label if a leaf node
            return node.node_id
        else:
            return ""
        
    def compose_node(self, node):
        """
        Given a DendroPy Node, this returns the Node as a NEWICK
        statement according to the class-defined formatting rules.
        """
        children = node.children()
        if children:
            subnodes = [self.compose_node(child) for child in children]
            statement = '(' + ','.join(subnodes) + ')'
            if self.internal_labels:
                statement = statement + self.choose_display_tag(node)
            if node.edge.weight != None and self.edge_weights:
                try:
                    statement =  "%s:%f" \
                                % (statement, float(node.edge.weight))
                except ValueError:
                    statement =  "%s:%s" \
                                % (statement, node.edge.weight)
            return statement
        else:
            if self.internal_labels:
                statement = self.choose_display_tag(node)
            if node.edge.weight != None and self.edge_weights:
                try:
                    statement =  "%s:%0.10f" \
                                % (statement, float(node.edge.weight))
                except ValueError:
                    statement =  "%s:%s" \
                                % (statement, node.edge.weight)
            return statement

class NewickTreeParser(object):
    """
    Encapsulates process of generating a (single) DendroPy Tree object
    based on a (single) Newick tree statement string. Slow, but
    (fairly) robust.
    """

    punctuation = '\(\)\[\]\{\}\\\/\,\;\:\=\*\'\"\`\+\-\<\>'
    whitespace = ' \0\t\n\r'

    def __init__(self):
        """
        Must be instantiated with factory object.
        """
        self.statement = ''
        self.curr_pos = 0
        self.current_token = None

    def parse_tree_statement(self, tree_statement, tree_block):
        """
        Processes a TREE command. Assumes that the input stream is
        located at the beginning of the statement (i.e., the first
        parenthesis that defines the tree).
        """
        self.statement = tree_statement
        self.curr_pos = 0
        self.current_token = None
        child_nodes = []
        tree = trees.Tree()
        token = self.read_next_token()
        while token and token != ';' and token != ':':
            # process nodes until no more tokens, end of tree
            # statement, or ':' is encountered, presumably outside
            # main tree parenthetical statement (i.e., length of root
            node = self.parse_tree_node(tree, tree_block.taxa_block)
            if node:
                child_nodes.append(node)
            token = self.current_token
            if self.current_token == ')':
                # OK, I'll be the first to admit that this is rather
                # hacky but it works.
                # If an end-parenthesis is encountered ...
                token = self.read_next_token()
                if token and not token in NewickTreeParser.punctuation:
                    break
        for node in child_nodes:
            tree.seed_node.add_child(node)
        if token and not token in NewickTreeParser.punctuation:
            tree.seed_node.taxon = token
            token = self.read_next_token()
        if token and token == ':':
            weight = self.read_next_token(ignore_punct='-')
            tree.seed_node.edge.weight = weight
        tree_block.append(tree)
        return tree

    def parse_tree_node(self, tree, taxa_block):
        """
        Processes a TREE statement. Assumes that the file reader is
        positioned right after the '(' token in a TREE statement or
        right after a comma following a node inside a tree statement.
        """
        node = trees.Node()
        token = self.read_next_token()
        while token and token != ')' and token != ',':
            if not token in NewickTreeParser.punctuation:
                node.taxon = taxa_block.find_taxon(label=token, update=True)
            if token == ':':
                edge_weight_str = self.read_next_token(ignore_punct='-')
                try:
                    node.edge.weight = float(edge_weight_str)
                except ValueError:
                    node.edge.weight = edge_weight_str
            if token == '(':
                while token and token != ')':
                    child_node = self.parse_tree_node(tree, taxa_block=taxa_block)
                    if child_node:
                        node.add_child(child_node)
                    token = self.current_token
            token = self.read_next_token()
        return node

    def is_eos(self):
        """
        Returns True if currently at end of the string stream.
        """
        return self.curr_pos >= len(self.statement)

    def read_next_char(self):
        """
        Advances the stream cursor to the next character and returns
        it.
        """
        self.curr_pos = self.curr_pos + 1
        if self.curr_pos < len(self.statement):
            return self.statement[self.curr_pos]
        else:
            return ''

    def skip_to_significant_character(self):
        """
        Advances to the first non-whitespace character.
        """
        while (self.statement[self.curr_pos] in NewickTreeParser.whitespace) \
                  and not self.is_eos():
            self.read_next_char()

    def read_next_token(self, ignore_punct=None):
        """
        Reads the next token in the file stream. A token in this
        context is any word or punctuation character outside of a
        comment block.
        """
        if ignore_punct == None:
            ignore_punct = []
        if not self.is_eos():
            token = ''
            self.skip_to_significant_character()
            if not self.is_eos():
                if self.statement[self.curr_pos] == "'":
                    self.read_next_char()
                    end_quote = False
                    while not end_quote and not self.is_eos():
                        if self.statement[self.curr_pos] == "'":
                            self.read_next_char()
                            if self.statement[self.curr_pos] == "'":
                                token = token + "'"
                                self.read_next_char()
                            else:
                                end_quote = True
                        else:
                            token = token + self.statement[self.curr_pos]
                            self.read_next_char()
                else:
                    # it gets pretty hairy here ...

                    if (self.statement[self.curr_pos] \
                        in NewickTreeParser.punctuation) \
                           and (self.statement[self.curr_pos] \
                                not in ignore_punct):
                        token = self.statement[self.curr_pos]
                        self.read_next_char()
                    else:
                        while not self.is_eos() \
                                  and not ((self.statement[self.curr_pos] \
                                            in NewickTreeParser.whitespace) \
                              or (self.statement[self.curr_pos] in \
                                  NewickTreeParser.punctuation \
                                  and self.statement[self.curr_pos] \
                                  not in ignore_punct)):
                            token = token + self.statement[self.curr_pos]
                            self.read_next_char()
                self.current_token = token
            else:
                self.current_token = None
        else:
            self.current_token = None
        return self.current_token


if __name__ == "__main__":
    source = "/home/jeet/Documents/Codeworks/Portfolios/Python/Projects/Phylogenetics/DendroPy/versions/trunk/pynexml/tests/files/newick_trees.tre"
    dataset = datasets.Dataset()
    tree_block = dataset.new_tree_block()
    nw = NewickTreeReader()
    fo = open(source, 'r')
    nw.read_trees(fileobj=fo, tree_block=tree_block)
    for taxa_block in dataset.taxa_blocks:
        print taxa_block
    for tree_block in dataset.tree_blocks:
        print "\n***TREE BLOCK: " + tree_block.elem_id + "/" + str(tree_block.label) + "***"
        for tree in tree_block:
            print
            print "\n***TREE: " + tree.elem_id + "/" + str(tree.label) + "***"
            print "Terminals:", [leaf.taxon.label for leaf in tree.leaf_iter()]            
            for node in tree.preorder_node_iter():
                print node.elem_id, ' ',
                if node.taxon is not None:
                    print node.taxon.label
                else:
                    print
    
    
