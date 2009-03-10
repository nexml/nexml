package org.nexml.model.impl;

import java.math.BigDecimal;

import org.nexml.model.Edge;
import org.nexml.model.Node;

public class EdgeImpl extends NexmlWritableImpl implements Edge {
	private Node mSource, mTarget;
	
	private BigDecimal mLength;

	@Override
	String getTagName() {
		return "edge";
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

}
