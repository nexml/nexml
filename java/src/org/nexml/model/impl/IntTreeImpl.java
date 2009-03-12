package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.IntEdge;
import org.nexml.model.Node;
import org.nexml.model.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class IntTreeImpl extends TreeImpl<IntEdge> implements Tree<IntEdge> {

	public IntTreeImpl(Document document) {
		super(document);
	}

	public IntTreeImpl(Document document, Element element) {
		super(document, element);
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

	/**
	 * XXX Why can't I just axe this method and let the removal
	 * be done by TreeImpl? I don't get generics. Sorry.
	 * @author rvosa
	 */	
	public void removeEdge(IntEdge edge) {
		removeEdge((Edge)edge);
	}

}
