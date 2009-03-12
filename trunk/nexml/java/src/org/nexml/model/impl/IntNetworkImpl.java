package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.nexml.model.Node;
import org.w3c.dom.Document;

class IntNetworkImpl extends NetworkImpl<IntEdge> {

	public IntNetworkImpl(Document document) {
		super(document);
	}

	/**
	 * This creates an edge element. Because edge elements
	 * require source and target attributes, these need to
	 * be passed in here.
	 * @author rvosa
	 */
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
