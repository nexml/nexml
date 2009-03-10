package org.nexml.model.impl;

import org.nexml.model.Dictionary;
import org.nexml.model.Edge;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;
import org.nexml.model.Tree;

public class NetworkImpl extends SetManager<NetworkObject> implements Tree {

	public void addEdge(Edge edge) {
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

	public Edge createEdge() {
		Edge edge = new EdgeImpl();
		addThing(edge);
		return edge;
	}

	public Node createNode() {
		Node node = new NodeImpl();
		addThing(node);
		return node;
	}

	public Edge getEdge(String edgeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNode(String nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeEdge(Edge edge) {
		// TODO Auto-generated method stub

	}

	public void removeNode(Node node) {
		// TODO Auto-generated method stub

	}

}
