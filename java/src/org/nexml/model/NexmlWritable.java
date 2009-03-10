package org.nexml.model;


/**
 * All {@code NexmlWritable}s are annotatable.
 */
public interface NexmlWritable {
	String getLabel();

	void setLabel(String label);

	Dictionary getDictionary();
	
	void addDictionary(Dictionary dictionary);
	
	
}
