package org.nexml.model.impl;

import org.nexml.model.MatrixCell;
import org.w3c.dom.Document;

class MatrixCellImpl<T> extends AnnotatableImpl implements
		MatrixCell<T> {

	public MatrixCellImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	private T mValue;

	@Override
	String getTagName() {
		return "cell";
	}

	public T getValue() {
		return mValue;
	}

	public void setValue(T value) {
		mValue = value;
	}
}
