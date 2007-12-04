package org.nexml;

// $Id$

import org.xml.sax.Attributes;

public class DefaultObject {
	private String namespaceURI;
	private String localName;
	private String qName;
	private Attributes atts;
	private char[] characters;
	
	public DefaultObject(String mynamespaceURI, String mylocalName, String myqName, Attributes myatts) {		
		this.namespaceURI = mynamespaceURI;
		this.localName = mylocalName;
		this.qName = myqName;
		this.atts = myatts;
	}
	
	/**
	 * Gets the universal resource identifier of the object
	 * @return a string of the URI, typically http://www.nexml.org/1.0
	 */
	public String getNamespaceURI () {
		return this.namespaceURI;
	}
	
	/**
	 * Gets the local xml element name of the object
	 * @return a local xml element name
	 */
	public String getLocalName () {
		return this.localName;
	}
	
	/**
	 * Gets the fully qualified element name of the object (i.e. with prefix)
	 * @return a fully qualified xml element name
	 */
	public String getQName () {
		return this.qName;
	}
	
	/**
	 * Gets the attributes associated with the object
	 * @return an Attributes object
	 */
	public Attributes getAttributes () {
		return this.atts;
	}
	
	/**
	 * Sets the raw character data (e.g. sequence data) for the object
	 * @param an array of characters
	 */
	public void setCharacterData(char[] myCharacters) {
		this.characters = myCharacters;
	}
	
	/**
	 * Gets the raw character data (e.g. sequence data) of the object
	 * @return an array of characters
	 */
	public char[] getCharacterData() {
		return this.characters;
	}
}
