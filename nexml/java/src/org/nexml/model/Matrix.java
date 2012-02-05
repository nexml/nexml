package org.nexml.model;

import java.util.List;

/**
 * @param <T> should be either a {@code Double} or {@CharacterState}.
 */
public interface Matrix<T> extends OTUsLinkable, Annotatable, Segmented<Character>, SetManager {
	/**
	 * Gets all matrix cell objects for the provided OTU
	 * @param otu 
	 * @return a list of matrix cells
	 */
	List<MatrixCell<T>> getRow(OTU otu);
	
	/**
	 * Gets the row object for the provided OTU
	 * @param otu
	 * @return
	 */
	MatrixRow<T> getRowObject(OTU otu);

	/**
	 * Gets all matrix cell objects for the provided column (character)
	 * @param a character
	 * @return a list of matrix cells
	 */
	List<MatrixCell<T>> getColumn(Character character);

	/**
	 * Gets the matrix cell defined by the otu ("row") and 
	 * character ("column")
	 * @param otu
	 * @param character
	 * @return a matrix cell
	 */
	MatrixCell<T> getCell(OTU otu, Character character);
	

	/**
	 * Returns a list of the characters ("columns") in the matrix
	 * @return list of character
	 */
	List<Character> getCharacters();

	/**
	 * Removes a column from the matrix
	 * @param character
	 */
	
	void removeCharacter(Character character);
	
	/**
	 * This method creates a char element, i.e. a column definition.
	 * Because NeXML requires for categorical matrices that these
	 * column definitions have an attribute to reference the 
	 * applicable state set, the state set object needs to be passed
	 * in here, from which the attribute's value is set. 
	 * @author rvosa
	 */
	Character createCharacter(CharacterStateSet stateSet);
	
	/**
	 * Parses the provided character state string representation and
	 * returns whatever object is most appropriate given the context.
	 * E.g. for continuous matrices, the provided symbol is assumed to
	 * be a string representation of a double precision number, so the 
	 * returned value is a Double
	 * @param symbol
	 * @return
	 */
	T parseSymbol(String symbol);
	
	/**
	 * Creates a row element for OTU otu, and populates
	 * it with a seq element
	 * @param seq
	 * @param otu
	 */
	void setSeq(String seq, OTU otu);
	
}
