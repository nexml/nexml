package org.biophylo.Util;

public class IDPool {
	private static IDPool instance = null;
	private int counter;
	protected IDPool() {
	     this.counter = 0;
	}
	public static IDPool getInstance() {
		if(instance == null) {
			instance = new IDPool();
		}
	    return instance;
	}
	public int makeId () {
		return this.counter++;
	}
}
