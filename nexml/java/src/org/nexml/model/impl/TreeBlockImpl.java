package org.nexml.model.impl;

import org.nexml.model.FloatEdge;
import org.nexml.model.IntEdge;
import org.nexml.model.Network;
import org.nexml.model.Tree;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Document;

public class TreeBlockImpl extends OTUsLinkableImpl<Network<?>> implements TreeBlock {

	public TreeBlockImpl(Document document) {
		super(document);
	}

	public Network<IntEdge> createIntNetwork() {
		IntNetworkImpl network = new IntNetworkImpl(getDocument());
		addThing(network);
		getElement().appendChild(network.getElement());
		network.getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "nex:IntNetwork");
		return network;
	}
	
	public Network<FloatEdge> createFloatNetwork() {
		FloatNetworkImpl network = new FloatNetworkImpl(getDocument());
		addThing(network);
		getElement().appendChild(network.getElement());
		network.getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "nex:FloatNetwork");		
		return network;
	}

	@Override
	public String getTagName() {
		return "trees";
	}

	public Tree<FloatEdge> createFloatTree() {
		FloatTreeImpl tree = new FloatTreeImpl(getDocument());
		getElement().appendChild(tree.getElement());
		tree.getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "nex:FloatTree");
		return tree;
	}

	public Tree<IntEdge> createIntTree() {
		IntTreeImpl tree = new IntTreeImpl(getDocument());
		getElement().appendChild(tree.getElement());
		tree.getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "nex:IntTree");		
		return tree;
	}
}
