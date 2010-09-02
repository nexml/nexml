package org.nexml.model;

public interface Edge extends NetworkObject {
	/**
	 * Returns the source (parent?) node of the invocant edge
	 * @return a node object
	 */
	Node getSource();

	/**
	 * Sets the source (parent?) node of the invocant edge
	 */
	void setSource(Node source);

	/**
	 * Returns the target (child?) node of the invocant edge
	 * @return a node object
	 */
	Node getTarget();

	/**
	 * Sets the target (child?) node of the invocant edge
	 */
	void setTarget(Node target);
	
	/**
	 * Gets the edge length as a number
	 * @return an edge length
	 */
	Number getLength();
	
	/**
	 * Sets the edge length as a number
	 * @param an edge length
	 * 
	 */
	void setLength(Number length);
	
}
