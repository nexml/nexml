package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.Node;
import org.w3c.dom.Document;

abstract class EdgeImpl extends NexmlWritableImpl implements Edge {

	public EdgeImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
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
		return "edge";
	}

}
