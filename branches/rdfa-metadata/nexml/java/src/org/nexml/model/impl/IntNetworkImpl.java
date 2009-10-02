package org.nexml.model.impl;

import org.nexml.model.IntEdge;
import org.nexml.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class IntNetworkImpl extends NetworkImpl<IntEdge> {

    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
     * @param document a DOM document object
     * @author rvosa
     */
	protected IntNetworkImpl(Document document) {
		super(document);
	}
	
    /**
     * Protected constructors are intended for recursive parsing, i.e.
     * starting from the root element (which maps onto DocumentImpl) we
     * traverse the element tree such that for every child element that maps
     * onto an Impl class the containing class calls that child's protected
     * constructor, passes in the element of the child. From there the 
     * child takes over, populates itself and calls the protected 
     * constructors of its children. These should probably be protected
     * because there is all sorts of opportunity for outsiders to call
     * these in the wrong context, passing in the wrong elements etc.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
	protected IntNetworkImpl(Document document, Element element, OTUsImpl otus) {
		super(document,element,otus);
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

	@Override
	protected IntEdge createEdge(Element element,Node source, Node target) {
		IntEdge edge = new IntEdgeImpl(getDocument(),element);
		edge.setSource(source);
		edge.setTarget(target);
		return edge;
	}
}
