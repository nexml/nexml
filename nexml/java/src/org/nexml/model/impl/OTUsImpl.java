package org.nexml.model.impl;

import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.nexml.model.Annotation;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class OTUsImpl extends SetManager<OTU> implements OTUs {

	public static String getTagNameClass() {
		return "otus";
	}

	public OTUsImpl(Document document) {
		super(document);
	}

	public OTUsImpl(Document document, Element item) {
		super(document, item);
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression otusExpr = xpath.compile(OTUImpl.getTagNameClass());
			Object result = otusExpr.evaluate(this.getElement(),
					XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element thisOTUElement = (Element) nodes.item(i);
				OTUImpl otu = new OTUImpl(getDocument(), thisOTUElement);
				this.addOTU(otu);
			}
		} catch (XPathExpressionException e) {

		}
	}	

	private void addOTU(OTU otu) {
		addThing(otu);
	}

	public void addOTUToSet(String setName, OTU otu) {
		addToSet(setName, otu);
	}

	public OTU createOTU() {
		OTUImpl otu = new OTUImpl(getDocument());
		addOTU(otu);
		getElement().appendChild(otu.getElement());
		return otu;
	}

	public void createOTUSet(String setName) {
		createSet(setName);
	}

	public List<OTU> getAllOTUs() {
		return getThings();
	}

	public List<OTU> getOTUsFromSet(String setName) {
		return getFromSet(setName);
	}

	public void removeOTU(OTU otu) {
		removeThing(otu);
	}

	public void removeOTUFromSet(String setName, OTU otu) {
		// TODO Auto-generated method stub

	}

	public Iterator<OTU> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getTagName() {
		return getTagNameClass();
	}

    public void addAnnotationToSet(String setName, Annotation annotation) {
        // TODO Auto-generated method stub
        
    }

    public void removeAnnotationFromSet(Annotation annotation) {
        // TODO Auto-generated method stub
        
    }
}
