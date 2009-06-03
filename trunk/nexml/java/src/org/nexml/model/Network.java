package org.nexml.model;

import java.util.Set;

public interface Network<E extends Edge> extends Annotatable {
	/**
	 * Creates a new edge object, paramerized by branch length
	 * type. As edges require a source and a target, this must
	 * be provided here (though they can be changed later).
	 * @param source
	 * @param target
	 * @return
	 */
	E createEdge(Node source, Node target);

	/**
	 * Removes an edge object.
	 * XXX Does this mean collapse?
	 * @param edge
	 */
	void removeEdge(E edge);

	/**
	 * Gets all the edges in the network
	 */
	Set<E> getEdges();

	/**
	 * Create a new {@code Node}.
	 * 
	 * @return a new {@code Node}.
	 */

	Node createNode();

	/**
	 * Removes a node from the network
	 * XXX does this mean collapse?
	 * @param node
	 */

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
	 * @param source see description.
	 * @return see description.
	 */
	Set<Node> getOutNodes(Node source);
	
	Edge getEdge(Node source, Node target);
}
