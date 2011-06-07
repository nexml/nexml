package org.nexml.model;

import java.util.Set;

public interface MolecularMatrix extends Matrix<CharacterState> {
	public static final String DNA = "Dna";
	public static final String RNA = "Rna";
	public static final String Protein = "Protein";
	
	/**
	 * Gets the character state sets associated with the
	 * invocant matrix. Typically, this would be a single,
	 * fixed state set.
	 */
	Set<CharacterStateSet> getCharacterStateSets();
	
	/**
	 * Gets the default character state set. Typically this
	 * will be a fixed, IUPAC-compliant state set.
	 * @return
	 */
	CharacterStateSet getCharacterStateSet();
	
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
	
	/**
	 * Parses the provided character state string representation and
	 * returns the CharacterState object that is most appropriate for the
	 * provided subDataType (which would be something like MolecularMatrix.DNA)
	 */
	CharacterState parseSymbol(String symbol,String subDataType);
}
