package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.Edge;
import org.nexml.model.Network;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class NetworkImpl<E extends Edge> extends SetManager<NetworkObject> implements Network<E> {

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
	protected NetworkImpl(Document document) {
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
	protected NetworkImpl(Document document,Element element, OTUsImpl otus) {
		super(document, element);
		for (Element nodeElement : getChildrenByTagName(element, "node")) {
			String otuId = nodeElement.getAttribute("otu");
			Node node = new NodeImpl(document, nodeElement);
			if (!otuId.equals("")) {
				OTU otu = otus.getThingById(otuId);
				node.setOTU(otu);
			}
			addThing(node);
		}
		for (Element edgeElement : getChildrenByTagName(element, "edge")) {
			String sourceId = edgeElement.getAttribute("source");
			String targetId = edgeElement.getAttribute("target");
			Node source = (Node) getThingById(sourceId);
			Node target = (Node) getThingById(targetId);
			addThing(createEdge(edgeElement, source, target));
		}
	}

	/** {@inheritDoc} */
	@Override
	String getTagName() {
		return getTagNameClass();
	}

	static String getTagNameClass() {
		return "network";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nexml.model.Network#createEdge(org.nexml.model.Node,
	 * org.nexml.model.Node)
	 */
	abstract public E createEdge(Node source, Node target);

	abstract protected E createEdge(Element element, Node source, Node target);

	/**
	 * This method creates a node element. Because node elements come before
	 * edge elements, the node is prepended to all other elements in the tree.
	 * XXX This is problematic once we start adding annotations.
	 * 
	 * @author rvosa
	 */
	public Node createNode() {
		NodeImpl node = new NodeImpl(getDocument());
		addThing(node);
		getElement().insertBefore(node.getElement(),
				getElement().getFirstChild());
		return node;
	}

	/** {@inheritDoc} */
	public Set<E> getEdges() {
		Set<E> edges = new HashSet<E>();
		for (NetworkObject networkObject : getThings()) {
			if (networkObject instanceof Edge) {
				@SuppressWarnings("unchecked")
				E edge = (E) networkObject;
				edges.add(edge);
			}
		}
		return edges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nexml.model.Network#removeEdge(org.nexml.model.Edge)
	 */
	public void removeEdge(E edge) {
		removeThing(edge);
		getElement().removeChild(((EdgeImpl) edge).getElement());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nexml.model.Network#getNodes()
	 */
	public Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<Node>();
		for (NetworkObject networkObject : getThings()) {
			if (networkObject instanceof Node) {
				nodes.add((Node) networkObject);
			}
		}
		return nodes;
	}

	/**
	 * XXX need to cast Node to NodeImpl here because we also remove the
	 * equivalent node element.
	 */
	public void removeNode(Node node) {
		removeThing(node);
		getElement().removeChild(((NodeImpl) node).getElement());
		// TODO: need to keep our tree connected. Right now it just deletes all
		// of the edges that were connected to it so we could end up with a
		// disconnected network.
		for (E edge : getEdges()) {
			if (node.equals(edge.getSource()) || node.equals(edge.getTarget())) {
				removeEdge(edge);
			}
		}
	}

	/** {@inheritDoc} */
	public Set<Node> getInNodes(Node target) {
		Set<Node> sourceNodes = new HashSet<Node>();
		for (Edge edge : getEdges()) {
			if (edge.getTarget().equals(target)) {
				sourceNodes.add(edge.getSource());
			}
		}
		return sourceNodes;
	}

	/** {@inheritDoc} */
	public Set<Node> getOutNodes(Node source) {
		Set<Node> targetNodes = new HashSet<Node>();
		for (Edge edge : getEdges()) {
			if (edge.getSource().equals(source)) {
				targetNodes.add(edge.getTarget());
			}
		}
		return targetNodes;
	}
	
	public Edge getEdge(Node source, Node target) {
		for (Edge edge : getEdges()) {
			if ( edge.getSource().equals(source) && edge.getTarget().equals(target) ) {
				return edge;
			}
		}
		return null;
	}
}
