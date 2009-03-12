package org.nexml.model.impl;

import org.nexml.model.Character;
import org.nexml.model.ContinuousMatrix;
import org.w3c.dom.Document;

class ContinuousMatrixImpl extends MatrixImpl<Double> implements ContinuousMatrix {

	public ContinuousMatrixImpl(Document document) {
		super(document);
	}

	/**
	 * This method creates a char element, i.e. a column definition.
	 * @author rvosa
	 */
	public Character createCharacter() {
		CharacterImpl character = new CharacterImpl(getDocument());
		addThing(character);
		getFormatElement().appendChild(character.getElement());
		return character;
	}

}
