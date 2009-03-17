package org.nexml.model;

public interface FloatEdge extends Edge {
	/**
	 * Gets the branch length as a double
	 * @return branch length
	 */
	Double getLength();

	/**
	 * Sets the branch length as a double
	 * @param branch length
	 */
	void setLength(Double length);
}
