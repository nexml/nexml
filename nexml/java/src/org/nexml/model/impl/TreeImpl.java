package org.nexml.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nexml.model.Edge;
import org.nexml.model.NetworkObject;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class TreeImpl<E extends Edge> extends NetworkImpl<E> implements
		Tree<E> {

	protected interface EdgeImplFactory {
		public EdgeImpl newEdgeImpl(Document rootDocument, Element element,
				String length);
	}

	static String getTagNameClass() {
		return "tree";
	}

	public TreeImpl(Document document) {
		super(document);
	}

	public TreeImpl(Document document, Element element,
			Map<String, OTU> originalOTUIds, EdgeImplFactory edgeFactory) {
		super(document, element);
		Map<Node, String> originalNodeIds = new HashMap<Node, String>();
		List<Element> nodeElements = getChildrenByTagName(this.getElement(),
				NodeImpl.getTagNameClass());
		for (Element thisNodeElement : nodeElements) {
			String originalNodeId = thisNodeElement.getAttribute("id");
			NodeImpl node = new NodeImpl(document, thisNodeElement);
			originalNodeIds.put(node, originalNodeId);
			if (thisNodeElement.getAttribute("root").equals("true")) {
				node.setRoot(true);
			}
			if (originalOTUIds.containsKey(thisNodeElement.getAttribute("otu"))) {
				node.setOTU(originalOTUIds.get(thisNodeElement
						.getAttribute("otu")));
			}
			this.addThing(node);
		}

		List<Element> edgeElements = getChildrenByTagName(this.getElement(),
				EdgeImpl.getTagNameClass());
		for (Element thisEdgeElement : edgeElements) {
			EdgeImpl edge = edgeFactory.newEdgeImpl(document, thisEdgeElement,
					thisEdgeElement.getAttribute("length"));
			this.addThing(edge);
			for (NetworkObject networkObject : getThings()) {
				if (networkObject instanceof Node) {
					Node node = (Node) networkObject;
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

	public Set<E> getEdges() {
		Set<E> edges = new HashSet<E>();
		for (NetworkObject networkObject : getThings()) {
			if (networkObject instanceof Edge) {
				@SuppressWarnings("unchecked")
				E edge = (E) networkObject;
				edges.add(edge);
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

	public void removeEdge(E edge) {
		removeThing(edge);
		getElement().removeChild(((EdgeImpl) edge).getElement());
	}

	public void removeNode(Node node) {
		removeThing(node);
		getElement().removeChild(((NodeImpl) node).getElement());
		// TODO: need to keep our tree connected.
		for (E edge : getEdges()) {
			if (node.equals(edge.getSource()) || node.equals(edge.getTarget())) {
				removeEdge(edge);
			}
		}
	}

	public Node getRoot() {
		for (Node node : getNodes()) {
			if (node.isRoot()) {
				return node;
			}
		}
		return null;
	}

}
