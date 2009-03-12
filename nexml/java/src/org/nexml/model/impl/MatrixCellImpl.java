package org.nexml.model.impl;

import org.nexml.model.MatrixCell;

class MatrixCellImpl<T> extends NexmlWritableImpl implements
		MatrixCell<T> {

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
