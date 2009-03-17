/**
 * 
 */
package org.nexml.model;

import java.io.File;
import java.io.IOException;

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
		File file = new File("trunk/nexml/examples/trees.xml");
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
		System.out.println(doc.getXmlString());
	}

}
