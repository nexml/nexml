package org.nexml.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.Assert;
import org.xml.sax.SAXException;

public class TestContinuousMatrix {
	@Test
	public void testContinuousMatrix() {
		Document doc = null;
		try {
			doc = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		OTUs otus = doc.createOTUs();
		OTU otu = otus.createOTU();
		ContinuousMatrix contmat = doc.createContinuousMatrix(otus);
		Character character = contmat.createCharacter();
		contmat.getCell(otu, character).setValue(0.434534);	
		Assert.assertNotNull("matrix != null", contmat);

		/**
		 * Now we're going to validate the output, so first we need
		 * to jump through the xml parsing hoops.
		 */
		DocumentBuilderFactory factory =
		    DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setAttribute(
		    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
		    "http://www.w3.org/2001/XMLSchema");
		factory.setAttribute(
		    "http://java.sun.com/xml/jaxp/properties/schemaSource",
		    "http://nexml-dev.nescent.org/1.0/nexml.xsd");
		DocumentBuilder builder = null;
		
		/**
		 * We're going to turn the string output of our created
		 * NeXML into an InputStream for the parser. If we don't
		 * have valid UTF-8, we've failed this test.
		 */
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(doc.getXmlString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			Assert.assertFalse(e1.getMessage(), false);
			e1.printStackTrace();
		} catch (NullPointerException e) {
			Assert.assertFalse(e.getMessage(),false);
			e.printStackTrace();
		}
		
		/**
		 * Let's see if we get through this hoop. Is not our
		 * fault if we don't.
		 */
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
		
		/**
		 * Now let's parse our produced xl
		 */
		try {
			builder.parse(is);
		} catch (SAXException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
		//System.out.println(doc.getXmlString());
		
	}
	
}
