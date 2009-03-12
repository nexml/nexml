package org.nexml.model.impl;

import org.nexml.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class NodeImpl extends OTULinkableImpl implements Node {

	private boolean mIsRoot = false;
	
	public NodeImpl(Document document) {
		super(document);
	}

	public NodeImpl(Document document, Element element) { 
		super(document, element);
	}

	@Override
	String getTagName() {
		return getTagNameClass();
	}

	public static String getTagNameClass() {
		return "node";
	}

	public boolean isRoot() {
		return mIsRoot;
	}

	public void setRoot(boolean isRoot) {
		mIsRoot = isRoot;
	}

}
