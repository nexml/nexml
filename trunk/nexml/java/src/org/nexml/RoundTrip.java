package org.nexml;

/**
 * A simple example program that reads in a nexml file
 * and writes it out again. Useful for debugging parsing
 * and serialization code.
 * @author rvosa
 * @see NexmlParser
 * @see NexmlWriter
 */
public class RoundTrip {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			NexmlParser parser = new NexmlParser();
			ObjectCache oc = parser.getHandler().getObjectListener().getObjectCache();
			parser.parseFile(args[0]);
			int i = 0;
			while ( oc.getObject(i) != null ) {
				NexmlWritable obj = oc.getObject(i);
				System.out.println(obj.getLocalName());
				i++;
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}

}
