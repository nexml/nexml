package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.w3c.dom.Document;

class CategoricalMatrixImpl extends
		MatrixImpl<CharacterState> implements CategoricalMatrix {
	public CategoricalMatrixImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	private Set<CharacterStateSet> mCharacterStateSets = new HashSet<CharacterStateSet>();

	public CharacterStateSet createCharacterStateSet() {
		CharacterStateSet characterStateSet = new CharacterStateSetImpl(getDocument());
		mCharacterStateSets.add(characterStateSet);
		return characterStateSet;
	}

	public Set<CharacterStateSet> getCharacterStateSets() {
		return Collections.unmodifiableSet(mCharacterStateSets);
	}
}
