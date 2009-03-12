package org.nexml.model.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.nexml.model.Document;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DocumentImpl extends AnnotatableImpl implements Document {
	private List<OTUs> mOtusList = new ArrayList<OTUs>();

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

			OTUsImpl otus = new OTUsImpl(document, (Element) nodes.item(0));
			mOtusList.add(otus);
			XPathExpression treesExpr = xpath.compile(this.getTagName() + "/"
					+ TreeBlockImpl.getTagNameClass());
			TreeBlockImpl treeBlock = new TreeBlockImpl(document,
					(Element) ((NodeList) treesExpr.evaluate(document,
							XPathConstants.NODESET)).item(0));
			treeBlock.setOTUs(otus);
			mTreeBlockList.add(treeBlock);

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

	public OTUs createOTUs() {
		OTUsImpl otus = new OTUsImpl(getDocument());
		mOtusList.add(otus);
		getElement().appendChild(otus.getElement());
		return otus;
	}

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

	public CategoricalMatrix createCategoricalMatrix(OTUs otus) {
		CategoricalMatrixImpl categoricalMatrix = new CategoricalMatrixImpl(
				getDocument());
		getElement().appendChild(categoricalMatrix.getElement());
		categoricalMatrix.setOTUs(otus);
		categoricalMatrix.getElement().setAttributeNS(XSI_NS,
				XSI_PREFIX + ":type", NEX_PREFIX + ":StandardCells");
		return categoricalMatrix;
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

	public List<TreeBlock> getTreeBlockList() {
		return mTreeBlockList;
	}

}
