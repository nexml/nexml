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
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#join(java.lang.String[])
	 */
	public String join(String[] chars) {
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < chars.length; i++ ) {
			if ( i > 0 ) {
				sb.append(' ');
			}
			sb.append(chars[i]);
		}
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#split(java.lang.String)
	 */
	public String[] split(String chars) {
		return chars.split(" ");
	}	

	public boolean isValid(String charsString) {
		String[] chars = split(charsString.toUpperCase());
		String missing = "" + getMissing();
		String gap = "" + getGap();
		for ( int i = 0; i < chars.length; i++ ) {
			if ( chars[i].length() == 0 ) {
				continue;
			}
			if ( !gap.equals(chars[i]) && !missing.equals(chars[i]) && ! chars[i].matches("[0-9]+") ) {
				return false;
			}
		}
		return true;
	}	

}
