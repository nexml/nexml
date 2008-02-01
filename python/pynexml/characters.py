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

class StateAlphabetElement(base.IdTagged):
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
                 multistate=SINGLE_STATE, 
                 member_states=None):
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)
        self.symbol = symbol
        self.token = token
        self.multistate = multistate
        self.member_states = member_states
        
    def __str__(self):
        return str(self.symbol)
        
    def __repr__(self):
        return str([self.elem_id, 
                    self.symbol, 
                    '[' + (', '.join(self._get_fundamental_symbols())) + ']'])
        
    def _get_fundamental_states(self):
        """
        Returns value of self in terms of a set of _get_fundamental states (i.e.,
        set of single states) that correspond to this state.
        """
        if self.member_states is None:
            return set([self])
        else:
            states = set()
            for state in self.member_states:
                states.update(state._get_fundamental_states())
            return states
            
    fundamental_states = property(_get_fundamental_states)          
                 
    def _get_fundamental_ids(self):
        """
        Returns set of id's of all _get_fundamental states to which this state maps.
        """
        return set([state.elem_id for state in self._get_fundamental_states()])
        
    fundamental_ids = property(_get_fundamental_ids)          
        
    def _get_fundamental_symbols(self):
        """
        Returns set of symbols of all _get_fundamental states to which this state maps.
        """
        return set([state.symbol for state in self._get_fundamental_states() if state.symbol]) 
        
    fundamental_symbols = property(_get_fundamental_symbols)
        
    def _get_fundamental_tokens(self):
        """
        Returns set of tokens of all _get_fundamental states to which this state maps.
        """
        return set([state.token for state in self._get_fundamental_states() if state.token])        
        
    fundamental_tokens = property(_get_fundamental_tokens)   
             
class StateAlphabetSet(base.IdTagged, set):
    """
    A set of states available for a particular character type/format.
    """
    
    def __init__(self, elem_id=None, label=None):
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)  
        set.__init__(self)
        
    def get_state(self, attr_name, value):
        """
        Returns state in self in which attr_name equals value.
        """
#         attr_name = None
#         value = None
#         if elem_id is not None:
#             attr_name = 'elem_id'
#             value = elem_id
#         elif symbol is not None:
#             attr_name = 'symbol'
#             value = symbol
#         elif token is not None:
#             attr_name = 'token'
#             value = token
#         else:
#             raise Exception("Must specify id, symbol or token")
        for state in self:
            if getattr(state, attr_name) == value:
                return state
        raise Exception("State with %s value of '%s' not defined" % (attr_name, str(value)))
    
    def get_states(self, elem_ids=None, symbols=None, tokens=None):
        """
        Returns list of states with ids/symbols/tokens equal to values
        given in a list of ids/symbols/tokens (exact matches, one-to-one
        correspondence between state and attribute value in list).
        """
        if elem_ids is not None:
            attr_name = 'elem_id'
            values = elem_ids
        elif symbols is not None:
            attr_name = 'symbol'
            values = symbols
        elif tokens is not None:
            attr_name = 'token'
            values = tokens
        else:
            raise Exception("Must specify ids, symbols or tokens")    
        states = []
        for value in values:
            states.append(self.get_state(attr_name=attr_name, value=value))
        return states
    
    def match_state(self, elem_ids=None, symbols=None, tokens=None):
        """
        Returns SINGLE state that has ids/symbols/tokens as member states.
        """
        if elem_ids is not None:
            attr_name = 'fundamental_ids'
            values = elem_ids
        elif symbols is not None:
            attr_name = 'fundamental_symbols'
            values = symbols
        elif tokens is not None:
            attr_name = 'fundamental_tokens'
            values = tokens
        else:
            raise Exception("Must specify ids, symbols or tokens")  
        if isinstance(values, list):
            values = set(values)
        elif isinstance(values, str):
            values = set([ch for ch in values])

        for state in self:
            if getattr(state, attr_name) == values:
                return state
        return None
    
    def id_map(self):
        """
        Returns dictionary of element id's to state objects.
        """
        map = {}
        for state in self:
            map[state.elem_id] = state
        return map
           
class DnaStateAlphabetSet(StateAlphabetSet):

    def __init__(self, elem_id=None, label=None):
        StateAlphabetSet.__init__(self, elem_id=elem_id, label=label)
        self.append(StateAlphabetElement(symbol="A"))
        self.append(StateAlphabetElement(symbol="C"))     
        self.append(StateAlphabetElement(symbol="G"))
        self.append(StateAlphabetElement(symbol="T"))
        self.append(StateAlphabetElement(symbol="-")) 
        self.append(StateAlphabetElement(symbol="?",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'G', 'T', '-'])))
        self.append(StateAlphabetElement(symbol="N",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'G', 'T'])))
        self.append(StateAlphabetElement(symbol="M", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C'])))                                            
        self.append(StateAlphabetElement(symbol="R", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'G'])))
        self.append(StateAlphabetElement(symbol="W",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'T'])))
        self.append(StateAlphabetElement(symbol="S", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['C', 'G'])))                                            
        self.append(StateAlphabetElement(symbol="Y", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['C', 'T'])))   
        self.append(StateAlphabetElement(symbol="K", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['G', 'T'])))                                            
        self.append(StateAlphabetElement(symbol="V", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'G'])))
        self.append(StateAlphabetElement(symbol="H",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'T'])))
        self.append(StateAlphabetElement(symbol="D", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'G', 'T'])))                                            
        self.append(StateAlphabetElement(symbol="B", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['C', 'G', 'T'])))

