package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.w3c.dom.Document;

class IntEdgeImpl extends EdgeImpl implements IntEdge {
	
	public IntEdgeImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	public Integer getLength() {
		return (Integer)getLengthAsNumber();
	}

	public void setLength(Integer length) {
		setLengthAsNumber(length);
	}
}
