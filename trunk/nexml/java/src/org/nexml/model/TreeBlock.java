package org.nexml.model;

public interface TreeBlock extends OTUsLinkable {
	Network<IntEdge> createIntNetwork();

	Network<FloatEdge> createFloatNetwork();

	Tree createTree();
}
