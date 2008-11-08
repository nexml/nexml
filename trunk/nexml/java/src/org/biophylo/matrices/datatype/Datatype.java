package org.biophylo.matrices.datatype;

import java.util.*;
import org.biophylo.util.*;
import org.biophylo.util.exceptions.*;
import org.w3c.dom.Element;

public abstract class Datatype extends XMLWritable {
	private static Logger logger = Logger.getInstance();
	protected String alphabet;
	protected char missing;
	protected char gap;
	protected int[][] lookup;
	
	/**
	 * @param type
	 * @return
	 */
	public static Datatype getInstance(String type) {
		String base = "org.biophylo.Matrices.Datatype.";
		Datatype dt = null;
		logger.info("Instantiating for data type " + type);
		try {
			dt = (Datatype)Class.forName(base+type).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dt.tag = "states";
		return dt;
	}
	
	/**
	 * @param missing
	 */
	public void setMissing(char missing) {
		this.missing = missing;
	}
	
	/**
	 * @return
	 */
	public char getMissing() {
		return this.missing;
	}
	
	/**
	 * @param gap
	 */
	public void setGap(char gap) {
		this.gap = gap;
	}
	
	/**
	 * @return
	 */
	public char getGap() {
		return this.gap;
	}
	
	/**
	 * @param lookup
	 */
	public void setLookup(int[][] lookup) {
		this.lookup = lookup;
	}
	
	/**
	 * @param lookup
	 */
	public void setLookup(HashMap lookup) {
		int firstDimension = lookup.size();
		String[] keys = new String[firstDimension];
		lookup.keySet().toArray(keys);
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < keys.length; i++ ) {
			sb.append(keys[i]);
		}
		
		this.alphabet = sb.toString();
		int[][] intLookup = new int[firstDimension][firstDimension];
		for ( int i = 0; i < keys.length; i++ ) {
			Vector values = (Vector)lookup.get(keys[i]);
			for ( int j = 0; j < values.size(); j++ ) {
				int k = this.alphabet.indexOf((String)values.get(j));
				intLookup[i][k] = 1;
			}
			for ( int j = 0; j < firstDimension; j++ ) {
				if ( intLookup[i][j] != 1 ) {
					intLookup[i][j] = 0;
				}
			}
		}
		this.lookup = intLookup;
	}
	
	/**
	 * @return
	 */
	public int[][] getLookup() {
		return this.lookup;
	}
	
