package org.nexml.model;

public interface Node extends NetworkObject, OTULinkable { 
	/**
	 * Gets the value of the root attribute. This indicates
	 * whether a tree is actually considered rooted, as opposed 
	 * to the rootedness imposed by the element structure. 
	 * XXX note that this attribute can occur multiple times
	 * in a tree. Perhaps this means multiple rootings?
	 * @return
	 */
	boolean isRoot();
	
	/**
	 * Sets the value of the root attribute. This indicates
	 * whether a tree is actually considered rooted, as opposed 
	 * to the rootedness imposed by the element structure. 
	 * XXX note that this attribute can occur multiple times
	 * in a tree. Perhaps this means multiple rootings?
	 */
	void setRoot(boolean isRoot);
}
