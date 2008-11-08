package org.biophylo.util;

public class IDPool {
	private static IDPool instance = null;
	private int counter;
	
	/**
	 * 
	 */
	protected IDPool() {
	     this.counter = 0;
	}
	
	/**
	 * @return
	 */
	public static IDPool getInstance() {
		if(instance == null) {
			instance = new IDPool();
		}
	    return instance;
	}
	
	/**
	 * @return
	 */
	public int makeId () {
		return this.counter++;
	}
}
