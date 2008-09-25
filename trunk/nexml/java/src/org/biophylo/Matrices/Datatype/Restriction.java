package org.biophylo.Matrices.Datatype;

public class Restriction extends Datatype {
	private static int[][] restrictionLookup = { 
		{1,0},
		{0,1},
	};	
	
	public Restriction() {
		this.alphabet = "01";
		this.lookup = restrictionLookup;
	}
}
