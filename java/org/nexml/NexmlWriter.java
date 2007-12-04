package org.nexml;

/**
 * Writes objects to nexml
 * @author rvosa
 * @see NexmlWritable
 */
public class NexmlWriter {
	private boolean compact;
	
	/**
	 * Sets whether the writer should write concise nexml
	 * @param isCompact if true, write compact representations
	 */
	public void setCompactMode(boolean isCompact) {
		this.compact = isCompact;
	}
	
	/**
	 * Writes nexml to a string
	 * @return a nexml string representation
	 */
	public String writeString() {
		return "<? xml etc.";
	}
	
	/**
	 * Writes nexml to a file
	 * @param path file path to write to
	 */
	public void writeFile (String path) {
		
	}
	
	/* TODO set object cache, and in fact serialize 
	 * its contents? Which would implement NexmlWritable? */
}
