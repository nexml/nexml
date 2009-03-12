package org.nexml.model.impl;

import org.nexml.model.OTUs;
import org.nexml.model.OTUsLinkable;
import org.w3c.dom.Document;

abstract class OTUsLinkableImpl<T> extends SetManager<T> implements
		OTUsLinkable {

	public OTUsLinkableImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
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
