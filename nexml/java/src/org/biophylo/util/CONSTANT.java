package org.biophylo.Util;
import org.biophylo.*;
import org.biophylo.Util.Exceptions.*;

public class CONSTANT {
	public static final int NONE     = 1;
	public static final int NODE     = 2;
	public static final int TREE     = 3;
	public static final int FOREST   = 4;
	public static final int TAXON    = 5;
	public static final int TAXA     = 6;
	public static final int DATUM    = 7;
	public static final int MATRIX   = 8;
	public static final int MATRICES = 9;
	public static final int PROJECT  = 10;
	
	/**
	 * @param obj
	 * @param constant
	 * @return
	 * @throws Generic
	 */
	public static final boolean looksLikeObject(Containable obj, int constant) throws Generic {
		if ( obj.type() == constant ) {
			return true;
		}
		else {
			throw new ObjectMismatch();
		}
	}
}
