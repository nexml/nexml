package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.FloatEdge;
import org.nexml.model.Node;
import org.nexml.model.Tree;
import org.w3c.dom.Document;

public class FloatTreeImpl extends TreeImpl<FloatEdge> implements Tree<FloatEdge> {

	public FloatTreeImpl(Document document) {
		super(document);
	}

	@Override
	public FloatEdge createEdge(Node source, Node target) {
		FloatEdgeImpl floatEdge = new FloatEdgeImpl(getDocument()); 
		addThing(floatEdge);
		getElement().appendChild(floatEdge.getElement());	
		floatEdge.setSource(source);
		floatEdge.setTarget(target);		
		return floatEdge;
	}

	/**
	 * XXX Why can't I just axe this method and let the removal
	 * be done by TreeImpl? I don't get generics. Sorry.
	 * @author rvosa
	 */
	public void removeEdge(FloatEdge edge) {
		removeEdge((Edge)edge);		
	}

}
