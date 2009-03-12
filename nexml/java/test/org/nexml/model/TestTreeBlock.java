package org.nexml.model;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;

public class TestTreeBlock {
	@Test
	public void makeIntNetwork() {
		Document doc = null;
		try {
			doc = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OTUs mammals = doc.createOTUs();

		TreeBlock treeBlock = doc.createTreeBlock(mammals);
		Network<IntEdge> network = treeBlock.createIntNetwork();
		Node node1 = network.createNode();
		Node node2 = network.createNode();
		IntEdge edge = network.createEdge(node1, node2);
		edge.setSource(node1);
		edge.setTarget(node2);
		Assert.assertEquals("node1 == getSource is what we want", node1, edge
				.getSource());
		Assert.assertEquals("node2 == getTarget is what we want", node2, edge
				.getTarget());

		edge.setLength(34);
		Assert.assertEquals("edge.setLength should be 34", 34, edge.getLength()
				.intValue());

		OTU chimp = mammals.createOTU();
		chimp.setLabel("chimp");
		node2.setOTU(chimp);
		Assert.assertEquals("node2.getOTU should be chimp", chimp, node2
				.getOTU());
		
		System.out.println("xmlstring: " + doc.getXmlString());

	}

	@Test
	public void makeFloatNetwork() {
		Document doc = null;
		try {
			doc = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OTUs mammals = doc.createOTUs();
		mammals.setLabel("mammals");

		TreeBlock treeBlock = doc.createTreeBlock(null);

		Network<FloatEdge> floatNetwork = treeBlock.createFloatNetwork();
		Node floatNode1 = floatNetwork.createNode();
		Node floatNode2 = floatNetwork.createNode();
		FloatEdge floatEdge = floatNetwork.createEdge(floatNode1, floatNode2);
		floatEdge.setSource(floatNode1);
		floatEdge.setTarget(floatNode2);
		Assert.assertEquals(
				"floatNode1 == floatEdge.getSource is what we want",
				floatNode1, floatEdge.getSource());
		Assert.assertEquals(
				"floatNode2 == floatEdge.getTarget is what we want",
				floatNode2, floatEdge.getTarget());
	}
}
