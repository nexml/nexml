package org.nexml;
import java.util.Hashtable;
import org.xml.sax.Attributes;

/**
 * Objects that implement this interface can be written 
 * to nexml by the NexmlWriter.
 * @author rvosa
 * @see NexmlWriter
 */
public interface NexmlWritable {
	
	/**
	 * Gets the id attribute for the element.
	 * @return the nexml id attribute's value
	 */
	public String getId();
	
	/**
	 * Gets the (optional) label attribute for the element.
	 * @return the nexml label attribute's value
	 */
	public String getLabel();
	
	/**
	 * Gets the local name of the element.
	 * @return a local (without prefix) nexml element name
	 */
	public String getLocalName();
	
	/**
	 * Gets any other attributes the object proposes to 
	 * serialize in its element. The NexmlWriter will decide
	 * which keys to access in this hash table
	 * @return a hashtable of attributes
	 */
	public Attributes getAttributes();
	
	/**
	 * Gets the plist dictionaries as Hashtables
	 * TODO what to do with any? use castor?
	 * @return an array of Hashtables
	 */
	public Hashtable[] getDictionaries();

	/**
	 * Gets the object this object references (typically an OTU or a set thereof)
	 * @return a NexmlWritable object
	 */
	public NexmlWritable getReferencedObject();
	
	/**
	 * Gets the NexmlWritable objects this object contains
	 * @return an array of NexmlWritable objects
	 */
	public NexmlWritable[] getContainedObjects();
	
	/**
	 * Gets the raw character data contained by this element
	 * @return an array of characters
	 */
	public char[] getCharacterData();
}
