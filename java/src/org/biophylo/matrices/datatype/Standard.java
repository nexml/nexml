package org.biophylo.matrices.datatype;

public class Standard extends Datatype {
	private static int[][] standardlookup = { 
		{1,0,0,0,0,0,0,0,0,0},
		{0,1,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0},
		{0,0,0,1,0,0,0,0,0,0},
		{0,0,0,0,1,0,0,0,0,0},
		{0,0,0,0,0,1,0,0,0,0},
		{0,0,0,0,0,0,1,0,0,0},
		{0,0,0,0,0,0,0,1,0,0},
		{0,0,0,0,0,0,0,0,1,0},
		{0,0,0,0,0,0,0,0,0,1},
	};	
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#isValueConstrained()
	 */
	public boolean isValueConstrained() {
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#isSequential()
	 */
	public boolean isSequential() {
		return false;
	}	
	
	/**
	 * 
	 */
	public Standard() {
		this.alphabet = "0123456789";
		this.missing = '?';
		this.lookup = standardlookup;
	}
}
