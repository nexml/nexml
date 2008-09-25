package org.biophylo.Matrices.Datatype;

public class Continuous extends Datatype {
	public Continuous() {
		this.missing = '?';
	}
	
	public String[] split(String chars) {
		return chars.split(" ");
	}
	
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
	
	public boolean isValueConstrained() {
		return true;
	}
	
	public boolean isSequential() {
		return false;
	}
	
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

























