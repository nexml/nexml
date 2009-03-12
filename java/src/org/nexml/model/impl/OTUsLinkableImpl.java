package org.nexml.model.impl;

import org.nexml.model.OTUs;
import org.nexml.model.OTUsLinkable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class OTUsLinkableImpl<T> extends SetManager<T> implements
		OTUsLinkable {

	public OTUsLinkableImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	public OTUsLinkableImpl(Document document, Element item) {
		super(document, item);
	}

	private OTUs mOTUs;

	public OTUs getOTUs() {
		return mOTUs;
	}

	public void setOTUs(OTUs otus) {
		mOTUs = otus;
		getElement().setAttribute("otus", otus.getId());
	}

}
