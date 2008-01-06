#! /usr/bin/env python

############################################################################
##  test_io.py
##
##  Part of the PyNexml phylogenetic tree manipulation library.
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
This module implements i/o tests of the PyNexml
library using the unittest harness.
"""

import os
import unittest
from pynexml import nexml
from pynexml.tests import test_conf
from pynexml import get_logger
_LOG = get_logger("tests.test_io")

class TreeIOTest(unittest.TestCase):
    """
    Base class to provide functionality used by deriving classes
    implmenting other i/o tests.
    """

    def setUp(self):
        self.configure()

    def configure(self):
        """
        Configures the test suite. Deriving classes can extend this.
        """
        pass

    def get_writer(self, writer_class):
        """
        Returns a TreeWriter of the given class.
        """
        return writer_class()

    def get_reader(self, reader_class):
        """
        Returns a TreeReader of the given class.
        """
        return reader_class(tree_factory=self.tree_factory)

class RoundTripper(TreeIOTest):
    """
    Round-trip Tests---encapsulates testing of reading and writing of trees from and back
    to format `A` after writing and reading to/from format `B'.
    """

#     def configure(self):
#         """
#         Deriving classes must override this to set customize the test appropriately.
#         Initializes configuration of the round-trip: Given
#         `a_reader_class` and `a_writer_class` (TreeReader and
#         TreeWriter classes implementing handling of `A` format
#         documents) and `b_reader_class` and `b_writer_class`
#         (TreeReader and TreeWriter classes implementing handling of
#         `B` format documents), this instantiates `a_reader`,
#         `a_writer`, `b_reader`, and `b_writer` from them respectively.
#         """
#         super(RoundTripper, self).configure()
#         self.a_format_title = None
#         self.b_format_title = None
#         self.a_reader = None
#         self.a_writer = None
#         self.b_reader = None
#         self.b_writer = None
#         self.testfile = None
#         self.strictness = None

    def do_round_trip(self):
        """
        Implements the full round-trip test:
        - Uses `a_reader` to read and instantiate trees from
            `testfile`, which must be in a format that can be read by
            `a_reader`.

        - Saves all these trees into the output directory using
            `b_writer`.

        - Re-instantiates all trees from this second document using
            `b_reader` (which must be able to read trees in the
            format written by `b_writer`).

        - These trees are then rewritten to a file using
          `a_writer`, which must produce documents
          parseable by `a_reader`.

        - These trees are then read again using `a_reader`.

        - For each tree thus read (T1):

            - `b_writer` is used to write it to a
              single-tree tree document in the output directory

            - It then uses `b_reader` to re-read that tree.

            - This tree, T2, is then compared to the original, T1,
              either strictly (full equivalence, including id's) or
              not (topological and branch length equivalence). Strict
              comparisons will only work for formats that maintain
              distinct id's for nodes and edges, such as NeXML, but
              not for other formats, such as NEXUS or Newick.
        """
        _LOG.debug('')
        _LOG.debug('')
        _LOG.debug('========= ROUND-TRIP =========')
        _LOG.debug('    From: "%s"\n      To: "%s"\n Back To: "%s"'
              % (self.a_format_title, self.b_format_title, self.a_format_title))
        _LOG.debug('==============================')
        _LOG.debug('')
        _LOG.debug('Exporting: "%s" to "%s"' % (self.a_format_title, self.b_format_title))
        trees = self.a_reader.get_trees(test_conf.testsrc(self.testfile))
        b_target = test_conf.testdest(self.testfile + self.ext_b)
        self.b_writer.store_tree_document(trees, b_target)

        _LOG.debug('Rereading: "%s" to "%s"' % (self.b_format_title, self.a_format_title))
        trees = self.b_reader.get_trees(b_target)
        ba_target = test_conf.testdest(b_target + self.ext_a)
        self.a_writer.store_tree_document(trees, ba_target)

        trees = self.a_reader.get_trees(ba_target)
        count = 0
        for tree in trees:
            count = count + 1
            _LOG.debug('')
            _LOG.debug('COMPARING: TREE %d to TREE %d' % (count, count))
            bab_target = test_conf.testdest(ba_target + self.ext_b)
            self.b_writer.store_tree_document(tree, bab_target)
            reread_trees = self.b_reader.get_trees(bab_target)
            self.failUnlessEqual(len(reread_trees),1)
            self.compare_trees(tree,
                               reread_trees[0],
                               filepath1=ba_target,
                               filepath2=bab_target)
        _LOG.debug("\n=== END ====\n")

    def compare_trees(self, tree1, tree2, filepath1, filepath2):
        """
        Compares two DendroPy objects for structural and logical
        identity.
        """

        self.compare_splits(tree1, tree2, filepath1, filepath2)

        ## NODE COMPARISON ##
        if self.strictness > 1:
            self.compare_nodes_strictly(tree1, tree2, filepath1, filepath2)
            self.compare_edges_strictly(tree1, tree2, filepath1, filepath2)

    def compare_splits(self, tree1, tree2, filepath1, filepath2):
        _LOG.debug('\n---SPLITS---')
        _LOG.debug("Tree 1: %s" % os.path.basename(filepath1))
        tree1_splits = tree1.seed_node.subtree_splits(attribute="taxon")
        _LOG.debug(tree1_splits)
        _LOG.debug("Tree 2:%s" % os.path.basename(filepath2))
        tree2_splits = tree2.seed_node.subtree_splits(attribute="taxon")
        _LOG.debug(tree2_splits)


    def compare_nodes_strictly(self, tree1, tree2, filepath1, filepath2):
        """
        Compares two DendroPy Tree objects for strict similarity in
        node identity.

        - the number of nodes in tree1 and tree2 must be the same
        - every node as identified by node_id in tree1 must be in tree2
        - every node as identified by node_id in tree2 must be in tree1
        - for every pair of nodes with the same id in tree1 and tree2,
          the:
              - parent node id must be the same
              - the edge id must be the same
              - the edge length must be the same
              - the label must be the same
              - the taxon must be the same
              - the number of children must be the same
              - the id's of the children must be the same
        """

        _LOG.debug('')
        _LOG.debug('---NODES---')

        tree1_nodes = [node for node in tree1.preorder_node_iter()]
        tree2_nodes = [node for node in tree2.preorder_node_iter()]

        self.failUnlessEqual(len(tree1_nodes), len(tree2_nodes))

        tree1_node_map = {}
        for node in tree1_nodes:
            tree1_node_map[node.node_id] = node

        tree2_node_map = {}
        for node in tree2_nodes:
            tree2_node_map[node.node_id] = node

        _LOG.debug("Tree 1: %s" % os.path.basename(filepath1))
        _LOG.debug(tree1_node_map.keys())
        _LOG.debug("Tree 2: %s" % os.path.basename(filepath2))
        _LOG.debug(tree2_node_map.keys())

        for node_id in tree1_node_map.keys():
            self.failIf(node_id not in tree2_node_map.keys())

        for node_id in tree2_node_map.keys():
            self.failIf(node_id not in tree1_node_map.keys())

        for tree1_node_id in tree1_node_map.keys():
            node1 = tree1_node_map[tree1_node_id]
            node2 = tree2_node_map[tree1_node_id]

            if node1.parent_node:
                self.failUnlessEqual(node1.parent_node.node_id, node2.parent_node.node_id)
            else:
                # if one is None, so should the other be
                self.failUnlessEqual(node1.parent_node, node2.parent_node)

            if node1.edge:
                self.failUnlessEqual(node1.edge.edge_id, node2.edge.edge_id)
                if node1.edge.length is not None:
                    self.failUnlessAlmostEqual(node1.edge.length, node2.edge.length)
                else:
                    self.failUnlessEqual(node1.edge.length, node2.edge.length)
            else:
                # if one is None, so should the other be
                self.failUnlessEqual(node1.edge, node2.edge)

            node1_children = node1.children()
            node2_children = node2.children()
            self.failUnlessEqual(node1.label, node2.label)
            self.failUnlessEqual(node1.taxon, node2.taxon)
            self.failUnlessEqual(len(node1_children),
                                 len(node2_children))

            node1_child_ids = [child_node.node_id \
                               for child_node in node1_children]
            node2_child_ids = [child_node.node_id \
                               for child_node in node2_children]
            for child_node_id in node1_child_ids:
                self.failUnless(child_node_id in node2_child_ids)
            for child_node_id in node2_child_ids:
                self.failUnless(child_node_id in node1_child_ids)


    def compare_edges_strictly(self, tree1, tree2, filepath1, filepath2):
        """
        Compares two DendroPy Tree objects for strict similarity in
        edge identity.

        - the number of edges in tree1 and tree2 must be the same
        - every edge as identified by edge_id in tree1 must be in tree2
        - every edge as identified by edge_id in tree2 must be in tree1
        - for every pair of edges with the same id in tree1 and tree2,
          the:
              - parent node id must be the same
              - child node id must be the same
              - the edge length must be the same
              - the label must be the same
              - the taxon must be the same
              - the number of children must be the same
              - the id's of the children must be the same
        """

        _LOG.debug('')
        _LOG.debug('---EDGES---')
        tree1_edges = [edge for edge in tree1.preorder_edge_iter()]
        tree2_edges = [edge for edge in tree2.preorder_edge_iter()]

        self.failUnlessEqual(len(tree1_edges), len(tree2_edges))

        tree1_edge_map = {}
        for edge in tree1_edges:
            tree1_edge_map[edge.edge_id] = edge

        tree2_edge_map = {}
        for edge in tree2_edges:
            tree2_edge_map[edge.edge_id] = edge

        _LOG.debug("Tree 1: %s" % os.path.basename(filepath1))
        _LOG.debug(tree1_edge_map.keys())
        _LOG.debug("Tree 2: %s" % os.path.basename(filepath2))
        _LOG.debug(tree2_edge_map.keys())

        for edge_id in tree1_edge_map.keys():
            self.failIf(edge_id not in tree2_edge_map.keys())

        for edge_id in tree2_edge_map.keys():
            self.failIf(edge_id not in tree1_edge_map.keys())

        for tree1_edge_id in tree1_edge_map.keys():
            edge1 = tree1_edge_map[tree1_edge_id]
            edge2 = tree2_edge_map[tree1_edge_id]

            if edge1.tail_node:
                self.failUnlessEqual(edge1.tail_node.node_id, edge2.tail_node.node_id)
            else:
                # if one is None, so should the other be
                self.failUnlessEqual(edge1.tail_node, edge2.tail_node)

            if edge1.head_node:
                self.failUnlessEqual(edge1.head_node.node_id, edge2.head_node.node_id)
            else:
                # if one is None, so should the other be
                self.failUnlessEqual(edge1.head_node, edge2.head_node)

            if edge1.length is not None:
                self.failUnlessAlmostEqual(edge1.length, edge2.length)
            else:
                # if one is None, so should the other be
                self.failUnlessEqual(edge1.length, edge2.length)

class NexmlNexmlRoundTripper(RoundTripper):
    """
    Round-trip test of NeXML-to-NexML.
    """

    def test(self):
        """
        Set up and run round-trip.
        """
        super(NexmlNexmlRoundTripper, self).configure()
        self.a_format_title = "NeXML"
        self.b_format_title = "NeXML"
        self.a_reader = self.get_reader(nexml.NexmlReader)
        self.a_writer = self.get_writer(nexml.NexmlWriter)
        self.ext_a = '.xml'
        self.b_reader = self.a_reader
        self.b_writer = self.a_writer
        self.ext_b = '.xml'
        self.testfile = 'nexml_trees.xml'
        self.strictness = 9
        self.do_round_trip()

# class NewickNewickRoundTripper(RoundTripper):
#     """
#     Round-trip test of Newick-to-Newick.
#     """

#     def test(self):
#         """
#         Set up and run round-trip.
#         """
#         super(NewickNewickRoundTripper, self).configure()
#         self.a_format_title = "Newick"
#         self.b_format_title = "Newick"
#         self.a_reader = self.get_reader(newick.NewickReader)
#         self.a_writer = self.get_writer(newick.NewickWriter)
#         self.ext_a = '.tre'
#         self.b_reader = self.a_reader
#         self.b_writer = self.a_writer
#         self.ext_b = '.tre'
#         self.testfile = 'newick_trees.tre'
#         self.strictness = 0
#         self.do_round_trip()

# class NexmlNewickRoundTripper(RoundTripper):
#     """
#     Round-trip test of NeXML-to-NexML.
#     """

#     def test(self):
#         """
#         Set up and run round-trip.
#         """
#         super(NexmlNewickRoundTripper, self).configure()
#         self.a_format_title = "NeXML"
#         self.b_format_title = "Newick"
#         self.a_reader = self.get_reader(nexml.NexmlReader)
#         self.a_writer = self.get_writer(nexml.NexmlWriter)
#         self.ext_a = '.xml'
#         self.b_reader = self.get_reader(newick.NewickReader)
#         self.b_writer = self.get_writer(newick.NewickWriter)
#         self.ext_b = '.tre'
#         self.testfile = 'nexml_trees.xml'
#         self.strictness = 0
#         self.do_round_trip()

# class NewickNexmlRoundTripper(RoundTripper):
#     """
#     Round-trip test of Newick-to-Newick.
#     """

#     def test(self):
#         """
#         Set up and run round-trip.
#         """
#         super(NewickNexmlRoundTripper, self).configure()
#         self.a_format_title = "Newick"
#         self.b_format_title = "NeXML"
#         self.a_reader = self.get_reader(newick.NewickReader)
#         self.a_writer = self.get_writer(newick.NewickWriter)
#         self.ext_a = '.tre'
#         self.b_reader = self.get_reader(nexml.NexmlReader)
#         self.b_writer = self.get_writer(nexml.NexmlWriter)
#         self.ext_b = '.xml'
#         self.testfile = 'newick_trees.tre'
#         self.strictness = 0
#         self.do_round_trip()

def additional_tests():
    "returns all tests in this file as suite"
    pass
#     return unittest.TestLoader().loadTestsFromTestCase(TreeIOTest)


# pylint: disable-msg=C0103
def getTestSuite():
    """Alias to the additional_tests().  This is unittest-style.
    `additional_tests` is used by setuptools.
    """
    return additional_tests()

if __name__ == "__main__":
    unittest.main()
