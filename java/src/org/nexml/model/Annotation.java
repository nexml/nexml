package org.nexml.model;


/**
 * <otu>
 * <meta property="dc:name">Homer</meta>
 * <meta property="dc:creator" resource="http://www.example.com/Homer"/>
 * </otu>
 */
public interface Annotation extends NexmlWritable {
    
	public String getProperty();

	public void setProperty(String property);

	public Object getValue();

	public void setValue(Object value);

}
