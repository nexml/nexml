package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CompoundCharacterState;

public abstract class CompoundCharacterStateImpl extends NexmlWritableImpl
		implements CompoundCharacterState {
	private Set<CharacterState> mCharacterStates;

	public Set<CharacterState> getStates() {
		return mCharacterStates;
	}

	public void setStates(Set<CharacterState> characterStates) {
		mCharacterStates = characterStates;
	}

}
