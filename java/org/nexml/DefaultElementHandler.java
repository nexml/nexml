package org.nexml;

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
	
	public char[] getLastCharacterSequence () {
		return this.characters;
	}
	
	public ObjectFactory getFactoryForElementName(String eltName) {
		return (ObjectFactory)this.factoryByElement.get(eltName);
	}

	public void setFactoryForElementName(String eltName, ObjectFactory fac) {
		this.factoryByElement.put(eltName, fac);
	}
	
	public void setFactoryForElementNames(String[] eltNames, ObjectFactory fac) {
		for ( int i = 0; i < eltNames.length; i++ ) {
			String eltName = eltNames[i];
			this.factoryByElement.put(eltName, fac);
		}
	}
	
	public void setObjectListener(ObjectListener myOl) {
		this.listener = myOl;
	}
	
	public ObjectListener getObjectListener () {
		return this.listener;
	}
	
	public void startDocument() {
		this.cache = new ObjectCache();
	}	
	
	public void endDocument(){
		this.cache = null;
	}	
	
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
	
	public void characters(char[] ch, int start, int length) {
		//System.out.print( this.currentElement + ": -->|");
		int j = 0;
		char[] segment = new char[length];
		for ( int i = start; i < start + length; i++ ) {
			//System.out.print(ch[i]);
			segment[j] = ch[i];			
			j++;
		}
		this.characters = segment;
		//System.out.print("|<--");
		ObjectFactory fac = this.getFactoryForElementName( this.currentElement );
		fac.setCharacterData(segment);
	}	
	
	public void endElement (String namespaceURI, String localName, String qName) {
		ObjectFactory fac = this.getFactoryForElementName(this.currentElement);
		fac.objectIsComplete(namespaceURI, localName, qName, null);		
		System.out.print("\n" + this.currentElement + ": |");
		System.out.print(this.characters);
		System.out.print("|\n");
		this.characters = null;
	}	
	
	/*	
	
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
