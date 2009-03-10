package org.nexml.model;

public interface Network extends NexmlWritable {
	Edge createEdge();

	void addEdge(Edge edge);
	
	Edge getEdge(String edgeId);
	
	void removeEdge(Edge edge);

	Node createNode();

	void addNode(Node node);
	
	Node getNode(String nodeId);
	
	void removeNode(Node node);
	
}
