package org.nexml.model;

public interface OTULinkable extends NexmlWritable {
	void setOTU(OTU otu);
	
	OTU getOTU();
}
