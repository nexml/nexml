package org.nexml.model.impl;

import org.nexml.model.FloatEdge;
import org.nexml.model.Node;
import org.nexml.model.Tree;
import org.w3c.dom.Document;

public class FloatTreeImpl extends TreeImpl<FloatEdge> implements Tree<FloatEdge> {

	public FloatTreeImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FloatEdge createEdge(Node source, Node target) {
		FloatEdge floatEdge = new FloatEdgeImpl(getDocument()); 
		addThing(floatEdge);
		getElement().appendChild(floatEdge.getElement());	
		floatEdge.setSource(source);
		floatEdge.setTarget(target);		
		return floatEdge;
	}

	public void removeEdge(FloatEdge edge) {
		// TODO Auto-generated method stub
		
	}

}
