package org.nexml.model;

/**
 * A network that is a tree.
 * 
 * @param <E> the edge type.
 */
public interface Tree<E extends Edge> extends Network<E> {
	/**
	 * Return the root of the tree, or {@code null} if the tree has no root.
	 * 
	 * @return see description.
	 */
	Node getRoot();
}
