package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CompoundCharacterState;

abstract class CompoundCharacterStateImpl extends CharacterStateImpl
		implements CompoundCharacterState {
	private Set<CharacterState> mCharacterStates;

	public Set<CharacterState> getStates() {
		return mCharacterStates;
	}

	public void setStates(Set<CharacterState> characterStates) {
		mCharacterStates = characterStates;
	}

}
