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
import org.w3c.dom.Node;

public interface Annotatable extends NexmlWritable {
    
	/**
	 * Assuming the object this method is called on (i.e. the invocant)
	 * is the subject of multiple triples that have the same predicate,
	 * this method returns the objects of these triples.
	 * 
	 * These could be literal text content of a meta element,
	 * a resource identifier (e.g. a url defined by the href
	 * attribute value), and/or recursively nested meta 
	 * annotations or other xml. This method returns all
	 * of these.
	 * @param property
	 * @return
	 */
    Set<Object> getAnnotationValues(String property);
    
    /**
     * Similar to getAnnotationValues, but here the triples use rel
     * attributes instead of property attributes for predicates. The
     * values in this case are either resources (URLs) or opaque XML.
     * 
     * @param rel
     * @return
     */
    Set<Object> getRelValues(String rel);
    
    /**
     * Returns all annotations whose predicate (property or rel attribute)
     * match the argument.
     * @param rel
     * @return
     */
    Set<Annotation> getAnnotations(String rel);
    
    /**
     * Gets all annotations attached to the object.
     * @return
     */
    Set<Annotation> getAllAnnotations();
    
    /**
     * Gets all attached annotations whose predicate (property or rel
     * attribute) are defined in the provided namespace.
     * @param uri
     * @return
     */
    Set<Annotation> getAllAnnotationsForURI(URI uri);
    
    /**
     * Sets a single key/value pair. 
     * @param property a predicate as a namespaced string
     * @param value semantically, an object
     */
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Set<Annotation> value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Object value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, NodeList value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Node value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Element value);	    
    Annotation addAnnotationValue(String property, URI nameSpaceURI, URI value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Byte[] value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, BigDecimal value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, BigInteger value);   
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Boolean value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Byte value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Calendar value);   
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Date value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Double value);   
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Float value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Integer value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Long value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Short value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, UUID value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, String value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, java.awt.Image value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Duration value);  
    Annotation addAnnotationValue(String property, URI nameSpaceURI, QName value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, Source value);  
    Annotation addAnnotationValue(String property, URI nameSpaceURI, XMLGregorianCalendar value);
    Annotation addAnnotationValue(String property, URI nameSpaceURI, java.lang.Character value);

}
