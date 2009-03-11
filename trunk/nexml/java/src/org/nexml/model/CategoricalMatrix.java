package org.nexml.model;

import java.util.Set;

public interface CategoricalMatrix extends Matrix<CharacterState> {

	Set<CharacterStateSet> getCategoricalCharacterStateSets();
	
	CharacterStateSet createCategoricalCharacterStateSet();
}
