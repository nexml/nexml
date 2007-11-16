package org.nexml;

public class DefaultObjectListener implements ObjectListener {
	public void newObjectNotification(Object obj) {
		System.out.println("Received new object: " + obj);
	}
}
