package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class IntEdgeImpl extends EdgeImpl implements IntEdge {

	public IntEdgeImpl(Document document) {
		super(document);
	}

	public IntEdgeImpl(Document document, Element thisEdgeElement) {
		super(document, thisEdgeElement);
	}

	public Integer getLength() {
		return (Integer) getLengthAsNumber();
	}

	public void setLength(Integer length) {
		setLengthAsNumber(length);
	}
}
