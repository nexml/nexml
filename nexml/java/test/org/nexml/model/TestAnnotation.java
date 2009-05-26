package org.nexml.model;

import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;

public class TestAnnotation {
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
		
		/**
		 * Create an OTUs block
		 */
		OTUs otus = nexmlDocument.createOTUs();
		otus.setLabel("bar");
		OTU otu = otus.createOTU();
		otu.setLabel("foo");
		
		/**
		 * Add annotations
		 */
		otu.addAnnotationValue("cdao:hasId","T23423");
		try {
			otu.addAnnotationValue("cdao:hasReference",new URI("http://example.org"));
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		otu.addAnnotationValue("cdao:hasLiteral", new Long(54353));
		
		System.out.println(nexmlDocument.getXmlString());
	}

}
