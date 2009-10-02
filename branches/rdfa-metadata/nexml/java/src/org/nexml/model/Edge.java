package org.nexml.model;

public interface Edge extends NetworkObject {
	/**
	 * Returns the source (parent?) node of the invocant edge
	 * @return a node object
	 */
	Node getSource();

	/**
	 * Sets the source (parent?) node of the invocant edge
	 * @return a node object
	 */
	void setSource(Node source);

	/**
	 * Returns the target (child?) node of the invocant edge
	 * @return a node object
	 */
	Node getTarget();

	/**
	 * Sets the target (child?) node of the invocant edge
	 * @return a node object
	 */
	void setTarget(Node target);
	
}
