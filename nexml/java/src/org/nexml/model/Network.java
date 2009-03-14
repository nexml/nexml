package org.nexml.model;

import java.util.Set;

public interface Network<E extends Edge> extends NexmlWritable {
	E createEdge(Node source, Node target);

	void removeEdge(E edge);

	Set<E> getEdges();

	Node createNode();

	void removeNode(Node node);

	Set<Node> getNodes();

	/**
	 * Get those nodes that go into {@code target}.
	 * 
	 * @param target see description.
	 * @return see description.
	 */
	Set<Node> getInNodes(Node target);

	/**
	 * Get those nodes that {@code source} goes into.
	 * 
	 * @param target see description.
	 * @return see description.
	 */
	Set<Node> getOutNodes(Node source);
}
