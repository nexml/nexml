package org.nexml;

// $Id$

import org.xml.sax.Attributes;

/**
 * In the nexml api factories are objects that create other objects from xml
 * elements. For example, an object might turn an "otu" element and its attributes
 * into a mesquite taxon object. The interface for doing this is defined by
 * ObjectFactory. This class is a concrete implementation of that interface,
 * used by default, when the user of the nexml class libraries doesn't provide
 * her own factories. 
 * @author rvosa
 * @see    ObjectFactory
 */
public class DefaultFactory implements ObjectFactory {
	private DefaultObject currentObject;
	
	/**
	 * Creates a new object from the provided xml element and its attributes.
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified version of the element name (i.e. without prefix)
	 * @param qName        the qualified version of the element name (i.e. with prefix)
	 * @param atts         attributes associated with the element
	 * @return             an Object or "null"
	 */
	public Object createObject(String namespaceURI, String localName, String qName, Attributes atts) {
		this.currentObject = new DefaultObject(namespaceURI, localName, qName, atts);
		return this.currentObject;
	}
	
	/**
	 * Executes when the closing tag for an element is encountered, and the facture completes the object
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified version of the element name (i.e. without prefix)
	 * @param qName        the qualified version of the element name (i.e. with prefix)
	 */
	public void objectIsComplete(String namespaceURI, String localName, String qName, Attributes atts) {
		
	}
	
	/**
	 * Sets raw character data (e.g. sequence data) for the object currently being constructed.
	 * @param characters an array of characters
	 */
	public void setCharacterData(char[] characters) {
		this.currentObject.setCharacterData(characters);
	}
	
	/**
	 * Gets the object currently under construction.
	 * @return the object currently under construction
	 */
	public Object getCurrentObject () {
		return this.currentObject;
	}
	
	/**
	 * Gets the array of elements name the invocant factory can handle
	 * @return an array of local element names
	 */
	public String[] getElementsToHandle () {
		return null;
	}

}
