package org.nexml.model;

import java.util.Set;

public interface MolecularMatrix extends Matrix<CharacterState> {
	public static final String DNA = "Dna";
	public static final String RNA = "Rna";
	public static final String Protein = "Protein";
	/**
	 * Gets the charactere state sets associated with the
	 * invocant matrix. Typically, this would be a single,
	 * fixed state set.
	 */
	Set<CharacterStateSet> getCharacterStateSets();
	
	/**
	 * Creates and returns a new character state set
	 */
	CharacterStateSet createCharacterStateSet();
	
	/**
	 * This method creates a char element, i.e. a column definition.
	 * Because NeXML requires for categorical matrices that these
	 * column definitions have an attribute to reference the 
	 * applicable state set, the state set object needs to be passed
	 * in here, from which the attribute's value is set. 
	 * @author rvosa
	 */	
	Character createCharacter(CharacterStateSet characterStateSet);
	
	/**
	 * Retrieves a fixed character state set for IUPAC single
	 * character DNA nucleotide symbols
	 */
	CharacterStateSet getDNACharacterStateSet();

	/**
	 * Retrieves a fixed character state set for IUPAC single
	 * character RNA nucleotide symbols
	 */
	CharacterStateSet getRNACharacterStateSet();
	
	/**
	 * Retrieves a fixed character state set for IUPAC single
	 * character amino acid symbols
	 */
	CharacterStateSet getProteinCharacterStateSet();
	
	CharacterState parseSymbol(String symbol,String subDataType);
}
