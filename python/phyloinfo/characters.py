#! /usr/bin/env python

############################################################################
##  characters.py
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
This module handles the core definition of tree data structure class,
as well as all the structural classes that make up a tree.
"""

import re

from phyloinfo import elementary
from phyloinfo import taxa

GAP_CHARS = ['-', '?']

def map_to_iupac_ambiguity_code(states):
    """
    Given a sequence of characters, maps ambiguities given the form of
    `AG` to IUPAC codes (e.g., `AC -> R').
    """
    if len(states) == 1:
        return states[0]
    states = [state.upper() for state in states]
    if states.count('A') and states.count('C') and states.count('G') and (states.count('T') or states.count('U')):
        return 'N'
    if states.count('A') and states.count('C') and states.count('G'):
        return 'V'
    if states.count('A') and states.count('C') and (states.count('T') or states.count('U')):
        return 'H'
    if states.count('A') and states.count('G') and (states.count('T') or states.count('U')):
        return 'D'
    if states.count('C') and states.count('G') and (states.count('T') or states.count('U')):
        return 'B'
    if states.count('A') and states.count('C'):
        return 'M'
    if states.count('A') and states.count('G'):
        return 'R'
    if states.count('C') and (states.count('T') or states.count('U')):
        return 'W'    
    if states.count('C') and states.count('G'):
        return 'S'
    if states.count('C') and (states.count('T') or states.count('U')):
        return 'Y'
    if states.count('G') and (states.count('T') or states.count('U')):
        return 'K'
    raise Exception('Unrecognized characters in "%s"' % states)

def parse_sequence_iupac_ambiguities(seq):
    """
    Given a sequence of characters, with ambiguities denoted by
    `{<STATES>}`, this returns a sequence of characters with the
    ambiguities mapped to the IUPAC codes.
    """
    if isinstance(seq, list):
      as_list = True
      seq = ''.join(seq)
    else:
      as_list = False
    result = ""
    pattern = re.compile('{(.*?)}')
    pos = 0
    match = pattern.search(seq, pos)
    if match:
        while match:
          if pos > 0:
            result = result + seq[pos-1:match.start()]
          else:
            result = result + seq[pos:match.start()]
          result = result + map_to_ambiguity_code(match.group(1))
          pos = match.end() + 1
          if pos < len(seq) - 1:
            match = pattern.search(seq, pos)
          else:
            match = None
        result = result + seq[pos-1:]
    else:
      return seq
    if as_list:
      return [char for char in result]
    else:
      return result

class CharacterType(object):
    def __init__(self, state_list, pad_char='-'):
        """
        Initializes state list, and optional padding character.
        """
        self.state_list = state_list
        self.pad_char = pad_char

    def map_state_names(self, state_names):
        """
        Given a state name, returns index.
        """
        return [self.state_list.index(state_name) for state_name in state_names]

    def map_state_indexes(self, state_indexes):
        """
        Given a state index, returns its name.
        """
        return [self.state_list[idx] for idx in state_indexes]
        
RNA_CHARTYPE = CharacterType(state_list="ACGU?NXMRWSYKVHDB-", pad_char='-')
DNA_CHARTYPE = CharacterType(state_list="ACGT?NXMRWSYKVHDB-", pad_char='-')
INFSITES_CHARTYPE = CharacterType(state_list="10", pad_char='0')

class CharBlock(dict, taxa.TaxaLinked):
    """
    Character manager.
    """

    def __init__(self, *args, **kwargs):
        """
        Inits. Handles keyword arguments: `elem_id`, `label`, and `taxa_block` and `chartype`.
        """
        dict.__init__(self, *args)
        taxa.TaxaLinked.__init__(self, *args, **kwargs)
        if 'chartype' in kwargs:
            self.chartype = kwargs['chartype']

    def new_sequence(self, taxon, elem_id=None, state_indexes=None, state_names=None):
        """
        Adds a new sequence of the correct character type.
        """
        seq = CharSequence(chartype=self.chartype, elem_id=elem_id, state_indexes=state_indexes, state_names=state_names, taxon=taxon)
        self[taxon] = seq
        return seq

class CharSequence(taxa.TaxonLinked):
    """
    A sequence of (biological) characters.
    """

    def __init__(self, chartype, elem_id=None, state_indexes=None, state_names=None, taxon=None):
        """
        Inits. Handles keyword arguments: `elem_id` and `label`.
        """
        taxa.TaxonLinked.__init__(self, elem_id=elem_id, taxon=taxon)        
        self.chartype = chartype        
        self.state_indexes = []
        if state_indexes:
            self.state_indexes = state_indexes
        elif state_names:
            self.state_names = state_names
        
    def __len__(self):
        """
        Returns length of state_indexes.
        """
        if self.state_indexes:
            return len(self.state_indexes)
        else:
            return 0

    def _get_state_names(self):
        """
        Returns mapping of character states to their value
        representation (as a list).
        """
        return [self.chartype.state_list[idx] for idx in self.state_indexes]

    def _set_state_names(self, values):
        """
        Sets the character states based on the given character value
        representations.
        """
        ## TODO: must handle ambiguous and unrecognized characters        
        self.state_indexes = [self.chartype.state_list.index(char) for char in values]

    state_names = property(_get_state_names, _set_state_names)

    def extend(self, names=None, indexes=None):
        """
        Extends a sequence by adding given states.
        """
        if names:
            ## TODO: must handle ambiguous and unrecognized characters
            indexes = [self.chartype.state_list.index(char) for char in names]
        self.state_indexes.extend(indexes)

class NucBaseFrequencies(object):
    """
    Convenience class to track nucleotide base frequencies. Probably
    will abstract out generalizable methods to a base class later on.
    """

    ## STATIC METHODS ##

    def count_freqs(seq, ignore_gaps=False):
        """
        Counts occurrences of bases in given charsequence, and returns a
        BaseFrequencies object corresponding to counts. Does not
        handle ambiguities.
        """
        if isinstance(seq, list):
            charseq = seq
        else:
            # assume Sequence object
            charseq = seq.state_names
        state_list = DNA_CHARTYPE.state_list
        counts = [0 for i in range(len(state_list))]
        for char in charseq:
            try:
                ind = state_list.index(char)
                counts[ind] += 1
            except:
                if char not in GAP_CHARS:
                    raise
        if ignore_gaps:
            total = sum(counts)
        else:
            total = len(charseq)
        freqs = [float(count)/total for count in counts[:-1]]
        return NucBaseFrequencies(freqs)

    count_freqs = staticmethod(count_freqs)

    ## INSTANCE METHODS ##
    
    def __init__(self, freqs=None):
        """
        Initializes lists.
        """
        if freqs is not None:
            self.freqs = freqs
        else:
            self.freqs = [0.25, 0.25, 0.25, 0.25]
        if len(self.freqs) < 4:
            self.freqs.append(1.0 - sum(self.freqs))

    def _get_A(self):
        """
        Returns frequency of base A.
        """
        return self.freqs[0]

    A = property(_get_A)

    def _get_C(self):
        """
        Returns frequency of base C.
        """
        return self.freqs[1]

    C = property(_get_C)

    def _get_G(self):
        """
        Returns frequency of base G.
        """
        return self.freqs[2]

    G = property(_get_G)

    def _get_T(self):
        """
        Returns frequency of base T.
        """
        return self.freqs[3]

    T = property(_get_T)

    def _get_R(self):
        """
        Returns frequency of purine bases (A, G).
        """
        return self.A + self.G

    R = property(_get_R)

    def _get_Y(self):
        """
        Returns frequency of pyramidine bases (C, T).
        """
        return self.C + self.T

    Y = property(_get_Y)
            
class InfiniteSitesSequence(CharSequence):
    """
    An infinite sites sequence.
    """
    
    def __init__(self, state_indexes=None, global_mutation_index=None):
        """
        Initializes object.
        """
        self.mutated_positions = []
        if global_mutation_index is not None:
            self.global_mutation_index = global_mutation_index
        else:
            self.global_mutation_index = []
        super(InfiniteSitesSequence, self).__init__(chartype=INFSITES_CHARTYPE, state_indexes=state_indexes)

    def _get_state_indexes(self):
        """
        Composes list of state_indexes based on mutated positions.
        """
        state_indexes = []
        if self.global_mutation_index:
            maxpos = max(self.global_mutation_index)
        else:
            maxpos = 0
        for pos in range(maxpos + 1):
            if pos in self.mutated_positions:
                state_indexes.append(1)
            else:
                state_indexes.append(0)
        return state_indexes

    def _set_state_indexes(self, state_indexes):
        """
        Constructs list of mutated positions based on list of
        state_indexes.
        """
        self.mutated_positions = []
        for idx, char in enumerate(state_indexes):
            if char == 1:
                self.mutated_positions.append(idx)

    state_indexes = property(_get_state_indexes, _set_state_indexes)

    def _get_state_names(self):
        """
        Returns mapping of character states to their value
        representation (as a list).
        """
        return [str(char) for char in self._get_state_indexes()]

    def _set_state_names(self, values):
        """
        Sets the character states based on the given character value
        representations.
        """
        self._set_state_indexes(values)

    state_names = property(_get_state_names, _set_state_names)

        
class DnaSequence(CharSequence):
    """
    A sequence of nucleotides.
    """

    def __init__(self, state_indexes=None):
        """
        Initializes object.
        """
        super(NucleotideSequence, self).__init__(chartype=DNA_CHARTYPE,
                                                 state_indexes=state_indexes)        

    def _get_state_names(self):
        """
        Returns mapping of character states to their value
        representation (as a list).
        """
        return [self.chartype.state_list[idx] for idx in self.state_indexes]

    def _set_state_names(self, values):
        """
        Sets the character states based on the given character value
        representations.
        """
        self.state_indexes = [self.chartype.state_list.index(char) for char in values]

    state_names = property(_get_state_names, _set_state_names)
