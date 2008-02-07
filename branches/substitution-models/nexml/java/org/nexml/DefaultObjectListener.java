package org.nexml;

// $Id$

/**
 * Once objects have been created by their respective factories, the
 * nexml system then passes them to a listener. This would be the 
 * point, for example, where processed objects are added to a Mesquite
 * project, inserted in a database, and so on. The interface that 
 * describes this behaviour is defined by ObjectListener, this class
 * is a concrete implementation thereof.
 * @see ObjectListener
 */
public class DefaultObjectListener implements ObjectListener {
	private ObjectCache theCache;
	
	/**
	 * Constructor that accepts a cache to add objects received at
	 * notification
	 * @param cache
	 */
	public DefaultObjectListener (ObjectCache cache) {
		this.theCache = cache;
	}
	
	/**
	 * Default contructor
	 */
	public DefaultObjectListener () {
		this.theCache = new DefaultObjectCache();
	}
	
	/**
	 * Executes when a new object becomes available
	 * @param obj newly created object from the stream
	 */
	public void newObjectNotification(NexmlWritable obj) {
		System.out.println("Received new object: " + obj);
		this.theCache.setObject(obj, obj.getId(), null);
	}
	
	public ObjectCache getObjectCache() {
		return this.theCache;
	}
}