class RnaStateAlphabetSet(StateAlphabetSet):

    def __init__(self, elem_id=None, label=None):
        StateAlphabetSet.__init__(self, elem_id=elem_id, label=label)
        self.append(StateAlphabetElement(symbol="A"))
        self.append(StateAlphabetElement(symbol="C"))     
        self.append(StateAlphabetElement(symbol="G"))
        self.append(StateAlphabetElement(symbol="U"))
        self.append(StateAlphabetElement(symbol="-")) 
        self.append(StateAlphabetElement(symbol="?",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'G', 'U', '-'])))
        self.append(StateAlphabetElement(symbol="N",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'G', 'U'])))
        self.append(StateAlphabetElement(symbol="M", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C'])))                                            
        self.append(StateAlphabetElement(symbol="R", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'G'])))
        self.append(StateAlphabetElement(symbol="W",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'U'])))
        self.append(StateAlphabetElement(symbol="S", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['C', 'G'])))                                            
        self.append(StateAlphabetElement(symbol="Y", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['C', 'U'])))   
        self.append(StateAlphabetElement(symbol="K", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['G', 'U'])))                                            
        self.append(StateAlphabetElement(symbol="V", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'G'])))
        self.append(StateAlphabetElement(symbol="H",
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'U'])))
        self.append(StateAlphabetElement(symbol="D", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'G', 'U'])))                                            
        self.append(StateAlphabetElement(symbol="B", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['C', 'G', 'U'])))


class ProteinStateAlphabetSet(StateAlphabetSet):

    def __init__(self, elem_id=None, label=None):
        StateAlphabetSet.__init__(self, elem_id=elem_id, label=label)
        self.append(StateAlphabetElement(symbol="A"))
        self.append(StateAlphabetElement(symbol="C"))
        self.append(StateAlphabetElement(symbol="D"))
        self.append(StateAlphabetElement(symbol="E"))
        self.append(StateAlphabetElement(symbol="F"))
        self.append(StateAlphabetElement(symbol="G"))
        self.append(StateAlphabetElement(symbol="H"))
        self.append(StateAlphabetElement(symbol="I"))
        self.append(StateAlphabetElement(symbol="K"))
        self.append(StateAlphabetElement(symbol="L"))
        self.append(StateAlphabetElement(symbol="M"))
        self.append(StateAlphabetElement(symbol="N"))
        self.append(StateAlphabetElement(symbol="P"))
        self.append(StateAlphabetElement(symbol="Q"))
        self.append(StateAlphabetElement(symbol="R"))
        self.append(StateAlphabetElement(symbol="S"))
        self.append(StateAlphabetElement(symbol="T"))
        self.append(StateAlphabetElement(symbol="U"))
        self.append(StateAlphabetElement(symbol="V"))
        self.append(StateAlphabetElement(symbol="W"))
        self.append(StateAlphabetElement(symbol="Y"))
        self.append(StateAlphabetElement(symbol="-"))        
        self.append(StateAlphabetElement(symbol="B", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['D', 'N'])))     
        self.append(StateAlphabetElement(symbol="Z", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['E', 'Q'])))
        self.append(StateAlphabetElement(symbol="X", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'D', 'E',
                                           'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S',
                                           'T', 'U', 'V', 'W', 'Y'])))   
        self.append(StateAlphabetElement(symbol="?", 
                                           multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                           member_states=self.get_states(symbols=['A', 'C', 'D', 'E',
                                           'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S',
                                           'T', 'U', 'V', 'W', 'Y', '-'])))                                              

class BinaryStateAlphabetSet(StateAlphabetSet):

    def __init__(self, elem_id=None, label=None, allow_gaps=True, allow_missing=True):
        StateAlphabetSet.__init__(self, elem_id=elem_id, label=label)
        self.append(StateAlphabetElement(symbol="0"))
        self.append(StateAlphabetElement(symbol="1"))
        if allow_gaps:
            self.append(StateAlphabetElement(symbol="-")) 
            if allow_missing:
                self.append(StateAlphabetElement(symbol="?", 
                                                   multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                                   member_states=self.get_states(symbols=['0', '1', '-'])))
        elif allow_missing:
            self.append(StateAlphabetElement(symbol="?", 
                                               multistate=StateAlphabetElement.AMBIGUOUS_STATE,
                                               member_states=self.get_states(symbols=['0', '1'])))
                        
class RestrictionSitesStateAlphabetSet(BinaryStateAlphabetSet):

    def __init__(self, elem_id=None, label=None):
        BinaryStateAlphabetSet.__init__(self, elem_id=elem_id, label=label)            

