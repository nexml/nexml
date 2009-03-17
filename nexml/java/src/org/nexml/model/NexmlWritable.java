package org.nexml.model;

public interface NexmlWritable {
	String DEFAULT_NAMESPACE = "http://www.nexml.org/1.0";

	/**
	 * Gets the value of the label attribute. This is simply
	 * a human readable name, with no structural implications
	 * (e.g. can be absent, can be identical for different
	 * elements, etc.)
	 * @return a string, or null
	 */
	String getLabel();

	/**
	 * Sets the value of the label attribute. This is simply
	 * a human readable name, with no structural implications
	 * (e.g. can be absent, can be identical for different
	 * elements, etc.)
	 * @param label a human readable label
	 */	
	void setLabel(String label);
	
	/**
	 * Gets the value of the id attribute. This must be a
	 * string of type NCName, i.e. a string that matches
	 * ^[a-zA-Z_][a-zA-Z0-9_\-]*$ 
	 * @return
	 */
	String getId();
	
}
