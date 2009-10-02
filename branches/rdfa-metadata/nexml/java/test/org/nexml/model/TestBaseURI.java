package org.nexml.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.nexml.model.Document;
import org.nexml.model.impl.AnnotationImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TestBaseURI {
	@Test
	public void testBaseURI () {
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
		
		// 
		URI predicateValueURI = URI.create("http://purl.org/phylo/treebase/phylows/TB2:S1234");
		Annotation annotation = createAnnotation(nexmlDocument,predicateValueURI);
		
		Assert.assertEquals(predicateValueURI, (URI)annotation.getValue());
		Assert.assertEquals("dc:relation",annotation.getRel());

	    String nexml = nexmlDocument.getXmlString();
	    Assert.assertTrue(nexml.contains("\"TB2:S1234\""));
	    
	    Document document = null;
		try {
			document = DocumentFactory.parse(new ByteArrayInputStream(nexml.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		};
		Assert.assertNotNull(document);
	}
	
	private Annotation createAnnotation(Document nexmlDocument,URI predicateValueURI) {
		// namespace of the predicate, i.e. dublin core elements, dc:relation
		URI predicateNS = URI.create("http://purl.org/dc/elements/1.1/");
		
		// the base URI of the document, typically attached to the root element
		URI baseURI = URI.create("http://purl.org/phylo/treebase/phylows/");

		// here we attach the base URI to the root element. Now, other URIs in the document
		// are truncated to paths relative to the base, provided those URIs or subpaths of the base
		nexmlDocument.setBaseURI(baseURI);
		
		// here we create the triple, subject=nexmlDocument, predicate=dc:relation, object=predicateValueURI
		return nexmlDocument.addAnnotationValue("dc:relation", predicateNS, predicateValueURI);		
	}
	
}
