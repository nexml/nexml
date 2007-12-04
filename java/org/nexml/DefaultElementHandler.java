package org.nexml;

// $Id$

import java.util.Hashtable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class DefaultElementHandler extends DefaultHandler implements ElementHandler {
	private Hashtable factoryByElement;
	private ObjectCache cache;
	private ObjectListener listener;
	private char[] characters;
	private String currentElement;
	
	DefaultElementHandler(){		
		this.factoryByElement = new Hashtable();
		this.listener = new DefaultObjectListener(); 
	}	
	
	/**
	 * Returns the last accumulated sequence of characters saved from the
	 * stream.
	 * @return an array of characters
	 */
	public char[] getLastCharacterSequence () {
		return this.characters;
	}
	
	/**
	 * Returns a factory that creates objects appropriate for a given
	 * element name.
	 * @param eltName an xml element name
	 * @return        the factory that creates objects from the specified element
	 */
	public ObjectFactory getFactoryForElementName(String eltName) {
		return (ObjectFactory)this.factoryByElement.get(eltName);
	}

	/**
	 * Sets a factory, i.e. an object that turns xml elements into 
	 * phylogenetic objects, for a given element name.
	 * @param eltName an xml element name
	 * @param fac     a factory that turns elements of the specified name into objects
	 */
	public void setFactoryForElementName(String eltName, ObjectFactory fac) {
		this.factoryByElement.put(eltName, fac);
	}
	
	/**
	 * Sets a factory, i.e. an object that turns xml elements into 
	 * phylogenetic objects, for a given list of element names.
	 * @param eltNames a list of xml element names
	 * @param fac      a factory that turns elements of the specified names into objects
	 */
	public void setFactoryForElementNames(String[] eltNames, ObjectFactory fac) {
		for ( int i = 0; i < eltNames.length; i++ ) {
			String eltName = eltNames[i];
			this.factoryByElement.put(eltName, fac);
		}
	}
	
	/**
	 * Sets the listener which will be notified of any new objects 
	 * (including "null", potentially) created by factories.
	 * @param myOl an object listener 
	 */
	public void setObjectListener(ObjectListener myOl) {
		this.listener = myOl;
	}
	
	/**
	 * Gets the listener which will be notified of any new objects 
	 * (including "null", potentially) created by factories.
	 * @return an object listener 
	 */	
	public ObjectListener getObjectListener () {
		return this.listener;
	}
	
	/**
	 * Starts document processing, is called when the sax stream commences.
	 */
	public void startDocument() {
		this.cache = new ObjectCache();
	}	
	
	/**
	 * Ends document processing, is called when the sax stream finishes.
	 */	
	public void endDocument(){
		this.cache = null;
	}	
	
	/**
	 * Executes when an opening element is encountered.
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified version of the element name (i.e. without prefix)
	 * @param qName        the qualified version of the element name (i.e. with prefix)
	 * @param atts         attributes associated with the element
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		this.currentElement = localName;
		ObjectFactory fac = this.getFactoryForElementName(localName);
		if ( fac == null ) {
			fac = new DefaultFactory();
			this.setFactoryForElementName(localName, fac);
		}
		Object obj = fac.createObject(namespaceURI, localName, qName, atts);
		//this.cache.setObject(obj, null, null);
		this.getObjectListener().newObjectNotification(obj);
	}	
	
	/**
	 * Executes when raw characters are encountered on the stream.
	 * @param ch     an array of characters
	 * @param start  the starting index in the stream
	 * @param length the length of the character array
	 */
	public void characters(char[] ch, int start, int length) {
		int j = 0;
		char[] segment = new char[length];
		for ( int i = start; i < start + length; i++ ) {
			segment[j] = ch[i];			
			j++;
		}
		this.characters = segment;
		ObjectFactory fac = this.getFactoryForElementName( this.currentElement );
		fac.setCharacterData(segment);
	}	
	
	/**
	 * Executes when a closing element is encountered.
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified version of the element name (i.e. without prefix)
	 * @param qName        the qualified version of the element name (i.e. with prefix)
	 */	
	public void endElement (String namespaceURI, String localName, String qName) {
		ObjectFactory fac = this.getFactoryForElementName(this.currentElement);
		fac.objectIsComplete(namespaceURI, localName, qName, null);		
		System.out.print("\n" + this.currentElement + ": |");
		System.out.print(this.characters);
		System.out.print("|\n");
		this.characters = null;
	}	
	
	/*	
	// These are other SAX handlers, which we don't implement at present 
	public void startPrefixMapping(String prefix, String uri) {
		
	}
		
	public void endPrefixMapping(String prefix) {
		
	}
	
	public void ignorableWhitespace(char[] ch, int start, int length) {
		
	}
	
	public void processingInstruction(String target, String data) {
		
	}
	
	public void setDocumentLocator(Locator locator) {
		
	}
	
	public void skippedEntity(String name) {
		
	}
	
	*/

}
