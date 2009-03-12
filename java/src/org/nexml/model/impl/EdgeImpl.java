package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class EdgeImpl extends AnnotatableImpl implements Edge {

	public EdgeImpl(Document document) {
		super(document);
	}

	public EdgeImpl(Document document, Element element) {
		super(document, element);
	}

	private Node mSource;

	private Node mTarget;

	private Number mLength;

	protected Number getLengthAsNumber() {
		return mLength;
	}

	protected void setLengthAsNumber(Number length) {
		mLength = length;
	}

	public Node getSource() {
		return mSource;
	}

	public Node getTarget() {
		return mTarget;
	}

	public void setSource(Node source) {
		mSource = source;
		getElement().setAttribute("source", source.getId());
	}

	public void setTarget(Node target) {
		mTarget = target;
		getElement().setAttribute("target", target.getId());
	}

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
		final String TAB = "    ";

		String retValue = "";

		retValue = "EdgeImpl ( " + super.toString() + TAB + "mSource = "
				+ this.mSource + TAB + "mTarget = " + this.mTarget + TAB
				+ "mLength = " + this.mLength + TAB + " )";

		return retValue;
	}

}
