package org.nexml.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		 * Create an OTUs block and an OTU, which we will annotate
		 */
		OTUs otus = nexmlDocument.createOTUs();
		otus.setLabel("bar");
		OTU otu = otus.createOTU();
		otu.setLabel("foo");
		
		// we need this to create some obscure-ish objects, such as "Duration"
		DatatypeFactory dtf = null;
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		
		// we're going to namespace all predicates in CDAO
		URI ns = URI.create("http://evolutionaryontology.org/#");
		
		// attaching a simple object, just to see that we can
	    otu.addAnnotationValue("cdao:hasObject", ns, new Object());	
	    
	    /**
	     * Set/Get a URI as a dc:relation
	     */
	    String baseURI = "http://www.nexml.org/PhyloWS/";
	    nexmlDocument.setBaseURI(URI.create(baseURI));
	    String valueURI = "http://www.nexml.org/PhyloWS/TB2:Tl1234";
	    String dcns = "http://purl.org/dc/elements/1.1/";
	    nexmlDocument.addAnnotationValue("dc:relation", URI.create(dcns), URI.create(valueURI));
		otu.addAnnotationValue("dc:relation", URI.create(dcns), URI.create(valueURI));
		Assert.assertEquals(otu.getRelValues("dc:relation").iterator().next().toString(),valueURI);
	    
	    /**
	     * Set/Get a Calendar - doesn't work yet (because of
	     * unpredictable date string formatting)
	     */
//	    otu.addAnnotationValue("hasCalendar", Calendar.getInstance());
//	    annos = otu.getAnnotationValues("hasCalendar");
//	    relValue = annos.iterator().next();
//	    Assert.assertTrue(relValue instanceof Calendar);
	    
	    /**
	     * Set/Get a Date - doesn't work yet (because of
	     * unpredictable date string formatting)
	     */
//	    otu.addAnnotationValue("hasDate", new Date(0));
//	    annos = otu.getAnnotationValues("hasDate");
//	    relValue = annos.iterator().next();
//	    Assert.assertEquals((Date)relValue,new Date(0));
		
		otu.addAnnotationValue("cdao:hasBigDecimal", ns, new BigDecimal(0.5));
	    otu.addAnnotationValue("cdao:hasBigInteger", ns, new BigInteger("1"));
	    otu.addAnnotationValue("cdao:hasBoolean", ns, new Boolean(true));
	    otu.addAnnotationValue("cdao:hasByte", ns, new Byte("1"));
	    otu.addAnnotationValue("cdao:hasDouble", ns, new Double(12));
	    otu.addAnnotationValue("cdao:hasFloat", ns, new Float(0.5));
	    otu.addAnnotationValue("cdao:hasInteger", ns, new Integer(13));
	    otu.addAnnotationValue("cdao:hasLong", ns, new Long(14));
	    otu.addAnnotationValue("cdao:hasShort", ns, new Short("15"));
	    otu.addAnnotationValue("cdao:hasString", ns, "foo");
	    otu.addAnnotationValue("cdao:hasDuration", ns, dtf.newDuration(1));
	    otu.addAnnotationValue("cdao:hasQName", ns, new QName("foo"));
	    
	    runEqualsTests(otu); 
	    
	    /**
	     * Set/Get a UUID
	     */
	    otu.addAnnotationValue("cdao:hasUUID", ns, UUID.randomUUID());
	    Object relValue = getAnnotationValue(otu,"cdao:hasUUID");
	    Assert.assertTrue(relValue instanceof UUID);
	    
	    /**
	     *  Set/Get an Element
	     */
	    org.w3c.dom.Document domdoc = createDocument();
	    Element foo = domdoc.createElement("foo");
	    otu.addAnnotationValue("cdao:hasElement", ns, foo);	   
	    relValue = getAnnotationValue(otu,"cdao:hasElement");
	    Assert.assertTrue(relValue instanceof Element);
	    
	    /**
	     * Set/Get a NodeList
	     */
	    
	    // first we need to jump through a bunch of hoops to create a NodeList
	    String opaqueXml = "<phoo><foo/><bar/></phoo>";
	    org.w3c.dom.Document opaqueDoc = null;
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		try {
			opaqueDoc = documentBuilder.parse(new ByteArrayInputStream(opaqueXml.getBytes()));
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    NodeList phenoScapeNodes = opaqueDoc.getElementsByTagName("phoo");
	    
	    // here's the actual getting and setting
	    otu.addAnnotationValue("cdao:hasNodeList", ns, phenoScapeNodes);
	    relValue = getAnnotationValue(otu,"cdao:hasNodeList");
	    Assert.assertTrue(relValue instanceof NodeList);
	    
	    /**
	     * Adding a recursively nested annotation
	     */
	    // XXX The Object in this annotation is a placeholder, which will be 
	    // replaced by the inner annotation we attach 4 lines down.
	    otu.addAnnotationValue("cdao:hasAnnotation",ns,new Object());
	    Set<Annotation> annos = otu.getAnnotations("cdao:hasAnnotation");
	    Annotation outer = annos.iterator().next();
	    outer.addAnnotationValue("cdao:hasObject",ns,new Object());
	    
	    /**
	     * Haven't done these yet
	     */
//	    otu.addAnnotationValue("hasByteArray", Byte[] value);	    
//	    otu.addAnnotationValue("hasImage", new java.awt.Image());	    
//	    otu.addAnnotationValue("hasSource", Source value);  
//	    otu.addAnnotationValue("hasXMLGregorianCalendar", dtf.newXMLGregorianCalendar());  
		
	    /**
	     * Here we serialize *to* a nexml string, then parse that,
	     * then run the equals tests again to test we can properly
	     * round-trip
	     */
	    String nexml = nexmlDocument.getXmlString();
	    Document document = null;
		try {
			document = DocumentFactory.parse(new ByteArrayInputStream(nexml.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		};
		List<OTUs> otus1 = document.getOTUsList();
		//System.out.println(document.getXmlString());
		runEqualsTests(otus1.get(0).getAllOTUs().get(0));		
	}
	
	private Object getAnnotationValue(Annotatable annotatable,String property) {
		Set<Object> annos = annotatable.getAnnotationValues(property);
		return annos.iterator().next();
	}
	
	private void testEquals(String property,Object object,Annotatable annotatable) {
		Object value = getAnnotationValue(annotatable,property);
		Assert.assertEquals(value,object);
	}
	
	private void runEqualsTests(Annotatable otu) {
		DatatypeFactory dtf = null;
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}		
		testEquals("cdao:hasBigDecimal", new BigDecimal(0.5),otu);
	    testEquals("cdao:hasBoolean",new Boolean(true),otu);
	    testEquals("cdao:hasBigInteger",new BigInteger("1"),otu);
	    testEquals("cdao:hasByte",new Byte("1"),otu);
	    testEquals("cdao:hasDouble",new Double(12),otu);
	    testEquals("cdao:hasFloat",new Float(0.5),otu);
	    testEquals("cdao:hasLong",new Long(14),otu);
	    testEquals("cdao:hasShort",new Short("15"),otu);
	    testEquals("cdao:hasString","foo",otu);
	    testEquals("cdao:hasDuration",dtf.newDuration(1),otu);
	    testEquals("cdao:hasQName",new QName("foo"),otu); 		
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
