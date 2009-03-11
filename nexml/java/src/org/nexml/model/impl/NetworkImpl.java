package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.Edge;
import org.nexml.model.Network;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;

public abstract class NetworkImpl<E extends Edge> extends SetManager<NetworkObject> implements Network<E> {


	@Override
	String getTagName() {
		return "network";
	}

	abstract public E createEdge();

	public Node createNode() {
		Node node = new NodeImpl();
		addThing(node);
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
	
	public void removeEdge(E edge) {
		removeThing(edge);
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
	
	
	public void removeNode(Node node) {
		removeThing(node);
	}

}
