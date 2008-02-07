package org.nexml;

// $Id$

import org.xml.sax.Attributes;

/**
 * A factory is an object that creates other objects, a standard GoF design pattern. In this
 * interface, the factory creates objects based on the xml data (in particular, local and 
 * qualified element names, namespace, attributes and raw character data) provided to it by
 * the ElementHandler. 
 * @author rvosa
 * @see    ElementHandler
 */
public interface ObjectFactory {
	
	/**
	 * Creates a new object from the provided xml element and its attributes.
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified version of the element name (i.e. without prefix)
	 * @param qName        the qualified version of the element name (i.e. with prefix)
	 * @param atts         attributes associated with the element
	 * @return             an Object or "null"
	 */	
	public Object createObject (String namespaceURI, String localName, String qName, Attributes atts);
	
	/**
	 * Sets raw character data (e.g. sequence data) for the object currently being constructed.
	 * @param character an array of characters
	 */	
	public void setCharacterData (char[] character);
	
	/**
	 * Executes when the closing tag for an element is encountered, and the factory completes the object
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified version of the element name (i.e. without prefix)
	 * @param qName        the qualified version of the element name (i.e. with prefix)
	 * @param atts         the attributes associated with the element
	 * @see   Attributes
	 */	
	public void objectIsComplete (String namespaceURI, String localName, String qName, Attributes atts);
	
	/**
	 * Gets the object currently under construction.
	 * @return the object currently under construction
	 */	
	public Object getCurrentObject ();
	
	/**
	 * Gets the array of elements name the invocant factory can handle
	 * @return an array of local element names
	 */	
	public String[] getElementsToHandle ();
}
