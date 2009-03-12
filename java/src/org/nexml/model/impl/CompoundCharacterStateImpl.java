package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CompoundCharacterState;
import org.w3c.dom.Document;

abstract class CompoundCharacterStateImpl extends CharacterStateImpl
		implements CompoundCharacterState {
	public CompoundCharacterStateImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	private Set<CharacterState> mCharacterStates;

	public Set<CharacterState> getStates() {
		return mCharacterStates;
	}

	public void setStates(Set<CharacterState> characterStates) {
		mCharacterStates = characterStates;
	}

}
