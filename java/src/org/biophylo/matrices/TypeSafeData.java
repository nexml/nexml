package org.biophylo.matrices;
import org.biophylo.matrices.datatype.*;
public interface TypeSafeData {
	/**
	 * @return
	 */
	public char getGap();
	
	/**
	 * @return
	 */
	public char getMissing();	
	
	/**
	 * @return
	 */
	public int[][] getLookup();
	
	/**
	 * @return
	 */
	public String getType();
	
	/**
	 * @return
	 */
	public Datatype getTypeObject();
	
	/**
	 * @param gap
	 */
	public void setGap(char gap);
	
	/**
	 * @param missing
	 */
	public void setMissing(char missing);	
	
	/**
	 * @param lookup
	 */
	public void setLookup(int[][] lookup);
	
	/**
	 * @param typeObject
	 */
	public void setTypeObject(Datatype typeObject);
}
