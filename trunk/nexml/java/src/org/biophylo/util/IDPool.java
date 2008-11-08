package org.biophylo.util;

public class IDPool {
	private static IDPool mInstance = null;
	private int mCounter;
	
	/**
	 * 
	 */
	protected IDPool() {
		mCounter = 0;
	}
	
	/**
	 * @return
	 */
	public static IDPool getInstance() {
		if ( mInstance == null ) {
			mInstance = new IDPool();
		}
	    return mInstance;
	}
	
	/**
	 * @return
	 */
	public int makeId () {
		return mCounter++;
	}
}
