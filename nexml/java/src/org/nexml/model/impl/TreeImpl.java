package org.nexml.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.nexml.model.Edge;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class TreeImpl<E extends Edge> extends
		SetManager<NetworkObject> implements Tree<E> {

	static String getTagNameClass() {
		return "tree";
	}

	public TreeImpl(Document document) {
		super(document);
	}

	public TreeImpl(Document document, Element element,
			Map<String, OTU> originalOTUIds) {
		super(document, element);

		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			XPathExpression otusExpr = xpath
					.compile(NodeImpl.getTagNameClass());
			Object result = otusExpr.evaluate(this.getElement(),
					XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			Map<Node, String> originalNodeIds = new HashMap<Node, String>();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element thisNodeElement = (Element) nodes.item(i);
				String originalNodeId = thisNodeElement.getAttribute("id");
				NodeImpl node = new NodeImpl(document, thisNodeElement);
				originalNodeIds.put(node, originalNodeId);
				if (thisNodeElement.getAttribute("root").equals("true")) {
					node.setRoot(true);
				}
				if (originalOTUIds.containsKey(thisNodeElement
						.getAttribute("otu"))) {
					node.setOTU(originalOTUIds.get(thisNodeElement
							.getAttribute("otu")));
				}
				this.addThing(node);
			}

			XPathExpression edgesExpr = xpath.compile(EdgeImpl
					.getTagNameClass());
			Object edgesResult = edgesExpr.evaluate(this.getElement(),
					XPathConstants.NODESET);
			NodeList edges = (NodeList) edgesResult;
			for (int i = 0; i < edges.getLength(); i++) {
				Element thisEdgeElement = (Element) edges.item(i);
				try {
					Integer edgeLength = Integer.parseInt(thisEdgeElement
							.getAttribute("length"));
					IntEdgeImpl edge = new IntEdgeImpl(document,
							thisEdgeElement);
					edge.setLength(edgeLength);
					this.addThing(edge);
					for (NetworkObject networkObject : getThings()) {
						if (networkObject instanceof Node) {
							NodeImpl node = (NodeImpl) networkObject;
							if (originalNodeIds.get(node).equals(
									edge.getElement().getAttribute("source"))) {
								edge.setSource(node);
							} else if (originalNodeIds.get(node).equals(
									edge.getElement().getAttribute("target"))) {
								edge.setTarget(node);
							}
						}
					}
					edge.getElement().setAttribute("id", edge.getId());
				} catch (NumberFormatException e) {
					throw new RuntimeException("FloatEdge's not yet supported.");
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	String getTagName() {
		return getTagNameClass();
	}

	abstract public E createEdge(Node source, Node target);

	public Node createNode() {
		NodeImpl node = new NodeImpl(getDocument());
		addThing(node);
		getElement().insertBefore(node.getElement(),
				getElement().getFirstChild());
		return node;
	}

	@SuppressWarnings("unchecked")
	public Set<E> getEdges() {
		Set<E> edges = new HashSet<E>();
		for (NetworkObject networkObject : getThings()) {
			if (networkObject instanceof Edge) {
				edges.add((E) networkObject);
			}
		}
		return edges;
	}

	public Set<Node> getNodes() {
		Set<Node> nodes = new HashSet<Node>();
		for (NetworkObject networkObject : getThings()) {
			if (networkObject instanceof Node) {
				nodes.add((Node) networkObject);
			}
		}
		return nodes;
	}

	public void removeEdge(Edge edge) {
		removeThing(edge);
		getElement().removeChild(((EdgeImpl) edge).getElement());
	}

	public void removeNode(Node node) {
		removeThing(node);
		getElement().removeChild(((NodeImpl) node).getElement());
		//TODO: need to keep our tree connected.
		for (Edge edge : getEdges()) {
			if (node.equals(edge.getSource()) || node.equals(edge.getTarget())) {
				removeEdge(edge);
			}
		}
	}

}
