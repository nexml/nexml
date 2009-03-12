package org.nexml.model;

public interface TreeBlock extends Iterable<Network<?>>, OTUsLinkable {
	Network<IntEdge> createIntNetwork();

	Network<FloatEdge> createFloatNetwork();

	Tree<IntEdge> createIntTree();

	Tree<FloatEdge> createFloatTree();
}
