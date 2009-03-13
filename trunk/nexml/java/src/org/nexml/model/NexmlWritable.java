package org.nexml.model;

public interface NexmlWritable {
	String DEFAULT_NAMESPACE = "http://www.nexml.org/1.0";

	String getLabel();

	void setLabel(String label);
	
	String getId();
	
}
