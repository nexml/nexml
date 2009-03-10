package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.Node;

public abstract class EdgeImpl extends NexmlWritableImpl implements Edge {

	private Node mSource;

	private Node mTarget;

	protected Number mLength;

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
	}

	public void setTarget(Node target) {
		mTarget = target;
	}


	@Override
	String getTagName() {
		return "edge";
	}

}
