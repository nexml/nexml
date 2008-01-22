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

class DiscreteCharacterState(base.IdTagged):
    """
    A character state definition, which can either be a fundamental state or
    a mapping to a set of other character states (for polymorphic or ambiguous
    characters).
    """
    
    # multistate enums
    SINGLE_STATE = 0
    AMBIGUOUS_STATE = 1
    POLYMORPHIC_STATE = 2
    
    def __init__(self, 
                 elem_id=None, 
                 label=None,
                 symbol=None, 
                 token=None, 
                 multistate=DiscreteCharacterState.SINGLE_STATE, 
                 member_states=None):
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)
        self.symbol = symbol
        self.token = token
        self.multistate = multistate
        self.member_states = member_states
        
    def fundamental_states(self):
        """
        Returns value of self in terms of a set of fundamental states (i.e.,
        set of single states) that correspond to this state.
        """
        if self.member_states is None:
            return set(self)
        else:
            states = set()
            for state in member_states:
                states.update(state.fundamental_states())
            return states
                 
    def fundamental_ids(self):
        """
        Returns set of id's of all fundamental states to which this state maps.
        """
        return set([state.elem_id for state in self.fundamental_states()])
        
    def fundamental_symbols(self):
        """
        Returns set of symbols of all fundamental states to which this state maps.
        """
        return set([state.symbol for state in self.fundamental_states() if state.symbol])  
        
    def fundamental_tokens(self):
        """
        Returns set of tokens of all fundamental states to which this state maps.
        """
        return set([state.token for state in self.fundamental_states() if state.token])        
        
class DiscreteCharacterStateList(list):
    """
    A list of states available for a particular character type/format.
    """
    
    def __init__(self, *args, **kwargs):
        list.__init__(self, *args)    
    
    def state_by_symbol(self, symbol):
        """
        Returns a DiscreteCharacterState object corresponding to the given symbol.
        """
        for state in self:
            if state.symbol == symbol:
                return state
                
    
class DnaCharacterSymbols(DiscreteCharacterSymbols):

    def __init__(self):
        DiscreteCharacterSymbols.__init__(self)
        self.append(DiscreteCharacterSymbol("A", "A"))
        self.append(DiscreteCharacterSymbol("C", "C"))     
        self.append(DiscreteCharacterSymbol("G", "G"))
        self.append(DiscreteCharacterSymbol("T", "T"))
        self.append(DiscreteCharacterSymbol("GAP", "-")) 
        self.append(DiscreteCharacterSymbol("MISSING", "?",
                                            self.symbols_by_string(['A', 'C', 'G', 'T', '-'])))
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


class DiscreteCharacterStates(object):

    def __init__(self, symbols=None):
        self.symbol_list = symbols

    

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
    dna = DnaCharacterSymbols()
    for s in dna:
        print repr(s)
    print
    print
    input = "\n"
    while input:
        input = raw_input("Enter symbol(s): ")
        if input:
            input = input.upper()
            print repr(dna.string_to_symbol(input))