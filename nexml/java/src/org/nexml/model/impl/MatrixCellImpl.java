package org.nexml.model.impl;

import org.nexml.model.MatrixCell;

public class MatrixCellImpl extends NexmlWritableImpl implements MatrixCell {

	@Override
	String getTagName() {
		return "cell";
	}
}
