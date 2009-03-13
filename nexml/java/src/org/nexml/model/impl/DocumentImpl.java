package org.nexml.model.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.ContinuousMatrix;
import org.nexml.model.Document;
import org.nexml.model.Matrix;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DocumentImpl extends AnnotatableImpl implements Document {
	private List<OTUs> mOtusList = new ArrayList<OTUs>();
	private List<Matrix<?>> mMatrices = new ArrayList<Matrix<?>>();

	private List<TreeBlock> mTreeBlockList = new ArrayList<TreeBlock>();

	public DocumentImpl(org.w3c.dom.Document document) {
		super(document);
		setRootAttributes();
	}

	public DocumentImpl(org.w3c.dom.Document document, Element element) {
		super(document, element);
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression otusExpr = xpath.compile(this.getTagName() + "/"
					+ OTUsImpl.getTagNameClass());

			Object result = otusExpr.evaluate(document, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			Map<String, OTUsImpl> originalOTUsIds = new HashMap<String, OTUsImpl>();

			for (int i = 0; i < nodes.getLength(); i++) {
				String originalOTUsId = ((Element) nodes.item(i))
						.getAttribute("id");
				OTUsImpl otus = new OTUsImpl(document, (Element) nodes.item(i));
				originalOTUsIds.put(originalOTUsId, otus);
				mOtusList.add(otus);
			}
			XPathExpression treesExpr = xpath.compile(this.getTagName() + "/"
					+ TreeBlockImpl.getTagNameClass());
			NodeList treeBlockNodes = (NodeList) treesExpr.evaluate(document,
					XPathConstants.NODESET);
			for (int i = 0; i < treeBlockNodes.getLength(); i++) {
				Element treeBlockElement = (Element) treeBlockNodes.item(i);
				TreeBlockImpl treeBlock = new TreeBlockImpl(document,
						treeBlockElement, originalOTUsIds.get(
								treeBlockElement.getAttribute("otus"))
								.getOriginalOTUIds());
				treeBlock.setOTUs(originalOTUsIds.get(treeBlock.getElement()
						.getAttribute("otus")));
				mTreeBlockList.add(treeBlock);
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private void setRootAttributes() {
		getElement().setAttribute("version", "0.8");
		getElement().setAttribute("generator", getClass().getName());
		getElement().setPrefix("nex");
		getElement().removeAttribute("id");
	}

	protected DocumentImpl() {
	}

	/**
	 * THis method creates an otus element and appends it to the document root.
	 * 
	 * @author rvosa
	 */
	public OTUs createOTUs() {
		OTUsImpl otus = new OTUsImpl(getDocument());
		mOtusList.add(otus);
		getElement().appendChild(otus.getElement());
		return otus;
	}

	/**
	 * This method creates a trees element and appends it to the document root.
	 * Because NeXML requires that trees elements have an id reference attribute
	 * to specify the otus element it refers to, the equivalent OTUs object
	 * needs to be passed in here.
	 * 
	 * @author rvosa
	 */
	public TreeBlock createTreeBlock(OTUs otus) {
		TreeBlockImpl treeBlock = new TreeBlockImpl(getDocument());
		mTreeBlockList.add(treeBlock);
		getElement().appendChild(treeBlock.getElement());
		treeBlock.setOTUs(otus);
		return treeBlock;
	}

	@Override
	String getTagName() {
		return "nexml";
	}

	/**
	 * This method creates the characters element and appends it to the document
	 * root. Because NeXML requires that characters elements have an id
	 * reference attribute to specify the otus element it refers to, the
	 * equivalent OTUs object needs to be passed in here. In addition,
	 * characters elements need to specify the concrete subclass they implement
	 * (the xsi:type business). XXX Here, this subclass is set to StandardCells.
	 * Hopefully we come up with a better way to do this.
	 * 
	 * @author rvosa
	 */
	public CategoricalMatrix createCategoricalMatrix(OTUs otus) {
		CategoricalMatrixImpl categoricalMatrix = new CategoricalMatrixImpl(
				getDocument());
		mMatrices.add(categoricalMatrix);
		getElement().appendChild(categoricalMatrix.getElement());
		categoricalMatrix.setOTUs(otus);
		categoricalMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":StandardCells");
		return categoricalMatrix;
	}

	/**
	 * This method creates the characters element and appends it to the document
	 * root. Because NeXML requires that characters elements have an id
	 * reference attribute to specify the otus element it refers to, the
	 * equivalent OTUs object needs to be passed in here. In addition,
	 * characters elements need to specify the concrete subclass they implement
	 * (the xsi:type business). XXX Here, this subclass is set to
	 * ContinuousCells. Hopefully we come up with a better way to do this.
	 * 
	 * @author rvosa
	 */
	public ContinuousMatrix createContinuousMatrix(OTUs otus) {
		ContinuousMatrixImpl continuousMatrix = new ContinuousMatrixImpl(
				getDocument());
		mMatrices.add(continuousMatrix);
		getElement().appendChild(continuousMatrix.getElement());
		continuousMatrix.setOTUs(otus);
		continuousMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":ContinuousCells");
		return continuousMatrix;
	}

	public String getXmlString() {
		StringWriter stringWriter = new StringWriter();
		try {
			Source source = new DOMSource(getElement());
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return stringWriter.getBuffer().toString();
	}

	public List<OTUs> getOTUsList() {
		return mOtusList;
	}
	
	public List<Matrix<?>> getMatrices() {
	    return mMatrices;
	}

	public List<TreeBlock> getTreeBlockList() {
		return mTreeBlockList;
	}

	public String getId() {
		return null;
	}

	public void setLabel(String label) {

	}

	public String getLabel() {
		return null;
	}

}
