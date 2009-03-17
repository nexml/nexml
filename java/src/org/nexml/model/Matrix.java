package org.nexml.model;

import java.util.List;

/**
 * @param <T> should be either a {@code Double} or {@CharacterState}.
 */
public interface Matrix<T> extends OTUsLinkable {
	/**
	 * Gets all matrix cell objects for the provided OTU
	 * @param otu 
	 * @return a list of matrix cells
	 */
	List<MatrixCell<T>> getRow(OTU otu);

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
	 * Removes a column from the matrix
	 * @param character
	 */
	List<Character> getCharacters();
	
	void removeCharacter(Character character);
	
	// XXX removeOtu? or removeRow?
	
}
