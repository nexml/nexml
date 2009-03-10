package org.nexml.model.impl;

import org.nexml.model.FloatEdge;
import org.nexml.model.Network;

public class FloatNetworkImpl extends NetworkImpl<FloatEdge> implements Network<FloatEdge> {

	@Override
	public FloatEdge createEdge() {
		FloatEdge floatEdge = new FloatEdgeImpl(); 
		addThing(floatEdge);
		return floatEdge;
	}
}
