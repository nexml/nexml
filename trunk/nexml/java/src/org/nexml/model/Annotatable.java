package org.nexml.model;

import java.util.Set;

public interface Annotatable extends NexmlWritable {
    
	/**
	 * For a single annotation property (i.e. a predicate),
	 * multiple annotation values may be returned. These
	 * could be literal text content of a meta element,
	 * a resource identifier (e.g. a url defined by the href
	 * attribute value), and/or recursively nested meta 
	 * annotations or other xml. This method returns all
	 * of these.
	 * @param property
	 * @return
	 */
    Set<Object> getAnnotationValues(String property);
    
    /**
     * Sets a single key/value pair. 
     * @param property a predicate as a namespaced string
     * @param value semantically, an object
     */
    void addAnnotationValue(String property, Object value);

}
