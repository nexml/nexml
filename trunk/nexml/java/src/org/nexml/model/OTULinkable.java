package org.nexml.model;

public interface OTULinkable extends NexmlWritable {
	/**
	 * Associates the invocant object with the provided otu
	 * @param otu
	 */
	void setOTU(OTU otu);
	
	/**
	 * Gets the otu (if any) associated with the invocant object
	 * @return an OTU object
	 */
	OTU getOTU();
}
