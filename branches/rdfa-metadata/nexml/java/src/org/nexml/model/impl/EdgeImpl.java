package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class EdgeImpl extends AnnotatableImpl implements Edge {
	private Node mSource;
	private Node mTarget;
	private Number mLength;
	
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
	protected EdgeImpl(Document document) {
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
	protected EdgeImpl(Document document, Element element) {
		super(document, element);
	}

	protected Number getLengthAsNumber() {
		return mLength;
	}

	protected void setLengthAsNumber(Number length) {
		mLength = length;
		getElement().setAttribute("length", length.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Edge#getSource()
	 */
	public Node getSource() {
		return mSource;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Edge#getTarget()
	 */
	public Node getTarget() {
		return mTarget;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Edge#setSource(org.nexml.model.Node)
	 */
	public void setSource(Node source) {
		mSource = source;
		getElement().setAttribute("source", source.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Edge#setTarget(org.nexml.model.Node)
	 */
	public void setTarget(Node target) {
		mTarget = target;
		getElement().setAttribute("target", target.getId());
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
		return "edge";
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

		retValue = "EdgeImpl(" + super.toString() + TAB + "mSource="
				+ this.mSource + TAB + "mTarget=" + this.mTarget + TAB
				+ "mLength=" + this.mLength + TAB + ")";

		return retValue;
	}

}
