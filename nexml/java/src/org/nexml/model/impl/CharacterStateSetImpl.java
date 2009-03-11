package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;

public class CharacterStateSetImpl extends
		SetManager<CharacterState> implements CharacterStateSet {

	private Set<CharacterState> mCharacterStates;

	@Override
	String getTagName() {
		return "states";
	}

	public Set<CharacterState> getCharacterStates() {
		return mCharacterStates;
	}

	public void setCharacterStates(Set<CharacterState> characterStates) {
		mCharacterStates = characterStates;
	}

	public CharacterState createCharacterState() {
		return new CharacterStateImpl();
	}

}
