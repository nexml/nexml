package org.nexml.model.impl;

import org.nexml.model.IntEdge;

public class IntNetworkImpl extends NetworkImpl<IntEdge> {

	@Override
	public IntEdge createEdge() { 
		IntEdge intEdge = new IntEdgeImpl();
		addThing(intEdge);
		return intEdge;
	}
}
