package org.nexml.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

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
		 * Create an OTUs block
		 */
		OTUs otus = nexmlDocument.createOTUs();
		otus.setLabel("bar");
		OTU otu = otus.createOTU();
		otu.setLabel("foo");
		
		/**
		 * Add annotations
		 */
		DatatypeFactory dtf = null;
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		URI ns = null;
		try {
			ns = new URI("http://evolutionaryontology.org");
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    otu.addAnnotationValue("cdao:hasObject", ns, new Object());	
	    
	    /**
	     * Set/Get a URI
	     */
	    try {
			otu.addAnnotationValue("cdao:hasURI", ns, new URI("http://www.nexml.org"));
			Set<Object> annos = otu.getRelValues("cdao:hasURI");
			Object relValue = annos.iterator().next();
			Assert.assertEquals(((URI)relValue).toString(),"http://www.nexml.org");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	    
	    /**
	     * Set/Get a Calendar
	     */
//	    otu.addAnnotationValue("hasCalendar", Calendar.getInstance());
//	    annos = otu.getAnnotationValues("hasCalendar");
//	    relValue = annos.iterator().next();
//	    Assert.assertTrue(relValue instanceof Calendar);
	    
	    /**
	     * Set/Get a Date
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
	    

//	    otu.addAnnotationValue("hasNodeList", new NodeList());	    
//	    otu.addAnnotationValue("hasElement", Element value);	    
//	    otu.addAnnotationValue("hasByteArray", Byte[] value);	    
//	    otu.addAnnotationValue("hasImage", new java.awt.Image());	    
//	    otu.addAnnotationValue("hasSource", Source value);  
//	    otu.addAnnotationValue("hasXMLGregorianCalendar", dtf.newXMLGregorianCalendar());  
		
	    String nexml = nexmlDocument.getXmlString();
	    Document document = null;
		try {
			document = DocumentFactory.parse(new ByteArrayInputStream(nexml.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		};
		List<OTUs> otus1 = document.getOTUsList();
		System.out.println(document.getXmlString());
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

}
