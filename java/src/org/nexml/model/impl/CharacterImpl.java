package org.nexml.model.impl;

import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CharacterImpl extends AnnotatableImpl implements Character {

	public CharacterImpl(Document document) {
		super(document);
	}

	public CharacterImpl(Document document,Element element) {
		super(document,element);
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
		getElement().setAttribute("states", characterStateSet.getId());
	}

}
