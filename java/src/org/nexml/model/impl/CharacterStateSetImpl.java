package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;

public abstract class CharacterStateSetImpl<T extends CharacterState> extends
		SetManager<CharacterState> implements CharacterStateSet<T> {

	private Set<T> mCharacterStates;

	@Override
	String getTagName() {
		return "states";
	}

	public Set<T> getCharacterStates() {
		return mCharacterStates;
	}

	public void setCharacterStates(Set<T> characterStates) {
		mCharacterStates = characterStates;
	}

}
