package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.nexml.model.Node;
import org.nexml.model.Tree;
import org.w3c.dom.Document;

class IntTreeImpl extends TreeImpl<IntEdge> implements Tree<IntEdge>{

	public IntTreeImpl(Document document) {
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

	public void removeEdge(IntEdge edge) {
		// TODO Auto-generated method stub
		
	}

}
