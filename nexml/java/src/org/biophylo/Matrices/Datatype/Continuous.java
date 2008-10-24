package org.biophylo.Matrices.Datatype;

public class Continuous extends Datatype {
	
	/**
	 * 
	 */
	public Continuous() {
		this.missing = '?';
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#split(java.lang.String)
	 */
	public String[] split(String chars) {
		return chars.split(" ");
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
	 * @see org.biophylo.Matrices.Datatype.Datatype#isValueConstrained()
	 */
	public boolean isValueConstrained() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#isSequential()
	 */
	public boolean isSequential() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.Datatype.Datatype#isValid(java.lang.String)
	 */
	public boolean isValid(String charsString) {
		String[] chars = this.split(charsString.toUpperCase());
		String missing = "" + this.getMissing();
		for ( int i = 0; i < chars.length; i++ ) {
			if ( chars[i].equals(missing) ) {
				continue;
			}
			try {
				Double.parseDouble(chars[i]);
			} catch ( NumberFormatException e ) {
				return false;
			}
		}
		return true;
	}	
}

























