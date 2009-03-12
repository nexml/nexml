package org.nexml.model.impl;

import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class OTUImpl extends AnnotatableImpl implements OTU {

	/**
	 * Class version of {@code getTagName()}.
	 * 
	 * @return the tag name.
	 */
	static String getTagNameClass() {
		return "otu";
	}

	public OTUImpl(Document document) {
		super(document);
	}

	public OTUImpl(Document document, Element element) {
		super(document, element);

	}

	@Override
	String getTagName() {
		return getTagNameClass();
	}

}
