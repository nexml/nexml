package org.nexml.model;

public interface IntEdge extends Edge {
	/**
	 * Gets the branch length as an integer
	 * @return branch length
	 */	
	Integer getLength();

	/**
	 * Sets the branch length as an integer
	 * @param branch length
	 */	
	void setLength(Integer length);
}
