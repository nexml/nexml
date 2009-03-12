package org.nexml.model.impl;

import org.nexml.model.IntEdge;

class IntEdgeImpl extends EdgeImpl implements IntEdge {
	
	public Integer getLength() {
		return (Integer)getLengthAsNumber();
	}

	public void setLength(Integer length) {
		setLengthAsNumber(length);
	}
}
