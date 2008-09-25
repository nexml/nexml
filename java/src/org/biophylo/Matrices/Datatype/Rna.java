package org.biophylo.Matrices.Datatype;

public class Rna extends Datatype {
	private static int[][] rnalookup = { 
		{1,0,0,0,},
		{0,1,0,0,},
		{0,0,1,0,},
		{0,0,0,1,},
		{1,1,0,0,},
		{1,0,1,0,},
		{1,0,0,1,},
		{0,1,1,0,},
		{0,1,0,1,},
		{0,0,1,1,},
		{1,1,1,0,},
		{1,1,0,1,},
		{1,0,1,1,},
		{0,1,1,1,},
		{1,1,1,1,},
		{1,1,1,1,},
	};	
	
	public Rna() {
		this.alphabet = "ACGUMRWSYKVHDBXN";
		this.missing = '?';
		this.gap = '-';
		this.lookup = rnalookup;
	}
}
