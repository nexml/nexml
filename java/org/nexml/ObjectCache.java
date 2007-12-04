package org.nexml;

// $Id$

import java.util.*;

public class ObjectCache {
	private Hashtable objById;
	private Hashtable idByObj;
	private Hashtable referencedIdById;
	
	/**
	 * Gets an object from the cache by looking it up by its id
	 * @param id a nexml id attribute's value
	 * @return the object associated with the id
	 */
	public Object getObjectById (String id) {
		return this.objById.get(id);
	}
	
	/**
	 * Gets the nexml id attribute's value for the provided object
	 * @param obj an object for which to look up the id
	 * @return a string id
	 */
	public String getIdByObject(Object obj) {
		return (String)this.idByObj.get(obj);
	}
	
	/**
	 * Gets the id reference associated with the provided id. For
	 * example, if a characters element has id "c1" and it references
	 * OTUs element "o1", providing "c1" as the argument returns "o1".
	 * @param  id a nexml id attribute's value
	 * @return an id reference's value
	 */
	public String getReferencedIdById (String id) {
		return (String)this.referencedIdById.get(id);
	}
	
	/**
	 * Adds an object to the cache
	 * @param obj   an Object to add to the cache
	 * @param id    the id attribute's value
	 * @param idref the value of the id reference (if any)
	 */
	public void setObject(Object obj, String id, String idref) {
		this.idByObj.put(obj, id);
		this.objById.put(id, obj);
		if ( idref != null ) {
			this.referencedIdById.put(id, idref);
		}
	}
}
