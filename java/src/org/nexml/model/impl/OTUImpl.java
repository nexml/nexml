package org.nexml.model.impl;

import org.nexml.model.OTU;
import org.w3c.dom.Document;

class OTUImpl extends NexmlWritableImpl implements OTU {

	public OTUImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	@Override
	String getTagName() {
		return "otu";
	}

}
