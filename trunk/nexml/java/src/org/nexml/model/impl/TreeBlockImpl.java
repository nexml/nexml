package org.nexml.model.impl;

import org.nexml.model.Dictionary;
import org.nexml.model.FloatEdge;
import org.nexml.model.IntEdge;
import org.nexml.model.Network;
import org.nexml.model.Tree;
import org.nexml.model.TreeBlock;

public class TreeBlockImpl extends SetManager<Network> implements TreeBlock {

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

	public Dictionary createDictionary() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dictionary getDictionary() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDictionary(Dictionary dictionary) {
		// TODO Auto-generated method stub

	}

	public void setLabel(String label) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTagName() {
		return "trees";
	}

}
