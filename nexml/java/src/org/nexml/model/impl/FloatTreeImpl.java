package org.nexml.model.impl;

import java.util.Map;

import org.nexml.model.FloatEdge;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.Tree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FloatTreeImpl extends TreeImpl<FloatEdge> implements
		Tree<FloatEdge> {

	private static class FloatEdgeFactory implements EdgeImplFactory {
		public EdgeImpl newEdgeImpl(Document rootDocument, Element element,
				String length) {
			FloatEdgeImpl floatEdgeImpl = new FloatEdgeImpl(rootDocument,
					element);
			try {
				floatEdgeImpl.setLength(Double.parseDouble(length));
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						"Length in FloatTree wasn't an Double", e);
			}
			return floatEdgeImpl;
		}
	}

	public FloatTreeImpl(Document document) {
		super(document);
	}

	public FloatTreeImpl(Document rootDocument, Element thisElement,
			Map<String, OTU> originalOTUIds) {
		super(rootDocument, thisElement, originalOTUIds, new FloatEdgeFactory());
	}

	@Override
	public FloatEdge createEdge(Node source, Node target) {
		FloatEdgeImpl floatEdge = new FloatEdgeImpl(getDocument());
		addThing(floatEdge);
		getElement().appendChild(floatEdge.getElement());
		floatEdge.setSource(source);
		floatEdge.setTarget(target);
		return floatEdge;
	}

}
