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
	
}
