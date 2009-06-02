package org.nexml.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
    
    Set<Object> getRelValues(String rel);
    
    Set<Annotation> getAnnotations(String rel);
    
    Set<Annotation> getAllAnnotations();
    
    /**
     * Sets a single key/value pair. 
     * @param property a predicate as a namespaced string
     * @param value semantically, an object
     */
    void addAnnotationValue(String property, URI nameSpaceURI, Set<Annotation> value);
    void addAnnotationValue(String property, URI nameSpaceURI, Object value);
	void addAnnotationValue(String property, URI nameSpaceURI, NodeList value);	    
	void addAnnotationValue(String property, URI nameSpaceURI, Element value);	    
	void addAnnotationValue(String property, URI nameSpaceURI, URI value);
	void addAnnotationValue(String property, URI nameSpaceURI, Byte[] value);
	void addAnnotationValue(String property, URI nameSpaceURI, BigDecimal value);
	void addAnnotationValue(String property, URI nameSpaceURI, BigInteger value);   
	void addAnnotationValue(String property, URI nameSpaceURI, Boolean value);
	void addAnnotationValue(String property, URI nameSpaceURI, Byte value);
	void addAnnotationValue(String property, URI nameSpaceURI, Calendar value);   
	void addAnnotationValue(String property, URI nameSpaceURI, Date value);
	void addAnnotationValue(String property, URI nameSpaceURI, Double value);   
	void addAnnotationValue(String property, URI nameSpaceURI, Float value);
	void addAnnotationValue(String property, URI nameSpaceURI, Integer value);
	void addAnnotationValue(String property, URI nameSpaceURI, Long value);
	void addAnnotationValue(String property, URI nameSpaceURI, Short value);
	void addAnnotationValue(String property, URI nameSpaceURI, UUID value);
	void addAnnotationValue(String property, URI nameSpaceURI, String value);
	void addAnnotationValue(String property, URI nameSpaceURI, java.awt.Image value);
	void addAnnotationValue(String property, URI nameSpaceURI, Duration value);  
	void addAnnotationValue(String property, URI nameSpaceURI, QName value);
	void addAnnotationValue(String property, URI nameSpaceURI, Source value);  
	void addAnnotationValue(String property, URI nameSpaceURI, XMLGregorianCalendar value);    

}
