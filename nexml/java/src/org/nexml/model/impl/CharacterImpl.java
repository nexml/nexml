package org.nexml.model.impl;

import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;

class CharacterImpl extends NexmlWritableImpl implements Character {

	private CharacterStateSet mCharacterStateSet;

	@Override
	String getTagName() {
		return "char";
	}

	public CharacterStateSet getCharacterStateSet() {
		return mCharacterStateSet;
	}

	public void setCharacterStateSet(CharacterStateSet characterStateSet) {
		mCharacterStateSet = characterStateSet;
	}

}
