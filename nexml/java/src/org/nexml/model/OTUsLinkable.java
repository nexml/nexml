package org.nexml.model;

/**
 * An object that can be linked to an {@code}s.
 */
public interface OTUsLinkable extends NexmlWritable {

	/**
	 * Get the {@code OTUs} to which this object is linked.
	 * 
	 * @return see description.
	 */
	OTUs getOTUs();

	/**
	 * Set the {@code OTUs} to which this object is linked.
	 * 
	 * @param otus see description.
	 */
	void setOTUs(OTUs otus);
}
