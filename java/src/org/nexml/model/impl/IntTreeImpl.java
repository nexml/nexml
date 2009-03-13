package org.nexml.model.impl;

import java.util.Map;

import org.nexml.model.IntEdge;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class IntTreeImpl extends TreeImpl<IntEdge> implements Tree<IntEdge> {

	private static class IntEdgeFactory implements EdgeImplFactory {
		public EdgeImpl newEdgeImpl(Document rootDocument, Element element,
				String length) {
			IntEdgeImpl intEdgeImpl = new IntEdgeImpl(rootDocument, element);
			try {
				intEdgeImpl.setLength(Integer.parseInt(length));
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						"Length in IntTree wasn't an Integer", e);
			}
			return intEdgeImpl;
		}
	}

	private static EdgeImplFactory mIntEdgeFactory = new IntEdgeFactory();

	public IntTreeImpl(Document document) {
		super(document);
	}

	public IntTreeImpl(Document document, Element element,
			Map<String, OTU> originalOTUIds) {
		super(document, element, originalOTUIds, mIntEdgeFactory);

	}

	/**
	 * This creates an edge element. Because edge elements require source and
	 * target attributes, these need to be passed in here.
	 * 
	 * @author rvosa
	 */
	@Override
	public IntEdge createEdge(Node source, Node target) {
		IntEdgeImpl intEdge = new IntEdgeImpl(getDocument());
		addThing(intEdge);
		getElement().appendChild(intEdge.getElement());
		intEdge.setSource(source);
		intEdge.setTarget(target);
		return intEdge;
	}

}
