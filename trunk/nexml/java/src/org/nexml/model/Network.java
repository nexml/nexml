package org.nexml.model;

import java.util.Set;

public interface Network<E extends Edge> extends NexmlWritable {
	E createEdge(Node source, Node target);

	void removeEdge(E edge);

	Set<E> getEdges();

	/**
	 * Create a new {@code Node}.
	 * 
	 * @return a new {@code Node}.
	 */
	Node createNode();

	/**
	 * Remove {@code node} from this {@code Network}.
	 * 
	 * @param node to be removed.
	 */
	void removeNode(Node node);

	/**
	 * Get an unmodifiable view of this {@code Network}'s {@code Node}s.
	 * 
	 * @return an unmodifiable view of this {@code Network}'s {@code Node}s.
	 */
	Set<Node> getNodes();

	/**
	 * Get those nodes that go into {@code target}, via some edge.
	 * 
	 * @param target see description.
	 * @return see description.
	 */
	Set<Node> getInNodes(Node target);

	/**
	 * Get those nodes that {@code source} goes into, via some edge.
	 * 
	 * @param target see description.
	 * @return see description.
	 */
	Set<Node> getOutNodes(Node source);
}
