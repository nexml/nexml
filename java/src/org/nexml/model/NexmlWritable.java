package org.nexml.model;


/**
 * All {@code NexmlWritable}s are annotatable.
 */
public interface NexmlWritable {
	String getLabel();

	void setLabel(String pLabel);

	Dictionary getDictionary();
	
	void addDictionary(Dictionary pDictionary);
	
	
}
