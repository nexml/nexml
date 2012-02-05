package org.nexml.model;

import java.util.List;

public interface MatrixRow<T> extends OTULinkable, Annotatable, Segmented<T> {

	/**
	 * Gets the matrix cell defined by the otu ("row") and 
	 * character ("column")
	 * @param otu
	 * @param character
	 * @return a matrix cell
	 */
	MatrixCell<T> getCell(Character character);
	
	/**
	 * 
	 * @param matrixCell
	 * @param character
	 */
	void setCell(MatrixCell<T> matrixCell, Character character);
	
	/**
	 * 
	 * @param character
	 */
	void removeCharacter(Character character);

	/**
	 * Gets all matrix cell objects for the provided OTU
	 * @param otu 
	 * @return a list of matrix cells
	 */
	List<MatrixCell<T>> getCells(List<Character> characterList);	

	/**
	 * Populates the text content of the <seq> element
	 * @param seqString
	 */
	void setSeq(String seqString);	
	
	/**
	 * Returns the text content of the <seq> element
	 * @return
	 */
	String getSeq();
	
}
