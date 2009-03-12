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

	/**
	 * Constructs a <code>String</code> with all attributes in name = value
	 * format.
	 * 
	 * @return a <code>String</code> representation of this object.
	 */
	@Override
	public String toString() {
		final String TAB = "    ";

		String retValue = "";

		retValue = "NodeImpl ( " + super.toString() + TAB + "mIsRoot = "
				+ this.mIsRoot + TAB + " )";

		return retValue;
	}

}
