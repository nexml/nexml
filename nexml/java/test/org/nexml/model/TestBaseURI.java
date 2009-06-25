package org.nexml.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nexml.model.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TestBaseURI {
	@Test
	public void testAnnotation () {
		/** 
		 * First, let's create a document object. If this throws
		 * an exception, I guess that means we failed our first test.
		 */		
		Document nexmlDocument = null;
		try {
			nexmlDocument = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}

		
		// we're going to namespace this in DC
		URI predicateNS = null, baseURI = null, predicateValueURI = null;
		try {
			predicateNS = new URI("http://purl.org/dc/elements/1.1/");
			baseURI = new URI("http://purl.org/phylo/treebase/phylows/");
			predicateValueURI = new URI("http://purl.org/phylo/treebase/phylows/TB2:S1234");
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		

	    String nexml = nexmlDocument.getXmlString();
	    Document document = null;
		try {
			document = DocumentFactory.parse(new ByteArrayInputStream(nexml.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		};
		List<OTUs> otus1 = document.getOTUsList();
		System.out.println(document.getXmlString());
	}
	
	private org.w3c.dom.Document createDocument () {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return documentBuilder.newDocument();
	}
}
