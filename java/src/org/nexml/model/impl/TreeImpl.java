package org.nexml.model.impl;

import org.nexml.model.Edge;
import org.nexml.model.Node;
import org.nexml.model.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class TreeImpl<E extends Edge> extends NetworkImpl<E> implements
		Tree<E> {

	/**
	 * Get a tree's NeXML tag name.
	 * 
	 * @return tree's NeXML tag name.
	 */
	static String getTagNameClass() {
		return "tree";
	}

	/**
	 * Protected constructors that take a DOM document object but not an element
	 * object are used for generating new element nodes in a NeXML document. On
	 * calling such constructors, a new element is created, which can be
	 * retrieved using getElement(). After this step, the Impl class that called
	 * this constructor would still need to attach the element in the proper
	 * location (typically as a child element of the class that called the
	 * constructor).
	 * 
	 * @param document a DOM document object
	 * @author rvosa
	 */
	protected TreeImpl(Document document) {
		super(document);
	}

	/**
	 * Protected constructors are intended for recursive parsing, i.e. starting
	 * from the root element (which maps onto DocumentImpl) we traverse the
	 * element tree such that for every child element that maps onto an Impl
	 * class the containing class calls that child's protected constructor,
	 * passes in the element of the child. From there the child takes over,
	 * populates itself and calls the protected constructors of its children.
	 * These should probably be protected because there is all sorts of
	 * opportunity for outsiders to call these in the wrong context, passing in
	 * the wrong elements etc.
	 * 
	 * @param document the containing DOM document object. Every Impl class
	 *            needs a reference to this so that it can create DOM element
	 *            objects
	 * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's the
	 *            <otus/> element)
	 * @author rvosa
	 */
	protected TreeImpl(Document document, Element element, OTUsImpl otus) {
		super(document, element, otus);
		/*
		 * for ( Element nodeElement : getChildrenByTagName(element,"node") ) {
		 * String otuId = nodeElement.getAttribute("otu"); Node node = new
		 * NodeImpl(document,nodeElement); if ( ! otuId.equals("") ) { OTU otu =
		 * otus.getThingById(otuId); node.setOTU(otu); } addThing(node); } for (
		 * Element edgeElement : getChildrenByTagName(element,"edge") ) { Edge
		 * edge = createEdge(edgeElement); String sourceId =
		 * edgeElement.getAttribute("source"); String targetId =
		 * edgeElement.getAttribute("target"); Node source =
		 * (Node)getThingById(sourceId); Node target =
		 * (Node)getThingById(targetId); edge.setSource(source);
		 * edge.setTarget(target); addThing(edge); }
		 */
	}

	/** {@inheritDoc} */
	public Node getRoot() {
		for (Node node : getNodes()) {
			if (node.isRoot()) {
				return node;
			}
		}
		return null;
	}

}
