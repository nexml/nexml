package org.nexml;

// $Id$

public interface ObjectListener {
	
	/**
	 * Executes when a new object becomes available
	 * @param a newly created object from the stream
	 */	
	public void newObjectNotification (Object obj);
}
