package org.nexml.model.impl;

import java.lang.annotation.Inherited;
import java.util.HashSet;
import java.util.Set;

import org.nexml.model.Edge;
import org.nexml.model.Network;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class NetworkImpl<E extends Edge> extends SetManager<NetworkObject>
		implements Network<E> {

	public NetworkImpl(Document document) {
		super(document);
	}

	public NetworkImpl(Document document, Element element) {
		super(document, element);
	}

	@Override
	String getTagName() {
		return "network";
	}

	abstract public E createEdge(Node source, Node target);

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

	public void removeEdge(E edge) {
		removeThing(edge);
		getElement().removeChild(((EdgeImpl) edge).getElement());
	}

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

}
