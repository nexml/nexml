package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * An {@code OTUs} implemented with DOM objects.
 */
class OTUsImpl extends SetManager<OTU> implements OTUs {

	/**
	 * Get the (XML) tag name of otus.
	 * 
	 * @return the (XML) tag name of otus.
	 */
	public static String getTagNameClass() {
		return "otus";
	}

	/**
	 * Build an {@code OTUs} from a {@code Document}.
	 * <p>
	 * {@document} will then be owned by this object and shouldn't be modified
	 * outside of it.
	 * 
	 * @param document that from which we're building.
	 */
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
				String originalOTUId = thisOTUElement.getAttribute("id");
				OTUImpl otu = new OTUImpl(getDocument(), thisOTUElement);
				mOriginalOTUIds.put(originalOTUId, otu);
				this.addOTU(otu);
			}
		} catch (XPathExpressionException e) {

		}
	}

	private void addOTU(OTU otu) {
		addThing(otu);
	}

	/** {@inheritDoc} */
	public void addOTUToSubset(String setName, OTU otu) {
		addToSubset(setName, otu);
	}

	/** {@inheritDoc} */
	public OTU createOTU() {
		OTUImpl otu = new OTUImpl(getDocument());
		addOTU(otu);
		getElement().appendChild(otu.getElement());
		return otu;
	}

	public void createOTUSubset(String setName) {
		createSubset(setName);
	}

	public List<OTU> getAllOTUs() {
		return Collections.unmodifiableList(getThings());
	}

	public List<OTU> getOTUsFromSubset(String setName) {
		return getSubset(setName);
	}

	public void removeOTU(OTU otu) {
		removeThing(otu);
	}

	public void removeOTUFromSubset(String setName, OTU otu) {
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

	public void addAnnotationToSubset(String setName, Annotation annotation) {
		// TODO Auto-generated method stub

	}

	private Map<String, OTU> mOriginalOTUIds = new HashMap<String, OTU>();

	Map<String, OTU> getOriginalOTUIds() {
		return mOriginalOTUIds;
	}

}
