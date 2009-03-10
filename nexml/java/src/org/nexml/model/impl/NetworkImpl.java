package org.nexml.model.impl;

import org.nexml.model.Dictionary;
import org.nexml.model.Edge;
import org.nexml.model.Network;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;

public abstract class NetworkImpl<E extends Edge> extends SetManager<NetworkObject> implements Network<E> {

	public void addEdge(E edge) {
		// TODO Auto-generated method stub

	}

	public Dictionary createDictionary() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dictionary getDictionary() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDictionary(Dictionary dictionary) {
		// TODO Auto-generated method stub

	}

	public void setLabel(String label) {
		// TODO Auto-generated method stub
	}

	@Override
	String getTagName() {
		return "network";
	}

	public void addNode(Node node) {
		// TODO Auto-generated method stub

	}

	abstract public E createEdge();


	public Node createNode() {
		Node node = new NodeImpl();
		addThing(node);
		return node;
	}

	public E getEdge(String edgeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNode(String nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeEdge(E edge) {
		// TODO Auto-generated method stub

	}

	public void removeNode(Node node) {
		// TODO Auto-generated method stub

	}

}
