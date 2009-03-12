package org.nexml.model.impl;

import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;
import org.w3c.dom.Document;

class CharacterImpl extends AnnotatableImpl implements Character {

	public CharacterImpl(Document document) {
		super(document);
	}

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
