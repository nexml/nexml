package org.nexml.model.impl;

import org.nexml.model.FloatEdge;
import org.w3c.dom.Document;

class FloatEdgeImpl extends EdgeImpl implements FloatEdge {

	public FloatEdgeImpl(Document document) {
		super(document);
	}

	public Double getLength() {
		return (Double)getLengthAsNumber();
	}

	public void setLength(Double length) {
		setLengthAsNumber(length);
	}
}
