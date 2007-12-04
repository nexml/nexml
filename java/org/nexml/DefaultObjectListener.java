package org.nexml;

// $Id$

public class DefaultObjectListener implements ObjectListener {
	/**
	 * Executes when a new object becomes available
	 * @param a newly created object from the stream
	 */
	public void newObjectNotification(Object obj) {
		System.out.println("Received new object: " + obj);
	}
}
