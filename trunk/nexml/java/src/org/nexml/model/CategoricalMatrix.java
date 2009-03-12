package org.nexml.model;

import java.util.Set;

public interface CategoricalMatrix extends Matrix<CharacterState> {

	Set<CharacterStateSet> getCharacterStateSets();
	
	CharacterStateSet createCharacterStateSet();
	
	CharacterStateSet getDNACharacterStateSet();
	
	CharacterStateSet getRNACharacterStateSet();
	
	CharacterStateSet getProteinCharacterStateSet();
}
