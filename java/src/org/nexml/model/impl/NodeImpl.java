package org.nexml.model.impl;

import org.nexml.model.Node;
import org.w3c.dom.Document;

class NodeImpl extends OTULinkableImpl implements Node {

	public NodeImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	@Override
	String getTagName() {
		return "node";
	}

}
