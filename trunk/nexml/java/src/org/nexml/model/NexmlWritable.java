package org.nexml.model;

import java.net.URI;

public interface NexmlWritable {
	String DEFAULT_NAMESPACE = "http://www.nexml.org/2009";
	String DEFAULT_VERSION = "0.9";

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
	
	/**
	 * Sets the value of the id attribute. This must be a
	 * string of type NCName, i.e. a string that matches
	 * ^[a-zA-Z_][a-zA-Z0-9_\-]*$
	 * 
	 *  NOTE: unless you have specific reason to want to
	 *  change the default auto-generated identifiers
	 *  there is no need to use this method ever.
	 * @return
	 */	
	void setId(String id);

	/**
	 * Sets the xml:base attribute for the focal element.
	 * This facility is used to construct relative URLs 
	 * other than those computed from the location of the
	 * document.
	 * @param baseURI
	 */
	void setBaseURI(URI baseURI);
	
	/**
	 * Gets the xml:base attribute.
	 * @return base URI
	 */
	URI getBaseURI();	
	
}
