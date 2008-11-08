package org.biophylo.parsers;
import java.io.*;
public interface Parsers {
	/**
	 * @param data
	 * @return
	 */
	public Object[] parse (InputStream data);
}
