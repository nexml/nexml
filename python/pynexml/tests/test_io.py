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
log_message = _LOG.info

class NexmlTest(unittest.TestCase):
    """
    Base class to provide functionality used by deriving classes
    implmenting other i/o tests.
    """
    
    def test_all(self):
		"""
		Runs through test suite.
		"""
		log_message("Starting nexml parsing tests ...")
		test_filenames = ['comprehensive.xml']
		for test_filename in test_filenames:
			self.read_file(test_filename)
        
    def read_file(self, filename):
    	source_path = test_conf.test_source_path(filename)
    	log_message("- Reading '%s'" % source_path)
    	nexml_reader = nexml.NexmlReader()
    	dataset = nexml_reader.get_dataset(source_path)
    	log_message("  File read with: %d taxa blocks; %d character blocks; %d tree blocks" 
    		% (len(dataset.taxa_blocks), 
    		   len(dataset.char_blocks), 
    		   len(dataset.tree_blocks)))
    		   
    	log_message("-- Taxa Blocks")
    	for block_idx, taxa_block in enumerate(dataset.taxa_blocks):
    		log_message("--- Taxa Block %d of %d: [%s] '%s'" 
    			% (block_idx+1, 
    			   len(dataset.taxa_blocks), 
    			   taxa_block.elem_id,
    			   taxa_block.label))
    		for tax_idx, taxon in enumerate(taxa_block):
    			log_message("---- Taxon %d: [%s] '%s'" 
    				% (tax_idx+1, 
    				   taxon.elem_id, 
    				   taxon.label)) 
    		    		   
    	log_message("-- Characters Blocks")
    	for block_idx, char_block in enumerate(dataset.char_blocks):
    		log_message("--- Characters Block %d of %d: [%s] '%s'" 
    			% (block_idx+1, 
    			   len(dataset.char_blocks), 
    			   char_block.elem_id,
    			   char_block.label))
    		for taxon in char_block:
    			print taxon, char_block[taxon]
    			
    	log_message("-- Trees Blocks")
    	for block_idx, tree_block in enumerate(dataset.tree_blocks):
    		log_message("--- Trees Block %d of %d: [%s] '%s'" 
    			% (block_idx+1, 
    			   len(dataset.tree_blocks), 
    			   tree_block.elem_id,
    			   tree_block.label))
    		for tree in tree_block:
    			log_message(tree.seed_node.compose_newick())
     			



def additional_tests():
    "returns all tests in this file as suite"
    return unittest.TestLoader().loadTestsFromTestCase(NexmlTest)


# pylint: disable-msg=C0103
def getTestSuite():
    """Alias to the additional_tests().  This is unittest-style.
    `additional_tests` is used by setuptools.
    """
    return additional_tests()

if __name__ == "__main__":
    unittest.main()
