package org.nexml.model;

import org.junit.Test;

public class TestTreeBlock {
	@Test
	public void makeTreeBlock() {
		Document doc = DocumentFactory.createDocument();
		TreeBlock treeBlock = doc.createTreeBlock();
		Network network = treeBlock.createNetwork();
		Node node1 = network.createNode();
		Node node2 = network.createNode();
		Edge edge = network.createEdge();
	}
}
