package org.nexml.model.impl;

import org.nexml.model.FloatEdge;

public class FloatEdgeImpl extends EdgeImpl implements FloatEdge {

	public Double getLength() {
		return (Double)getLengthAsNumber();
	}

	public void setLength(Double length) {
		setLengthAsNumber(length);
	}
}
