package org.nexml.model.impl;

import org.nexml.model.FloatEdge;
import org.nexml.model.IntEdge;
import org.nexml.model.Network;
import org.nexml.model.Tree;
import org.nexml.model.TreeBlock;

public class TreeBlockImpl extends OTUsLinkableImpl<Network<?>> implements TreeBlock {

	public Network<IntEdge> createIntNetwork() {
		Network<IntEdge> network = new IntNetworkImpl();
		addThing(network);
		return network;
	}
	
	public Network<FloatEdge> createFloatNetwork() {
		Network<FloatEdge> network = new FloatNetworkImpl();
		addThing(network);
		return network;
	}

	public Tree createTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTagName() {
		return "trees";
	}
}
