package org.nexml.model;

import java.util.Set;

public interface MolecularMatrix extends Matrix<CharacterState> {

	Set<CharacterStateSet> getCharacterStateSets();
	
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
	
	CharacterStateSet getDNACharacterStateSet();
	
	CharacterStateSet getRNACharacterStateSet();
	
	CharacterStateSet getProteinCharacterStateSet();
}
