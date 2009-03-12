package org.nexml.model.impl;

import org.nexml.model.OTU;
import org.nexml.model.OTULinkable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class OTULinkableImpl extends AnnotatableImpl implements
		OTULinkable {
	
	public OTULinkableImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	public OTULinkableImpl(Document document, Element element) {
		super(document, element);
	}

	private OTU mOTU;

	public OTU getOTU() {
		return mOTU;
	}

	public void setOTU(OTU otu) {
		mOTU = otu;
		getElement().setAttribute("otu", otu.getId());
	}
}
