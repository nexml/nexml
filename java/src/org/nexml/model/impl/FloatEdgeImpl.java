package org.nexml.model.impl;

import org.nexml.model.FloatEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class FloatEdgeImpl extends EdgeImpl implements FloatEdge {

	public FloatEdgeImpl(Document document) {
		super(document);
	}

	public FloatEdgeImpl(Document rootDocument, Element element) {
		super(rootDocument, element);
	}

	public Double getLength() {
		return (Double)getLengthAsNumber();
	}

	public void setLength(Double length) {
		setLengthAsNumber(length);
	}
}
