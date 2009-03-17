package org.nexml.model;

import java.util.Set;

public interface CategoricalMatrix extends Matrix<CharacterState> {

	/**
	 * Returns all character state sets, i.e. the <states/>
	 * elements in a <format/> element.
	 * @return all character state sets
	 */
	Set<CharacterStateSet> getCharacterStateSets();
	
	/**
	 * Creates a new character state set (i.e. a <states/>
	 * element).
	 * @return
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
	
}
