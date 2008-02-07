#! /usr/bin/env python

############################################################################
##  taxa.py
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
This module provides classes and methods for managing taxa.
"""
import sys
from pynexml import base

class TaxonLinked(base.IdTagged):
    """
    Provides infrastructure for maintaining link/reference to a Taxon
    object.
    """
    
    def __init__(self, elem_id=None, label=None, taxon=None):
        """
        Initializes by calling base class.
        """
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)
        self.__taxon = taxon

    def _get_taxon(self):
        """
        Returns taxon associated with this object.
        """
        return self.__taxon

    def _set_taxon(self, taxon):
        """
        If `taxon` is a Taxon object, then it is assigned directly. If
        `taxon` is a string, then it is assumed to be a label, and a
        new taxon object is constructed based on it and assigned (the
        new taxon object will have the string given by `taxon` as id
        and label, though it will be modified as neccessary to make it
        xs:NCName compliant for the id.
        """
        if taxon is None:
            self.__taxon = None
        elif isinstance(taxon, Taxon):
            self.__taxon = taxon
        else:
            taxon_obj = Taxon()
            taxon_obj.label = taxon
            taxon_obj.elem_id = taxon
            self.__taxon = taxon_obj

    taxon = property(_get_taxon, _set_taxon)

class TaxaLinked(base.IdTagged):
    """
    Provides infrastructure for the maintenance of references to taxa
    blocks.
    """

    def __init__(self, elem_id=None, label=None, taxa_block=None):
        """
        Initializes by calling base class.
        """
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)
        self.__taxa_block = taxa_block

    def _get_taxa_block(self):
        """
        Returns taxon block associated with this object. If none is
        given, then it builds one.
        """
        if self.__taxa_block is None:
            self.__taxa_block= TaxaBlock()
        return self.__taxa_block

    def _set_taxa_block(self, taxa_block):
        """
        Sets the taxon block for this object.
        """
        self.__taxa_block = taxa_block

    taxa_block = property(_get_taxa_block, _set_taxa_block)
            
class TaxaBlock(list, base.IdTagged):
    """
    Taxon manager.
    """

    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id` and `label`.
        """
        list.__init__(self, *args)
        base.IdTagged.__init__(self, *args, **kwargs)

    def __str__(self):
        """
        String representation of self.
        """
        header = []
        if self.elem_id:
            header.append("%s" % str(self.elem_id))
        if self.label:
            header.append("(\"%s\")" % self.label)
        taxlist = []
        for taxon in self:
            taxlist.append(str(taxon))
        return ' '.join(header) + ' : [' + ', '.join(taxlist) + ']' 

    def find_taxon(self, elem_id=None, label=None, update=False):
        """
        Retrieves taxon object with given id OR label (if both are
        given, the first match found is returned). If taxon does not
        exist and update is False, an exception is raised. If taxon
        does not exist and update is True, then a new taxon is
        created, added, and returned.
        """
        if not elem_id and not label:
            raise Exception("Need to specify Element ID or Label.")
        for taxon in self:
            if taxon.elem_id == elem_id \
               or taxon.label == label:
                return taxon
        if not update:
            raise Exception("Taxon not found: %s/%s" % (elem_id, label))
        else:
            taxon = Taxon(elem_id=elem_id, label=label)
            self.append(taxon)
            return taxon
        
    def taxon_bitmask(self, taxon):
        """
        Returns unique bitmask of given taxon. Will raise index error if
        taxon does not exist.
        """
        try:
            return pow(2, self.index(taxon))
        except ValueError:
            raise ValueError("Taxon with ID '%s' and label '%s' not found" 
                             % (str(taxon.elem_id), str(taxon.label)))
            
            
class Taxon(base.IdTagged):
    """
    A taxon associated with a sequence or a node on a tree.
    """
    
    def cmp(taxon1, taxon2):
        """
        Compares taxon1 and taxon2 based on label.
        """
        return cmp(str(taxon1.label), str(taxon2.label))

    def __init__(self, elem_id=None, label=None): 
        """
        Initializes by calling base class.
        """
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)

    def __str__(self):
        """
        String representation of self = taxon name.
        """
        return str(self.label)
