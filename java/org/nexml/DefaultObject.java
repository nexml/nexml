package org.nexml;

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
	
	public String getNamespaceURI () {
		return this.namespaceURI;
	}
	
	public String getLocalName () {
		return this.localName;
	}
	
	public String getQName () {
		return this.qName;
	}
	
	public Attributes getAttributes () {
		return this.atts;
	}
	
	public void setCharacterData(char[] myCharacters) {
		this.characters = myCharacters;
	}
	
	public char[] getCharacterData() {
		return this.characters;
	}
}
