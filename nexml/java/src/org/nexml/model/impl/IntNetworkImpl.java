package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.nexml.model.Node;
import org.w3c.dom.Document;

class IntNetworkImpl extends NetworkImpl<IntEdge> {

	public IntNetworkImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IntEdge createEdge(Node source, Node target) { 
		IntEdgeImpl intEdge = new IntEdgeImpl(getDocument());
		addThing(intEdge);
		getElement().appendChild(intEdge.getElement());
		intEdge.setSource(source);
		intEdge.setTarget(target);		
		return intEdge;
	}
}