class InfiniteSitesStateAlphabetSet(BinaryStateAlphabetSet):

    def __init__(self, elem_id=None, label=None):
        BinaryStateAlphabetSet.__init__(self, elem_id=elem_id, label=label)
                
class Character(base.IdTagged):
    """                                                                                                                                                                                                                                                                                                                                                                           
    A character format or type of a particular column: i.e., maps
    a particular set of character state definitions to a column in a character matrix.
    """
  
    def __init__(self, elem_id=None,label=None, state_alphabet_set=None):
        base.IdTagged.__init__(self, elem_id=elem_id, label=label)
        self.__state_alphabet_set = None
        self.state_id_map = None
        self.state_alphabet_set = state_alphabet_set
        
    def _set_state_alphabet_set(self, value):
        self.__state_alphabet_set = value
        if self.__state_alphabet_set is not None:
            self.state_id_map = self.__state_alphabet_set.state_id_map()
        else:
            self.state_id_map = None
            
    def _get_state_alphabet_set(self):
        return self.__state_alphabet_set
        
    state_alphabet_set = property(_get_state_alphabet_set, _set_state_alphabet_set)
      

class CharacterDataCell(base.Annotated):
    """                                                                                                                                                                                                                                                                                                                                                                           
    A container for the state / state value for a particular cell in a matrix.
    """
  
    def __init__(self, value=None):
        base.Annotated.__init__(self)
        self.value = value
        
class CharacterDataVector(list, base.Annotated):
    """
    A list of character data values.
    """

    def __init__(self):
        list.__init__(self)
        base.Annotated.__init__(self)
        
    def set_cell_by_index(self, column_index, value):
        """
        Sets the value of a cell at a particular position.
        """        
        while len(self) <= column_index:
            self.append(None)
        self[column_index] = value            

class CharacterDataMatrix(dict, base.Annotated):
    """
    An annotable dictionary with Taxon objects as keys and 
    CharacterDataVectors objects as values.
    """

    def __init__(self):
        dict.__init__(self)
        base.Annotated.__init__(self)
                       
class CharactersBlock(taxa.TaxaLinked):
    """
    Character data container/manager manager.
    """

    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """
        dict.__init__(self, *args)
        taxa.TaxaLinked.__init__(self, *args, **kwargs)
        self.matrix = CharacterDataMatrix()
        self.characters = []
        
    def __getitem__(self, key):
        """
        Dictionary interface implementation for direct access to matrix.
        """
        return self.matrix[key]
        
    def __setitem__(self, key, value):
        """
        Dictionary interface implementation for direct access to matrix.
        """
        self.matrix[key] = value
                
    def __contains__(self, key):
        """
        Dictionary interface implementation for direct access to matrix.
        """
        return key in self.matrix
        
    def characters_id_map(self):
        """
        Returns dictionary of element id to corresponding
        character definition.
        """
        map = {}
        for char in self.characters:
            map[char.elem_id] = char
            
class ContinuousCharactersBlock(CharactersBlock):
    """
    Character data container/manager manager.
    """

    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """
        CharactersBlock.__init__(*args, **kwargs)

class DiscreteCharactersBlock(CharactersBlock):
    """
    Character data container/manager manager.
    """

    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """
        CharactersBlock.__init__(*args, **kwargs)
        self.state_alphabet_sets = []
        self.default_state_alphabet_set = None
                
class DnaCharactersBlock(DiscreteCharactersBlock):
    """
    DNA nucleotide data.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """
        DiscreteCharactersBlock.__init__(*args, **kwargs)
        self.default_state_alphabet_set = DnaStateAlphabetSet()
        self.state_alphabet_sets.append(self.default_state_alphabet_set)         

class RnaCharactersBlock(DiscreteCharactersBlock):
    """
    RNA nucleotide data.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """
        DiscreteCharactersBlock.__init__(*args, **kwargs)
        self.default_state_alphabet_set = RnaStateAlphabetSet()
        self.state_alphabet_sets.append(self.default_state_alphabet_set)      
        
class ProteinCharactersBlock(DiscreteCharactersBlock):
    """
    Protein / amino acid data.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """        
        DiscreteCharactersBlock.__init__(*args, **kwargs)
        self.default_state_alphabet_set = ProteinStateAlphabetSet()
        self.state_alphabet_sets.append(self.default_state_alphabet_set)               
        
class RestrictionSitesCharactersBlock(DiscreteCharactersBlock):
    """
    Restriction sites data.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label` and `taxa_block`.
        """
        DiscreteCharactersBlock.__init__(*args, **kwargs)
        self.default_state_alphabet_set = RestrictionSitesStateAlphabetSet()
        self.state_alphabet_sets.append(self.default_state_alphabet_set)                

if __name__ == "__main__":
    dna = DnaStateAlphabets()
    for s in dna:
        print repr(s)
    print
    print
    input = "\n"
    while input:
        input = raw_input("Enter symbol(s): ")
        if input:
            input = input.upper()
            print repr(dna.match_state(symbols=input))