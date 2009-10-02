package org.nexml.model.impl;

import org.nexml.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class NodeImpl extends OTULinkableImpl implements Node {

	private boolean mIsRoot = false;

    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
     * @param document a DOM document object
     * @author rvosa
     */
	protected NodeImpl(Document document) {
		super(document);
	}

    /**
     * Protected constructors are intended for recursive parsing, i.e.
     * starting from the root element (which maps onto DocumentImpl) we
     * traverse the element tree such that for every child element that maps
     * onto an Impl class the containing class calls that child's protected
     * constructor, passes in the element of the child. From there the 
     * child takes over, populates itself and calls the protected 
     * constructors of its children. These should probably be protected
     * because there is all sorts of opportunity for outsiders to call
     * these in the wrong context, passing in the wrong elements etc.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
	protected NodeImpl(Document document, Element element) {
		super(document, element);
		if ( element.getAttribute("root").equals("true") ) {
			setRoot(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}

	public static String getTagNameClass() {
		return "node";
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Node#isRoot()
	 */
	public boolean isRoot() {
		return mIsRoot;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Node#setRoot(boolean)
	 */
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
		final String TAB = "|";
		String retValue = "";
		retValue = "NodeImpl(" + getLabel() + TAB + "mIsRoot=" + this.mIsRoot
				+ TAB + ")";
		return retValue;
	}
}
