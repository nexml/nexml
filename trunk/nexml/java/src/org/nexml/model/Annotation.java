package org.nexml.model;


/**
 * <otu>
 * <meta property="dc:name">Homer</meta>
 * <meta property="dc:creator" resource="http://www.example.com/Homer"/>
 * </otu>
 */
public interface Annotation extends NexmlWritable {
    
	/**
	 * Returns a namespaced predicate, e.g. dc:name
	 * @return a namespaced string
	 */
	public String getProperty();

	/**
	 * XXX how to define namespaces?
	 * Sets a namespaced predicate, e.g. dc:name
	 * @param property
	 */
	public void setProperty(String property);

	/**
	 * From a triple (subject, predicate, object) returns
	 * the object. Could be a string literal, or a resource
	 * identifier, or an encapsulation of a more complex 
	 * structure (e.g. recursively nested annotation objects,
	 * other xml, rdf/xml).
	 * @return
	 */
	public Object getValue();

	/**
	 * From a triple (subject, predicate, object) sets
	 * the object. Could be a string literal, or a resource
	 * identifier, or an encapsulation of a more complex 
	 * structure (e.g. recursively nested annotation objects,
	 * other xml, rdf/xml).
	 * @param value
	 */
	public void setValue(Object value);

}
