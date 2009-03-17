package org.nexml.model;

public interface Character extends Annotatable {
    
	
	/**
	 * Returns the character state set for the focal character,
	 * i.e. an enumeration of all allowed states for that matrix
	 * column, including their ambiguity mappings. 
	 * @return
	 */
	CharacterStateSet getCharacterStateSet();

	/**
	 * Sets the character state set for the focal character,
	 * i.e. an enumeration of all allowed states for that matrix
	 * column, including their ambiguity mappings.
	 */	
	void setCharacterStateSet(CharacterStateSet characterStateSet);
}
