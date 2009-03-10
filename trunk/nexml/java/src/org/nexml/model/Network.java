package org.nexml.model;

public interface Network<E extends Edge> extends NexmlWritable {
	E createEdge();

	void addEdge(E edge);
	
	E getEdge(String edgeId);
	
	void removeEdge(E edge);

	Node createNode();

	void addNode(Node node);
	
	Node getNode(String nodeId);
	
	void removeNode(Node node);
	
}
