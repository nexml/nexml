package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;

class CategoricalMatrixImpl extends
		MatrixImpl<CharacterState> implements CategoricalMatrix {
	private Set<CharacterStateSet> mCharacterStateSets = new HashSet<CharacterStateSet>();

	public CharacterStateSet createCharacterStateSet() {
		CharacterStateSet characterStateSet = new CharacterStateSetImpl();
		mCharacterStateSets.add(characterStateSet);
		return characterStateSet;
	}

	public Set<CharacterStateSet> getCharacterStateSets() {
		return Collections.unmodifiableSet(mCharacterStateSets);
	}
}
