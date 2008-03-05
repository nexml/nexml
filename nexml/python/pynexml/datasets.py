#! /usr/bin/env python

############################################################################
##  datasets.py
##
##  Part of the PyNexml phylogenetic data parsing library.
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
Handles the full collection of phylogenetic data: character matrices,
trees, models etc.
"""

import os
import StringIO

from pynexml import taxa
from pynexml import characters
from pynexml import trees

class Dataset(object):
    """
    Top-level data structure.
    """

    def __init__(self, taxa_blocks=None, char_blocks=None, trees_blocks=None):
        """
        Instantiates collections of taxa, blocks, trees, and models.
        """
        if taxa_blocks is None:
            self.taxa_blocks = []
        else:
            self.taxa_blocks = taxa_blocks
        if char_blocks is None:
            self.char_blocks = []
        else:
            self.char_blocks = char_blocks
        if trees_blocks is None:
            self.trees_blocks = []
        else:
            self.trees_blocks = trees_blocks

    def normalize_taxa_blocks(self):
        """
        Builds up list of taxon blocks by collecting taxon blocks
        referenced in self's char_blocks and trees_blocks.
        """
        self.taxa_blocks = []
        for matrix in self.char_blocks:
            self.normalize_taxa_linked(matrix)
        for trees_block in self.trees_blocks:
            self.normalize_taxa_linked(trees_block)

    def normalize_taxa_linked(self, taxa_linked):
        """
        `taxa_linked` is a trees_block or char_block or some other
        object with a `taxa_block` attribute that points to a
        TaxaBlock object. This searches the current collection of
        taxon blocks to see if the referred taxon block exists, as
        determined by the elem_id. If it does, then the referer's
        taxa_block is set to point to it. If not, the taxa_block is
        added to the collection.
        """
        for taxa_block in self.taxa_blocks:
            if taxa_block.elem_id == taxa_linked.taxa_block.elem_id:
                taxa_linked.taxa_block = taxa_block
                return
        self.taxa_blocks.append(taxa_linked.taxa_block)

    def find_taxa_block(self, elem_id=None, label=None):
        """
        Returns taxon block based on element id or label, whichever is
        given and found first.
        """
        for taxa_block in self.taxa_blocks:
            if (elem_id and taxa_block.elem_id == elem_id) \
               or (label and taxa_block.label == label):
                return taxa_block
        return None

    def add_taxa_block(self, elem_id=None, label=None, taxa_block=None, taxa_block_factory=None):
        """
        Adds (and returns) new taxa block object, creating one using
        the default factory if not given.
        """
        if taxa_block is None:
            if taxa_block_factory is None:
                taxa_block = taxa.TaxaBlock(elem_id=elem_id, label=label)
            else:
                taxa_block = taxa_block_factory(elem_id=elem_id, label=label)
        self.taxa_blocks.append(taxa_block)
        return taxa_block

    def add_taxa_linked_block(self,
                              elem_id=None,
                              label=None,
                              taxa_block=None,
                              linked_block=None,
                              linked_block_factory=None,
                              normalize_taxa_blocks=True):
        """
        Adds (and returns) a tree block object, creating one using the
        default factory if not given.
        """
        if linked_block is None:
            if linked_block_factory is not None:
                linked_block = linked_block_factory(elem_id=elem_id, label=label)
                if taxa_block is not None:
                    linked_block.taxa_block = taxa_block
                else:
                    self.taxa_blocks.append(linked_block.taxa_block)
            else:
                raise Exception("Neither object nor method to create object given.")
        else:
            if elem_id is not None:
                linked_block.elem_id = elem_id
            if label is not None:
                linked_block.label = label
        if taxa_block is not None:
            linked_block.taxa_block = taxa_block
        if normalize_taxa_blocks:
            self.normalize_taxa_linked(linked_block)
        return linked_block

    def add_trees_block(self,
                       elem_id=None,
                       label=None,
                       taxa_block=None,
                       trees_block=None,
                       trees_block_factory=None,
                       normalize_taxa_blocks=True):
        """
        Adds (and returns) a tree block object, creating one using the
        default factory if not given.
        """
        if trees_block is None and trees_block_factory is None:
            trees_block_factory = trees.TreesBlock
        trees_block = self.add_taxa_linked_block(elem_id=elem_id,
                                                label=label,
                                                taxa_block=taxa_block,
                                                linked_block=trees_block,
                                                linked_block_factory=trees_block_factory,
                                                normalize_taxa_blocks=normalize_taxa_blocks)
        self.trees_blocks.append(trees_block)
        return trees_block

    def add_char_block(self,
                       elem_id=None,
                       label=None,
                       taxa_block=None,
                       char_block=None,
                       char_block_factory=None,
                       normalize_taxa_blocks=True):
        """
        Adds (and returns) a char block object, creating one using the
        default factory if not given if not given.
        """
        if char_block is None and char_block_factory is None:
            char_block_factory = characters.CharBlock        
        char_block = self.add_taxa_linked_block(elem_id=elem_id,
                                                label=label,
                                                taxa_block=taxa_block,
                                                linked_block=char_block,
                                                linked_block_factory=char_block_factory,
                                                normalize_taxa_blocks=normalize_taxa_blocks)
        self.char_blocks.append(char_block)
        return char_block

class Reader(object):
    """
    Interface for instantiation of Dataset objects from various
    formats, to be implemented by derived classes.
    """
    
    def get_file_handle(filepath=None, fileobj=None, text=None):
    	"""
    	Opens and returns a file descriptor/handle based on the 
    	given parameters.
    	"""
        if filepath:
            filepath = os.path.expandvars(os.path.expanduser(filepath))
            return open(filepath, 'r')            
        elif fileobj:
            return fileobj
        elif text:
            return StringIO.StringIO(text)
        else:
            raise Exception("Source of dataset must be specified")    	
    get_file_handle = staticmethod(get_file_handle)

    def __init__(self):
        """
        Initializes.
        """
        # 0 = ignore all errors; 1 = print warning; 2 = raise exception
        self.error_level=0
        self.taxa_block_factory = taxa.TaxaBlock
        self.taxon_factory = taxa.Taxon
        #self.char_block_factory = characters.CharBlock
        self.trees_block_factory = trees.TreesBlock
        self.tree_factory = trees.Tree
        self.edge_factory = trees.Edge
        self.node_factory = trees.Node

    def get_dataset(self, filepath=None, fileobj=None, text=None, dataset=None):
        """
        Instantiates and returns a Dataset object from a filepath, a
        file descriptor or direct text source respectively.
        """
        filedesc = self.get_file_handle(filepath=filepath, fileobj=fileobj, text=text)
        return self.read_dataset(filedesc, dataset)

    def get_matrices(self, filepath=None, fileobj=None, text=None, char_block_factory=None):
        """
        Instantiates and returns a list of CharMatrix objects from a filepath, a
        file descriptor or direct text source respectively.
        """
        dataset = Dataset()        
        if char_block_factory is not None:
            dataset.char_block_factory = char_block_factory
        dataset = self.get_dataset(filepath=filepath, fileobj=fileobj, text=text, dataset=dataset)
        return dataset.char_blocks

    def get_taxa(self, filepath=None, fileobj=None, text=None, taxa_block_factory=None):
        """
        Instantiates and returns a list of TaxaBlock objects from a
        filepath, a file descriptor or direct text source
        respectively.
        """
        dataset = Dataset()        
        if taxa_block_factory is not None:
            dataset.taxa_block_factory = taxa_block_factory
        dataset = self.get_dataset(filepath=filepath, fileobj=fileobj, text=text, dataset=dataset)
        return dataset.taxa_blocks

    def get_trees(self, filepath=None, fileobj=None, text=None, trees_block_factory=None, tree_factory=None):
        """
        Instantiates and returns a list of Tree objects from a
        filepath, a file descriptor or direct text source
        respectively.
        """
        dataset = Dataset()        
        if trees_block_factory is not None:
            dataset.trees_block_factory = trees_block_factory
        if tree_factory is not None:
            dataset.tree_factory = tree_factory
        dataset = self.get_dataset(filepath=filepath, fileobj=fileobj, text=text, dataset=dataset)
        return dataset.trees_blocks
           
    ### Following methods must be implemented by deriving classes  ###

    def read_dataset(self, fileobj, dataset=None):
        """
        Implementing classes should instantiate and return a Dataset
        object based on contents read from the file descriptor object
        `fileobj`.
        """
        raise NotImplementedError

class Writer(object):
    """
    Interface for composing and writing a representation of a DataSet
    object in various formats, to be implemented by derived classes.
    """

    def store_dataset(self, dataset, destination, filemode='w'):
        """
        Writes a DataSet object to the specified destination as a
        formatted fully-formed document (e.g., an entire file or a
        NEXML nexml element).
        """
        if isinstance(destination, str):
            filepath = os.path.expandvars(os.path.expanduser(destination))
            destf = open(filepath, filemode)
        else:
            destf = destination
        self.write_dataset(dataset, destf)

    def compose_dataset(self, dataset):
        """
        Returns a string representation of a DataSet as a fully-formed
        and formatted dataset document.
        """
        dataset_text = StringIO.StringIO()
        self.store_dataset(dataset, dataset_text)
        return dataset_text.getvalue()
        
    ### Following methods must be implemented by deriving classes  ###

    def write_dataset(self, dataset, dest):
        """
        Writes a DataSet object to a full document-level
        representation of the format being implemented by the deriving
        class. `dest` is an output stream that support 'write'.
        """
        raise NotImplementedError
