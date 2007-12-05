package org.nexml;

// $Id$

/**
 * Mediator objects (such as a MesquiteProject) need to implement this interface,
 * possibly in an adaptor.
 * @author rvosa
 */
public interface ObjectCache {

	/**
	 * Gets an object from the cache by looking it up by its id
	 * @param id a nexml id attribute's value
	 * @return the object associated with the id
	 */
	public abstract NexmlWritable getObjectById(String id);

	/**
	 * Gets the nexml id attribute's value for the provided object
	 * @param obj an object for which to look up the id
	 * @return a string id
	 */
	public abstract String getIdByObject(NexmlWritable obj);

	/**
	 * Gets the id reference associated with the provided id. For
	 * example, if a characters element has id "c1" and it references
	 * OTUs element "o1", providing "c1" as the argument returns "o1".
	 * @param  id a nexml id attribute's value
	 * @return an id reference's value
	 */
	public abstract String getReferencedIdById(String id);

	/**
	 * Adds an object to the cache
	 * @param obj   an Object to add to the cache
	 * @param id    the id attribute's value
	 * @param idref the value of the id reference (if any)
	 */
	public abstract void setObject(NexmlWritable obj, String id, String idref);
	
	/**
	 * Iterates over objects in the cache
	 * @param i an index
	 * @return a NexmlWritable object
	 */
	public abstract NexmlWritable getObject(int i);

}