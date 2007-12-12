#! /usr/bin/env python

############################################################################
##  nexus.py
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
This module wraps routines needed for reading and writing trees (and data) in
NEXUS format.
"""

import re
import os
import sys
import StringIO

from phyloinfo import datasets
from phyloinfo import taxa
from phyloinfo import trees
from phyloinfo import characters

class NexusReader(datasets.Reader):
    """
    Encapsulates loading and parsing of a NEXUS format file.
    """    
    
    ##########################################################
    ## CHARACTER/SYMBOL ANALYSIS AND VALIDATION             ##
    ##########################################################
    
    punctuation = '\(\)\[\]\{\}\\\/\,\;\:\=\*\'\"\`\+\-\<\>'
    whitespace = ' \0\t\n\r'
    
    def is_whitespace(char):
        return char in NexusReader.whitespace

    is_whitespace = staticmethod(is_whitespace)
    
    def has_whitespace(s):
        return re.search('['+NexusReader.whitespace+']', s) != None

    has_whitespace = staticmethod(has_whitespace)
    
    def is_punctuation(char):
        if char:
            return char in NexusReader.punctuation

    is_punctuation = staticmethod(is_punctuation)    
    
    def has_punctuation(s):
        return re.search('['+NexusReader.punctuation+']', s) != None

    has_punctuation = staticmethod(has_punctuation)
    
    def is_whitespace_or_punctuation(char):
        return NexusReader.is_whitespace(char) or NexusReader.is_punctuation(char)

    is_whitespace_or_punctuation = staticmethod(is_whitespace_or_punctuation)
    
    def has_whitespace_or_punctuation(s):
        return NexusReader.has_whitespace(s) or NexusReader.has_punctuation(s)

    has_whitespace_or_punctuation = staticmethod(has_whitespace_or_punctuation)

    def validate_identifier(label):
        if NexusReader.has_whitespace_or_punctuation(label):
            if (label[0] == "'" and label[1] == "'") or label[0] != "'":
                label = "'" + label
            if (label[-1] == "'" and label[-2] == "'") or label[-1] != "'":
                label = label + "'"
            return label
        else:
            return label

    validate_identifier = staticmethod(validate_identifier)
    
    ##########################################################
    ## EXCEPTIONS                                           ##
    ##########################################################    
    
    class SyntaxException(Exception):

        def __init__(self, filepath, row, column, message):
            self.filepath = filepath
            self.row = row
            self.column = column
            self.message = message

        def __str__(self):
            return '\n\nERROR PARSING FILE: "%s"\nLine %d: %s' % (self.filepath, self.row, self.message) 

    class NotNexusFileException(SyntaxException):
        def __init__(self, filepath, row, column, message):
            super(NotNexusFileException, self).__init__(filepath, row, column, message)

    ##########################################################
    ## CLASS MEMBER METHODS                                 ##
    ##########################################################

    def __init__(self):
        """
        `tree_factory` is a DendroPy TreeFactory class or derived
        object.
        """
        datasets.Reader.__init__(self)
        self.dataset = None

    ## Implementation of the datasets.Reader interface ##

    def read_dataset(self, fileobj):
        """
        Instantiates and returns a DataSet object based on the
        NEXML-formatted contents read from the file descriptor object
        `fileobj`.
        """
        self.filehandle = fileobj
        return self.parse_nexus_file()

    ## Class-specific ##

    def parse_nexus_file(self):
        """
        Main file parsing driver.
        """
        self.dataset = datasets.Dataset()
        self.interleave = False
        self.datatype = characters.DNA_CHARTYPE
        self.gap_char = '-'
        self.missing_char = '?'
        self.match_char = '.'
        self.current_file_char = None
        self.eof = False
        self.current_line_number = 1
        self.current_col_number = 1
        self.previous_file_char = None        
        token = self.read_next_token_ucase()
        if token != "#NEXUS":
            raise self.syntax_exception('Expecting "#NEXUS", but found "%s"' % token)
        else:
            while not self.eof:
                token = self.read_next_token_ucase()
                while token != None and token != 'BEGIN' and not self.eof:
                    token = self.read_next_token_ucase()
                token = self.read_next_token_ucase()
                if token == 'TAXA':
                    self.skip_to_semicolon() # move past BEGIN statement
                    while not (token == 'END' or token == 'ENDBLOCK') and not self.eof and not token==None:
                        token = self.read_next_token_ucase()
                        if token == 'DIMENSIONS':
                            self.parse_dimensions_statement()                        
                        if token == 'TAXLABELS':
                            self.parse_taxlabels_statement()
                    self.skip_to_semicolon() # move past END statement
                elif token == 'CHARACTERS':
                    self.skip_to_semicolon() # move past BEGIN command
                    while not (token == 'END' or token == 'ENDBLOCK') and not self.eof and not token==None:
                        token = self.read_next_token_ucase()
                        if token == 'DIMENSIONS':
                            self.parse_dimensions_statement()    
                        if token == 'FORMAT':
                            self.parse_format_statement()                              
                        if token == 'MATRIX':
                            self.parse_matrix_statement()
                    self.skip_to_semicolon() # move past END command
                elif token == 'DATA':
                    self.skip_to_semicolon() # move past BEGIN command
                    while not (token == 'END' or token == 'ENDBLOCK') and not self.eof and not token==None:
                        token = self.read_next_token_ucase()
                        if token == 'DIMENSIONS':
                            self.parse_dimensions_statement()     
                        if token == 'FORMAT':
                            self.parse_format_statement()                               
                        if token == 'MATRIX':
                            self.parse_matrix_statement()
                    self.skip_to_semicolon() # move past END command
                elif token == 'TREES':
                    self.skip_to_semicolon() # move past BEGIN command
                    while not (token == 'END' or token == 'ENDBLOCK') and not self.eof and not token==None:
                        token = self.read_next_token_ucase()
                        if token == 'TRANSLATE':
                            self.parse_translate_statement()                         
                        if token == 'TREE':
                            self.parse_tree_statement()    
                    self.skip_to_semicolon() # move past END command    
                else:
                    # unknown block
                    while not (token == 'END' or token == 'ENDBLOCK') and not self.eof and not token==None:
                        #print token
                        self.skip_to_semicolon()
                        token = self.read_next_token_ucase()
        return self.dataset
    
    def _get_current_file_char(self):
        """
        Returns the current character from the file stream.
        """
        if self.__current_file_char == None:
            self.__current_file_char = self.read_next_char()
        return self.__current_file_char

    def _set_current_file_char(self, new_char):
        self.__current_file_char = new_char

    current_file_char = property(_get_current_file_char, _set_current_file_char)
    
    def read_next_char(self):
        """
        Advances the file stream cursor to the next character and returns 
        it.
        """
        if self.filehandle:
            read_char = self.filehandle.read(1) # returns empty string if EOF
            if read_char == '':
                self.eof = True
            else:
                if self.previous_file_char == '\n':
                    self.current_line_number = self.current_line_number + 1
                    self.current_col_number = 0
                self.previous_file_char = self.__current_file_char
                self.current_col_number = self.current_col_number + 1
            self.current_file_char = read_char
            return self.__current_file_char
        else:
            return None    
            
    def skip_comment(self):
        """
        Reads characters from the file until current comment block (and 
        any nested comment block terminates. Assumes current cursor 
        position is on first character inside comment block.
        """
        while self.current_file_char != ']' and not self.eof:
            if self.read_next_char() == '[':
                self.skip_comment()
        self.read_next_char()
        
    def read_noncomment_character(self):
        """
        Gets the first character outside a comment block from the
        current file stream position, inclusive.
        """
        if self.current_file_char == '[':
            self.skip_comment()
        return self.current_file_char
    noncomment_file_char = property(read_noncomment_character)
    
    def skip_to_significant_character(self):
        """
        Advances to the first non-whitespace character outside a comment block.
        """
        while (NexusReader.is_whitespace(self.current_file_char) or self.current_file_char=='[') and not self.eof:
            if self.current_file_char=='[':
                self.read_noncomment_character()
            else:
                self.read_next_char()
        return self.current_file_char
        
    def read_next_token(self, ignore_punctuation=[]):
        """
        Reads the next token in the file stream. A token in this context is any word or punctuation character
        outside of a comment block.
        """
        if not self.eof:
            token = ''
            self.skip_to_significant_character()
            if not self.eof:
                if self.current_file_char == "'":
                    self.read_next_char()
                    end_quote = False
                    while not end_quote and not self.eof:  
                        if self.current_file_char == "'":                        
                            self.read_next_char()
                            if self.current_file_char == "'":
                                token = token + "'"
                                self.read_next_char()
                            else:
                                end_quote = True
                        else:
                            token = token + self.current_file_char
                            self.read_next_char()
                else:
                    if NexusReader.is_punctuation(self.current_file_char) and self.current_file_char not in ignore_punctuation:
                        token = self.current_file_char
                        self.read_next_char()
                    else:
                        while not self.eof and (not NexusReader.is_whitespace_or_punctuation(self.current_file_char) or self.current_file_char in ignore_punctuation): 
                            token = token + self.current_file_char
                            self.read_next_char()
                self.current_token = token
            else:
                self.current_token = None
        else:
            self.current_token = None
        return self.current_token

    def read_next_token_ucase(self, ignore_punctuation=[]):
        """
        Reads the next token in the file stream, upper-casing it 
        before returning it.
        """
        t = self.read_next_token(ignore_punctuation=ignore_punctuation)
        if t != None:
            return t.upper()
        else:
            return None
        
    def skip_to_semicolon(self):
        """
        Advances the file stream cursor to the next semi-colon.
        """
        token = self.read_next_token()
        while token != ';' and not self.eof and token != None: 
            token = self.read_next_token()
            pass
    
    def syntax_exception(self, message):
        """
        Returns an exception object parameterized with the current filepath, line and 
        column number values.
        """
        return NexusReader.SyntaxException(self.filepath, self.current_line_number, self.current_col_number, message)
    
    def parse_format_statement(self):
        """
        Processes a FORMAT command. Assumes that the file reader is 
        positioned right after the "FORMAT" token in a FORMAT command.
        """
        token = self.read_next_token_ucase()
        while token != ';':
            #print token
            if token == 'DATATYPE':
                token = self.read_next_token_ucase()
                if token == '=':
                    token = self.read_next_token_ucase()
                    if token == "DNA" or token == "NUCLEOTIDES":                        
                        self.datatype = characters.DNA_CHARTYPE
                    elif token == "RNA":
                        self.datatype = characters.RNA_CHARTYPE
                else:
                    raise self.syntax_exception('Expecting "=" after DATATYPE keyword')
            elif token == 'GAP':
                token = self.read_next_token_ucase()
                if token == '=':
                    token = self.read_next_token_ucase()
                    self.gap_char = token
                else:
                    raise self.syntax_exception('Expecting "=" after GAP keyword')
            elif token == 'MISSING':
                token = self.read_next_token_ucase()
                if token == '=':
                    token = self.read_next_token_ucase()
                    self.missing_char = token
                else:
                    raise self.syntax_exception('Expecting "=" after MISSING keyword')       
            elif token == 'MATCHCHAR':
                token = self.read_next_token_ucase()
                if token == '=':
                    token = self.read_next_token_ucase()
                    self.match_char = token
                else:
                    raise self.syntax_exception('Expecting "=" after MISSING keyword')                 
            token = self.read_next_token_ucase()   

    def parse_tree_statement(self):
        """
        Processes a TREE command. Assumes that the file reader is 
        positioned right after the "TREE" token in a TREE command.
        Calls on the NewickStatementParser of the trees module.
        """                
        token = self.read_next_token()
        tree_name = token
        token = self.read_next_token()
        if token != '=':
            raise self.syntax_exception('Expecting "=" in definition of Tree "%s"' % tree.name)
        else:
            # collect entire tree statement by accumulating tokens until we reach a semi-colon
            statement = []
            token = self.read_next_token()
            while token and token != ';': 
                statement.append(token)
                token = self.read_next_token()
            tp = NewickStatementParser(statement)
            tree = tp.tree                    
            #for n in child_nodes:
                #tree.head_node.add_child(n)
            #if not NexusReader.is_punctuation(token):
                #tree.head_node.label = token
                #token = self.read_next_token()
            #if token == ':':
                #tree.head_node.edge_weight = self.read_next_token(ignore_punctuation='-')
            tree.name = tree_name
            self.trees.append(tree)
        if self.current_token != ';':
            self.skip_to_semicolon()        
            
    def parse_translate_statement(self):
        """
        Processes a TRANSLATE command. Assumes that the file reader is 
        positioned right after the "TRANSLATE" token in a TRANSLATE command.
        """     
        token = self.current_token
        while token and token != ';':
            translation_token = self.read_next_token()
            translation_label = self.read_next_token()
            token = self.read_next_token() # ","
            #print translation_token, translation_label, token
            if token != ',' and token != ';':
                raise self.syntax_exception('Expecting "," in TRANSLATE statement after definition for %s = "%s", but found "%s" instead' % (translation_token, translation_label, token))
            else:
                self.tree_translate_dict[translation_token] = translation_label

    def parse_dimensions_statement(self):
        """
        Processes a DIMENSIONS command. Assumes that the file reader is 
        positioned right after the "DIMENSIONS" token in a DIMENSIONS command.
        """
        token = self.read_next_token_ucase()
        while token != ';':
            #print token
            if token == 'NTAX':
                token = self.read_next_token_ucase()
                if token == '=':
                    token = self.read_next_token_ucase()
                    if token.isdigit():
                        self.file_specified_ntax = int(token)
                    else:
                        raise self.syntax_exception('Expecting numeric value for NTAX')
                else:
                    raise self.syntax_exception('Expecting "=" after NTAX keyword')
            elif token == 'NCHAR':
                token = self.read_next_token_ucase()
                if token == '=':
                    token = self.read_next_token_ucase()
                    if token.isdigit():
                        self.file_specified_nchar = int(token)
                    else:
                        raise self.syntax_exception('Expecting numeric value for NCHAR')
                else:
                    raise self.syntax_exception('Expecting "=" after NCHAR keyword')
            token = self.read_next_token_ucase()
            
    def parse_taxlabels_statement(self, taxa_block=None):
        """
        Processes a TAXLABELS command. Assumes that the file reader is 
        positioned right after the "TAXLABELS" token in a TAXLABELS command.
        """
        if taxa_block is None:
            taxa_block = self.dataset.new_taxa_block()
        token = self.read_next_token()
        while token != ';':
            taxon = taxa.Taxon(label=token)
            taxa_block.append(taxon)
#             if taxon not in self.dataset.taxlabels:
#                 self.dataset.add_taxon(taxon)
#             else:
#                 raise self.syntax_exception('Multiple declaration of taxon %s')
            token = self.read_next_token()            
    
    def parse_matrix_statement(self, taxa_block=None):
        """
        Processes a MATRIX command. Assumes that the file reader 
        is positioned right after the "MATRIX" token in a MATRIX command, 
        and that NTAX and NCHAR have been specified accurately.
        """
        if taxa_block is None:
            if len(self.dataset.taxa_blocks) == 0:
                taxa_block = self.dataset.new_taxa_block()
            else:
                taxa_block = self.dataset.taxa_blocks[0]
        if not self.file_specified_ntax:
            raise self.syntax_exception('NTAX must be defined by DIMENSIONS command to non-zero value before MATRIX command')
        elif not self.file_specified_nchar:
            raise self.syntax_exception('NCHAR must be defined by DIMENSIONS command to non-zero value before MATRIX command')
        else:
            char_block = self.dataset.new_char_block(taxa_block=taxa_block)
            char_block.chartype = self.datatype
            if True: # future: trap and handle no labels, transpose etc.
                token = self.read_next_token()
                while token != ';' and not self.eof:
                    taxon = taxa_block.find_taxon(label=token, update=True)
                    if taxon not in char_block:
                        sequence = char_block.new_sequence(taxon=taxon)
                    if self.interleave:
                        while self.current_file_char != '\n' and self.current_file_char != '\r':
                            if self.current_file_char not in [' ', '\t']:
                                char_block[taxon].extend(self.current_file_char)
                            self.read_next_char()
                    else:
                        while len(char_block[taxon]) < self.file_specified_nchar and not self.eof:
                            char_group = self.read_next_token()
                            char_group = characters.parse_sequence_iupac_ambiguities(char_group)
                            char_block[taxon].extend(char_group)
                    token = self.read_next_token()
            else:
                ## TODO: NO LABELS/TRANSPOSED ##
                pass

if __name__ == "__main__":
    source = "/home/jeet/Documents/Codeworks/Portfolios/Python/Projects/Phylogenetics/DendroPy/versions/trunk/phyloinfo/tests/files/primate-mtDNA.nex"
    nexus = NexusReader()
    dataset = nexus.get_dataset(filepath=source)
    import nexml
    nexmlw = nexml.NexmlWriter()
    print nexmlw.compose_dataset(dataset)
    
