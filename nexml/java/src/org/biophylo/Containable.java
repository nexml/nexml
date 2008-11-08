package org.biophylo;
import org.biophylo.util.*;
public abstract class Containable extends XMLWritable {	
	protected int container;
	protected int type;
	
	/**
	 * @return
	 */
	public int container () {
		return this.container;
	}
	
	/**
	 * @return
	 */
	public int type () {
		return this.type;
	}
}
