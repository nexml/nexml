package org.biophylo;
import java.io.*;
import org.biophylo.parsers.*;
import org.biophylo.matrices.datatype.Datatype;
public class IO {
	/**
	 * @param format
	 * @param data
	 * @return
	 */
	public static Object[] parse(String format, InputStream data) {
		String base = "org.biophylo.Parsers.";
		Parsers p = null;
		try {
			p = (Parsers)Class.forName(base+format).newInstance();			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return p.parse(data);
	}
}
