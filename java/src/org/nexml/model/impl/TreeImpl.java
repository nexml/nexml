package org.nexml.model.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nexml.model.Edge;
import org.nexml.model.NetworkObject;
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
	protected TreeImpl(Document document,Element element, OTUsImpl otus) {
		super(document,element,otus);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}
	
	//abstract protected E createEdge(Element element,Node source, Node target);

	abstract public E createEdge(Node source, Node target);
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Network#createNode()
	 */
	public Node createNode() {
		NodeImpl node = new NodeImpl(getDocument());
		addThing(node);
		List<Element> edgeList = getChildrenByTagName(getElement(),"edge");
		List<Element> rootEdgeList = getChildrenByTagName(getElement(),"rootedge");
		if ( rootEdgeList.size() > 0 ) { 
			getElement().insertBefore(node.getElement(),rootEdgeList.get(0));			
		}
		else if ( edgeList.size() > 0 ) {
			getElement().insertBefore(node.getElement(),edgeList.get(0));
		}
		else {		
			getElement().appendChild(node.getElement());
		}
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Network#getEdges()
	 */
	@SuppressWarnings("unchecked")
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

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Network#removeEdge(org.nexml.model.Edge)
	 */
	public void removeEdge(E edge) {
		removeThing(edge);
		getElement().removeChild(((EdgeImpl) edge).getElement());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Network#removeNode(org.nexml.model.Node)
	 */
	public void removeNode(Node node) {
		removeThing(node);
		getElement().removeChild(((NodeImpl) node).getElement());
		// TODO: need to keep our tree connected.
		for (E edge : getEdges()) {
			if (node.equals(edge.getSource()) || node.equals(edge.getTarget())) {
				removeEdge(edge);
			}
		}
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
