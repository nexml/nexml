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


/**
 * <otu>
 * <meta property="dc:name">Homer</meta>
 * <meta rel="dc:creator" href="http://www.example.com/Homer"/>
 * </otu>
 */
public interface Annotation extends Annotatable {
    
	/**
	 * Returns a namespaced predicate, e.g. dc:name
	 * @return a namespaced string
	 */
	public String getProperty();

	/**
	 * XXX how to define namespaces?
	 * Sets a CURIE predicate, e.g. dc:name
	 * @param property
	 */
	public void setProperty(String property);
	
	/**
	 * Sets a CURIE predicate when object is a URI
	 * @param rel
	 */
	public void setRel(String rel);
	
	/**
	 * Gets CURIE predicate when object is a URI
	 * @return
	 */
	public String getRel();

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
	public void setValue(Set<Annotation> value);
	public void setValue(Annotation value);
	public void setValue(Object value);	    
	public void setValue(NodeList value);
	public void setValue(Node value);
	public void setValue(Element value);	    
	public void setValue(URI value);
	public void setValue(Byte[] value);
	public void setValue(BigDecimal value);
	public void setValue(BigInteger value);   
	public void setValue(Boolean value);
	public void setValue(Byte value);
	public void setValue(Calendar value);   
	public void setValue(Date value);
	public void setValue(Double value);   
	public void setValue(Float value);
	public void setValue(Integer value);
	public void setValue(Long value);
	public void setValue(Short value);
	public void setValue(UUID value);
	public void setValue(String value);
	public void setValue(java.awt.Image value);
	public void setValue(Duration value);  
	public void setValue(QName value);
	public void setValue(Source value);  
	public void setValue(XMLGregorianCalendar value);

}
