/**
 * 
 */
package org.nexml.model;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author rvosa
 *
 */
public class TestFileParse {
	@Test
	public void parseCharacters() {
		String nexmlRoot = System.getenv("NEXML_ROOT");
		if ( nexmlRoot == null ) {
			nexmlRoot = "/Users/rvosa/Documents/workspace/nexml/trunk/nexml";
		}
		File file = new File(nexmlRoot+"/examples/trees.xml");
		Document doc = null;
		try {
			doc = DocumentFactory.parse(file);
		} catch (SAXException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
		//System.out.println(doc.getXmlString());
	}

}
