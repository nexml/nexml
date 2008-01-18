#! /usr/bin/env python

############################################################################
##  characters.py
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
This module handles the core definitions of character data types, as well as 
specializations to handle nucleotide, etc. character types.
"""

#                [    [0],                // A
#                     [1],                // C
#                     [2],                // G
#                     [3],                // T
#                     [-1, 0, 1, 2, 3],   // ?
#                     [0, 1, 2, 3],       // N or X
#                     [0, 1],             // M
#                     [0, 2],             // R
#                     [0, 3],             // W
#                     [1, 2],             // S
#                     [1, 3],             // Y
#                     [2, 3],             // K
#                     [0, 1, 2],          // V
#                     [0, 1, 3],          // H
#                     [0, 2, 3],          // D
#                     [1, 2, 3],          // B
#                 ]        

from pynexml import base
from pynexml import taxa


class DiscreteCharacterSymbol(object):
    
    def __init__(self, symbol_id, symbol_string, symbol_mappings=None):
        self.set(symbol_id=symbol_id,
                 symbol_string=symbol_string,
                 symbol_mappings=symbol_mappings)
                 
    def set(self, symbol_id, symbol_string, symbol_mappings=None):
        self.symbol_id = symbol_id
        self.symbol_string = symbol_string
        if symbol_mappings is not None:
            self.symbol_mappings = symbol_mappings
        else:
            self.symbol_mappings = [self]    

class DiscreteCharacterType(list):
    """
    Discrete character mapper.
    """
    
    def __init__(self, *args, **kwargs):
        list.__init__(self, *args)
        
    def symbols_by_id(self, ids):
        return [sym for sym in self if sym.symbol_id in ids]
        
    def symbols_by_string(self, str):
        return [sym for sym in self if sym.symbol_string in str]        
    
class DnaCharacterType(DiscreteCharacterType):
    
    def __init__(self):
        DiscreteCharacterType.__init__(self)
        self.append(DiscreteCharacterSymbol("A", "A"))
        self.append(DiscreteCharacterSymbol("C", "C"))     
        self.append(DiscreteCharacterSymbol("G", "G"))
        self.append(DiscreteCharacterSymbol("T", "T"))
        self.append(DiscreteCharacterSymbol("GAP", "-")) 
        self.append(DiscreteCharacterSymbol("MISSING", "?"))
        self.append(DiscreteCharacterSymbol("N", "N",
                                            self.symbols_by_string(['A', 'C', 'G', 'T'])))
        self.append(DiscreteCharacterSymbol("M", "M", 
                                            self.symbols_by_string(['A', 'C'])))                                            
        self.append(DiscreteCharacterSymbol("R", "R", 
                                            self.symbols_by_string(['A', 'G'])))
        self.append(DiscreteCharacterSymbol("W", "W",
                                            self.symbols_by_string(['A', 'T'])))
        self.append(DiscreteCharacterSymbol("S", "S", 
                                            self.symbols_by_string(['C', 'G'])))                                            
        self.append(DiscreteCharacterSymbol("Y", "Y", 
                                            self.symbols_by_string(['C', 'T'])))   
        self.append(DiscreteCharacterSymbol("K", "K", 
                                            self.symbols_by_string(['G', 'T'])))                                            
        self.append(DiscreteCharacterSymbol("V", "V", 
                                            self.symbols_by_string(['A', 'C', 'G'])))
        self.append(DiscreteCharacterSymbol("H", "H",
                                            self.symbols_by_string(['A', 'C', 'T'])))
        self.append(DiscreteCharacterSymbol("D", "D", 
                                            self.symbols_by_string(['A', 'G', 'T'])))                                            
        self.append(DiscreteCharacterSymbol("B", "B", 
                                            self.symbols_by_string(['C', 'G', 'T'])))                                               


class CharacterBlock(dict, taxa.TaxaLinked):
    """
    Character block manager.
    """

    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label`, and `taxa_block` and `chartype`.
        """
        dict.__init__(self, *args)
        taxa.TaxaLinked.__init__(self, *args, **kwargs)


if __name__ == "__main__":
    dna = DnaCharacterType()