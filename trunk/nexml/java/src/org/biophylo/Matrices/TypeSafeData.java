package org.biophylo.Matrices;
import org.biophylo.Matrices.Datatype.*;
public interface TypeSafeData {
	public char getGap();
	public char getMissing();	
	public int[][] getLookup();
	public String getType();
	public Datatype getTypeObject();
	public void setGap(char gap);
	public void setMissing(char missing);	
	public void setLookup(int[][] lookup);
	public void setTypeObject(Datatype typeObject);
}
