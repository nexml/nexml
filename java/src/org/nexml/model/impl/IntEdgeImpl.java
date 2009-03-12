package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class IntEdgeImpl extends EdgeImpl implements IntEdge {

	public IntEdgeImpl(Document document) {
		super(document);
	}

	public IntEdgeImpl(Document document, Element element) {
		super(document, element);
	}

	public Integer getLength() {
		return (Integer) getLengthAsNumber();
	}

	public void setLength(Integer length) {
		setLengthAsNumber(length);
	}

	/**
	 * Constructs a <code>String</code> with all attributes in name = value
	 * format.
	 * 
	 * @return a <code>String</code> representation of this object.
	 */
	@Override
	public String toString() {
		final String TAB = "|";
		String retValue = "";
		retValue = "IntEdgeImpl(" + super.toString() + TAB + ")";
		return retValue;
	}

}
