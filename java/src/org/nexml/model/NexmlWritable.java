package org.nexml.model;

/**
 * All are annotatable.
 * 
 * @author Sam Donnelly
 */
public interface NexmlWritable {
	String getId();
	void setId(String pId);

	String getLabel();
	void setLabel(String pLabel);

	String getTagName();
	void setTagName(String pTagName);
}
