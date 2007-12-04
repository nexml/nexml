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
	
	/**
	 * Executes when a new object becomes available
	 * @param obja newly created object from the stream
	 */
	public void newObjectNotification(Object obj) {
		System.out.println("Received new object: " + obj);
	}
}
