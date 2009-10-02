package org.nexml.model;

public interface CharacterState extends Annotatable {
	
	/**
	 * Returns the state symbol for the focal state. Depending
	 * on the data type, this might be a IUPAC symbol, an Integer,
	 * etc.
	 * @return a state symbol
	 */
	Object getSymbol();

	/**
	 * Sets the state symbol for the focal state. Depending
	 * on the data type, this might be a IUPAC symbol, an Integer,
	 * etc.
	 */	
	void setSymbol(Object symbol);
}
