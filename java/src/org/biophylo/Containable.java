package org.biophylo;
import org.biophylo.util.*;
public abstract class Containable extends XMLWritable {	
	protected int mContainer;
	protected int mType;
	
	/**
	 * @return
	 */
	public int container () {
		return mContainer;
	}
	
	/**
	 * @return
	 */
	public int type () {
		return mType;
	}
}
