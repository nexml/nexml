package org.nexml.model.impl;

import java.util.Iterator;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.nexml.model.FloatEdge;
import org.nexml.model.IntEdge;
import org.nexml.model.Network;
import org.nexml.model.OTU;
import org.nexml.model.Tree;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TreeBlockImpl extends OTUsLinkableImpl<Network<?>> implements
		TreeBlock {
	static String getTagNameClass() {
		return "trees";
	}

	public TreeBlockImpl(Document document) {
		super(document);
	}

	/**
	 * This method creates a network with edge lengths which are integers. Here
	 * we also create a tree element, and set its xsi:type attribute to
	 * nex:IntNetwork
	 * 
	 * @author rvosa
	 */
	public TreeBlockImpl(Document document, Element item,
			Map<String, OTU> originalOTUIds) {
		super(document, item);

		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression otusExpr = xpath
					.compile(TreeImpl.getTagNameClass());
			Object result = otusExpr.evaluate(this.getElement(),
					XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element thisTreeElement = (Element) nodes.item(i);
				if (thisTreeElement.getAttribute(XSI_PREFIX + ":type").equals(
						NEX_PREFIX + ":IntTree")) {
					TreeImpl<IntEdge> tree = new IntTreeImpl(document,
							thisTreeElement, originalOTUIds);
					this.addThing(tree);
				} else if (thisTreeElement.getAttribute(XSI_PREFIX + ":type")
						.equals(NEX_PREFIX + ":FloatTree")) {
					TreeImpl<FloatEdge> tree = new FloatTreeImpl(document,
							thisTreeElement, originalOTUIds);
					this.addThing(tree);
				} else {
					throw new RuntimeException("tree type ["
							+ thisTreeElement
									.getAttribute(XSI_PREFIX + ":type")
							+ "] not supported.");
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public Network<IntEdge> createIntNetwork() {
		IntNetworkImpl network = new IntNetworkImpl(getDocument());
		addThing(network);
		getElement().appendChild(network.getElement());
		network.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":IntNetwork");
		return network;
	}

	/**
	 * This method creates a network with edge lengths which are floats. Here we
	 * also create a tree element, and set its xsi:type attribute to
	 * nex:FloatNetwork
	 * 
	 * @author rvosa
	 */
	public Network<FloatEdge> createFloatNetwork() {
		FloatNetworkImpl network = new FloatNetworkImpl(getDocument());
		addThing(network);
		getElement().appendChild(network.getElement());
		network.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":FloatNetwork");
		return network;
	}

	@Override
	public String getTagName() {
		return getTagNameClass();
	}

	/**
	 * This method creates a tree with edge lengths which are floats. Here we
	 * also create a tree element, and set its xsi:type attribute to
	 * nex:FloatTree
	 * 
	 * @author rvosa
	 */
	public Tree<FloatEdge> createFloatTree() {
		FloatTreeImpl tree = new FloatTreeImpl(getDocument());
		getElement().appendChild(tree.getElement());
		tree.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":FloatTree");
		return tree;
	}

	/**
	 * This method creates a tree with edge lengths which are ints. Here we also
	 * create a tree element, and set its xsi:type attribute to nex:IntTree
	 * 
	 * @author rvosa
	 */
	public Tree<IntEdge> createIntTree() {
		IntTreeImpl tree = new IntTreeImpl(getDocument());
		getElement().appendChild(tree.getElement());
		tree.getElement().setAttributeNS(XSI_NS, XSI_PREFIX + ":type",
				NEX_PREFIX + ":IntTree");
		return tree;
	}

	public Iterator<Network<?>> iterator() {
		return getThings().iterator();
	}
}