	/**
	 * @param alphabet
	 */
	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}
	
	/**
	 * @return
	 */
	public String getAlphabet() {
		return this.alphabet;
	}
	
	/**
	 * @param states
	 * @return
	 */
	public char getSymbolForStates(char[] states) {
		int[][] thisLookup = this.getLookup();		
		if ( thisLookup != null ) {
			int length = thisLookup[0].length;
			int[] indices = new int[length];
			String thisAlphabet = this.getAlphabet();
			for ( int i = 0; i < states.length; i++ ) {
				int index = this.getAlphabet().indexOf(states[i]);
				if ( index > length ) {
					// boom
				}
				else {
					indices[index] = 1;
				}
			}
			SYMBOL: for ( int i = 0; i < thisLookup.length; i++ ) {
				for ( int j = 0; j < thisLookup[i].length; j++ ) {
					if ( indices[j] != thisLookup[i][j] ) {
						continue SYMBOL;
					}
				}
				return thisAlphabet.charAt(i);
			}
			return thisAlphabet.charAt( thisAlphabet.length() - 1 );
		}
		else {
			return 0;
		}
	}	
	
	/**
	 * @return
	 */
	public String getType() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * @return
	 */
	public HashMap getIdsForStates() {
		HashMap result = null;
		if ( this.lookup != null ) {
			result = new HashMap();
			for ( int i = 0; i < this.lookup.length; i++ ) {
				String symbol = this.alphabet.substring(i, i+1);
				result.put(symbol, ""+(i+1));
			}
		}
		return result;
	}	
	
	/**
	 * @param charsString
	 * @return
	 */
	public boolean isValid(String charsString) {
		logger.info("validating "+charsString);
		String[] chars = this.split(charsString.toUpperCase());
		String alphabet = this.getAlphabet();
		String missing = "" + this.getMissing();
		String gap = "" + this.getGap();
		for ( int i = 0; i < chars.length; i++ ) {
			if ( chars[i].length() == 0 ) {
				continue;
			}
			logger.info(chars[i]);
			if ( !gap.equals(chars[i]) && !missing.equals(chars[i]) && alphabet.indexOf(chars[i]) < 0 ) {
				logger.info("neither missing ("+missing+"), gap ("+gap+"), nor alphabet ("+alphabet+") character: " + chars[i]);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return
	 */
	public abstract boolean isValueConstrained();
	
	/**
	 * @return
	 */
	public abstract boolean isSequential();
	
	/**
	 * @param that
	 * @return
	 */
	public boolean isSame(Datatype that) {		
		if ( !this.getType().equals(that.getType()) ) {
			return false;
		}
		char thisMissing = this.getMissing();
		char thatMissing = that.getMissing();
		if ( thisMissing != 0 && thatMissing != 0 && thisMissing != thatMissing ) {
			return false;
		}
		char thisGap = this.getGap();
		char thatGap = that.getGap();
		if ( thisGap != 0 && thatGap != 0 && thisGap != thatGap ) {
			return false;
		}
		int[][] thisLookup = this.getLookup();
		int[][] thatLookup = that.getLookup();
		if ( thisLookup == null ^ thatLookup == null ) {
			return false;
		}
		if ( thisLookup != null && thatLookup != null ) {
			if ( thisLookup.length != thatLookup.length ) {
				return false;
			}
			for ( int i = 0; i < thisLookup.length; i++ ) {
				if ( thisLookup[i].length != thatLookup[i].length ) {
					return false;
				}
				for ( int j = 0; j < thisLookup[i].length; j++ ) {
					if ( thisLookup[i][j] != thatLookup[i][j] ) {
						return false;
					}
				}
			}
		}
		return true;
	}	
	
	/**
	 * @param chars
	 * @return
	 */
	public String[] split(String chars) {
		String[] temp = chars.split("");
		String[] result = new String[temp.length-1];
		for ( int i = 1; i < temp.length; i++ ) {
			result[i-1] = temp[i];
		}
		return result;
	}
	
	/**
	 * @param chars
	 * @return
	 */
	public String join(String[] chars) {
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < chars.length; i++ ) {
			sb.append(chars[i]);
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Util.XMLWritable#toXmlElement()
	 */
	public Element toXmlElement() throws ObjectMismatch {
		Element toElt = null;
		int[][] lookup = this.getLookup();
		if ( lookup != null && ! this.isValueConstrained() ) {
			toElt = createElement(getTag(),getAttributes(),getDocument());
			final HashMap idForState = this.getIdsForStates();
			class Sorter implements Comparator {
				public int compare (Object obja, Object objb) {
					return Integer.parseInt((String)idForState.get(obja)) 
						- Integer.parseInt((String)idForState.get(objb));
				}
			}
			Object[] tmp = idForState.keySet().toArray();
			String[] states = new String[tmp.length];
			for ( int i = 0; i < tmp.length; i++ ) {
				states[i] = (String)tmp[i];
			}
			Arrays.sort(states, new Sorter());
			for ( int i = 0; i < states.length; i++ ) {
				int stateId = Integer.parseInt((String)idForState.get(states[i]));
				idForState.put(states[i], "s"+stateId);
			}
			for ( int i = 0; i < states.length; i++ ) {
				String stateId = (String)idForState.get(states[i]);
				String[] mapping = this.getAmbiguitySymbols(states[i]);
				if ( mapping.length > 1 ) {
					HashMap stateAttrs = new HashMap();
					stateAttrs.put("id", stateId);
					stateAttrs.put("symbol", states[i]);
					Element stateElt = createElement("state",stateAttrs,getDocument());
					for ( int j = 0; j < mapping.length; j++ ) {
						HashMap mappingAttrs = new HashMap();
						mappingAttrs.put("state", idForState.get(mapping[j]));
						mappingAttrs.put("mstaxa", "uncertainty");
						Element mappingElt = createElement("mapping",mappingAttrs,getDocument());
						stateElt.appendChild(mappingElt);
					}
					toElt.appendChild(stateElt);
				}
				else {
					HashMap stateAttrs = new HashMap();
					stateAttrs.put("id", stateId);
					stateAttrs.put("symbol", states[i]);
					toElt.appendChild(createElement("state",stateAttrs,getDocument()));
				}
			}
		}		
		return toElt;
	}
	
	/**
	 * @param symbol
	 * @return
	 */
	private String[] getAmbiguitySymbols(String symbol) {
		if ( this.alphabet == null || this.lookup == null ) {
			return null;
		}
		int index = this.alphabet.indexOf(symbol.toUpperCase());
		int[] ambig = this.lookup[index];
		int ambigLength = 0;
		for ( int i = 0; i < ambig.length; i++ ) {
			if ( ambig[i] == 1 ) {
				ambigLength++;
			}
		}
		String[] mapping = new String[ambigLength];
		int j = 0;
		for ( int i = 0; i < ambig.length; i++ ) {
			if ( ambig[i] == 1 ){
				mapping[j] = this.alphabet.substring(i, i+1);
				j++;
			}
		}
		return mapping;
	}
}
