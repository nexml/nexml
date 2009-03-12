package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.Edge;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;
import org.nexml.model.Tree;
import org.w3c.dom.Document;

public abstract class TreeImpl<E extends Edge> extends SetManager<NetworkObject> implements Tree<E> {

	public TreeImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	@Override
	String getTagName() {
		return "tree";
	}	
	
	abstract public E createEdge(Node source, Node target);

	public Node createNode() {
		NodeImpl node = new NodeImpl(getDocument());
		addThing(node);
		getElement().insertBefore(node.getElement(),getElement().getFirstChild());
		return node;
	}

	@SuppressWarnings("unchecked")
	public Set<E> getEdges() {
		Set<E> edges = new HashSet<E>();
		for (NetworkObject networkObject : getThings()) { 
			if (networkObject instanceof Edge) { 
				edges.add((E)networkObject);
			}
		}
		return edges;
	}

	public Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<Node>();
		for (NetworkObject networkObject : getThings()) { 
			if (networkObject instanceof Node) { 
				nodes.add((Node)networkObject);
			}
		}
		return nodes;
	}

	public void removeEdge(Edge edge) {
		removeThing(edge);
		getElement().removeChild(((EdgeImpl)edge).getElement());
	}

	public void removeNode(Node node) {
		removeThing(node);
		getElement().removeChild(((NodeImpl)node).getElement());
	}

}
