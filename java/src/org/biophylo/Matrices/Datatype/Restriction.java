package org.biophylo.Matrices.Datatype;

public class Restriction extends Datatype {
	private static int[][] restrictionLookup = { 
		{1,0},
		{0,1},
	};	
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#isValueConstrained()
	 */
	public boolean isValueConstrained() {
		return true;
	}	
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#isSequential()
	 */
	public boolean isSequential() {
		return true;
	}	
	
	/**
	 * 
	 */
	public Restriction() {
		this.alphabet = "01";
		this.lookup = restrictionLookup;
	}
}
