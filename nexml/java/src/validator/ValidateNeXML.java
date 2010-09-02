package validator;

import java.io.FileReader;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;

public class ValidateNeXML {
	public static String againstXSD(final String xmlFilepath) {
		String apacheXml = "http://apache.org/xml/properties/schema/";
		String namespace = "http://www.nexml.org/2009";
		String schemaURL = "http://www.nexml.org/nexml/xsd/nexml.xsd";
		try {
			final DOMParser parser = new DOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema", true);
			parser.setProperty(apacheXml + "external-schemaLocation", namespace.trim() + " " + schemaURL);
			parser.parse(new InputSource(new FileReader(xmlFilepath)));
			return null;
		}
		catch (final Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static void main(final String args[]) {
		if ( args.length == 0 ) {
			System.out.println("Usage: NexmlValidator <filename>");
			System.exit(1);
		}		
		final String retval = againstXSD(args[0]);
		if (retval == null) {
			System.exit(0);
		}
		System.out.println("Validation error: " + retval);
		System.exit(1);
	}	
}
