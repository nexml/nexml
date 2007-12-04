package org.nexml;

// $Id$

/**
 * Once objects have been created by their respective factories, the
 * nexml system then passes them to a listener. This would be the 
 * point, for example, where processed objects are added to a Mesquite
 * project, inserted in a database, and so on. The interface that 
 * describes this behaviour is defined here.
 */
public interface ObjectListener {
	
	/**
	 * Executes when a new object becomes available
	 * @param obj a newly created object from the stream
	 */	
	public void newObjectNotification (Object obj);
}
