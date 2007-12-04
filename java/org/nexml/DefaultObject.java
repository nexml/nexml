package org.nexml;

// $Id$

import java.util.Hashtable;
import org.xml.sax.Attributes;

/**
 * The nexml class libraries implement an extensible system for parsing nexml data
 * in java. The data is processed as a SAX stream, from which a handler dispatches
 * elements and their attributes to object factories, which will instantiate objects
 * based on the contents of the attributes. Typically, these objects would be those
 * used by a toolkit such as mesquite. However, if no suitable factory was found for
 * a given element name a simple default object is created which holds the xml data
 * (element name in local and fully qualified form, namespace, attributes). This
 * class is the implementation of that simple object.
 * @author rvosa
 * @see    Attributes
 */
public class DefaultObject implements NexmlWritable {
	private String namespaceURI;
	private String localName;
	private String qName;
	private Attributes atts;
	private char[] characters;
	private String nex = "http://www.nexml.org/1.0";
	private Hashtable[] dictionaries;
	private NexmlWritable[] containedObjects;
	private NexmlWritable referencedObject;
	
	/**
	 * Constructs a new DefaultObject from element and attributes of the stream
	 * @param mynamespaceURI a universal resource identifier, typically http://www.nexml.org/1.0
	 * @param mylocalName    the local (unqualified, without prefix) name of the element
	 * @param myqName        the fully qualified (with prefix) name of the element
	 * @param myatts         the attributes of the element
	 */
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
	 * @param myCharacters an array of characters
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
	
	/**
	 * Gets the nexml id attribute's value, a unique identifier in
	 * block scope
	 * @return an identifier
	 */
	public String getId () {
		return this.atts.getValue( nex, "id" );
	}
	
	/**
	 * Gets the nexml label attribute's value, a human readable name
	 * for the element (has no structural implications, may be null)
	 * @return a label
	 */
	public String getLabel() {
		return this.atts.getValue( nex,"label" );
	}
	
	/**
	 * Gets the association nexml plist dictionaries as parameterized 
	 * hash tables
	 * @return an array of hashtables
	 */
	public Hashtable[] getDictionaries() {
		return this.dictionaries;
	}
	
	/**
	 * Gets the objects contained by segmented objects. For example,
	 * if the invocant is a taxa block, this returns an array of
	 * taxon objects.
	 * @return an array of NexmlWritable objects
	 */
	public NexmlWritable[] getContainedObjects() {
		return this.containedObjects;
	}
	
	/**
	 * Gets the referenced object. For example, if the invocant is a
	 * trees block, which links to a taxa block through its "otus" id 
	 * reference, this returns that referenced taxa block.
	 * @return a NexmlWritable object
	 */
	public NexmlWritable getReferencedObject(){
		return this.referencedObject;
	}
}
