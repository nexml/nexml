package org.nexml.model;

public interface TreeBlock extends Iterable<Network<?>>, OTUsLinkable {
	/**
	 * Creates a new network with integer branch lengths
	 * @return a parameterized network object
	 */
	Network<IntEdge> createIntNetwork();

	/**
	 * Creates a new network with float branch lengths
	 * @return a parameterized network object 
	 */
	Network<FloatEdge> createFloatNetwork();

	/**
	 * Creates a new tree with integer branch lengths
	 * @return a parameterized tree object
	 */
	Tree<IntEdge> createIntTree();

	/**
	 * Creates a new tree with float branch lengths
	 * @return a parameterized tree object
	 */
	Tree<FloatEdge> createFloatTree();
}
