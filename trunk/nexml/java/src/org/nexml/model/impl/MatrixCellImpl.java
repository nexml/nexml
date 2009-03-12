package org.nexml.model.impl;

import org.nexml.model.CharacterState;
import org.nexml.model.MatrixCell;
import org.w3c.dom.Document;

class MatrixCellImpl<T> extends AnnotatableImpl implements
		MatrixCell<T> {

	public MatrixCellImpl(Document document) {
		super(document);
	}

	private T mValue;

	@Override
	String getTagName() {
		return "cell";
	}

	public T getValue() {
		return mValue;
	}

	/**
	 * XXX Aargh!!!
	 * @author rvosa
	 */
	public void setValue(T value) {
		mValue = value;
		if ( value instanceof Double ) {
			// Continuous, i.e. Double
			getElement().setAttribute("state", value.toString());
		}
		else {
			// Categorical, i.e. CharacterState
			getElement().setAttribute("state", ((CharacterState)value).getId());
		}
	}
}
