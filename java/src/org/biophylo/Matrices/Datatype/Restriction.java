package org.biophylo.Matrices.Datatype;

public class Restriction extends Datatype {
	private static int[][] restrictionLookup = { 
		{1,0},
		{0,1},
	};	
	
	public boolean isValueConstrained() {
		return true;
	}	
	
	public boolean isSequential() {
		return true;
	}	
	
	public Restriction() {
		this.alphabet = "01";
		this.lookup = restrictionLookup;
	}
}
