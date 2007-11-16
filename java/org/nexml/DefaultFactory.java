package org.nexml;

import org.xml.sax.Attributes;

public class DefaultFactory implements ObjectFactory {
	private DefaultObject currentObject;
	public Object createObject(String namespaceURI, String localName, String qName, Attributes atts) {
		this.currentObject = new DefaultObject(namespaceURI, localName, qName, atts);
		return this.currentObject;
	}
	public void objectIsComplete(String namespaceURI, String localName, String qName, Attributes atts) {
		
	}
	public void setCharacterData(char[] characters) {
		this.currentObject.setCharacterData(characters);
	}
	public Object getCurrentObject () {
		return this.currentObject;
	}
	public String[] getElementsToHandle () {
		return null;
	}

}
