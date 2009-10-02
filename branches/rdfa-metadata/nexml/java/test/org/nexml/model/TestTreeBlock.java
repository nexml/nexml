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
		OTUs otus = doc.createOTUs();
		TreeBlock treeBlock = doc.createTreeBlock(otus);
		Network<IntEdge> network = treeBlock.createIntNetwork();
		Node source = network.createNode();
		Node target = network.createNode();
		IntEdge edge = network.createEdge(source,target);
		Assert.assertEquals("node1 == getSource is what we want", source, edge
				.getSource());
		Assert.assertEquals("node2 == getTarget is what we want", target, edge
				.getTarget());

		edge.setLength(34);
		Assert.assertEquals("edge.setLength should be 34", 34, edge.getLength()
				.intValue());

		OTU chimp = otus.createOTU();
		chimp.setLabel("chimp");
		target.setOTU(chimp);
		Assert.assertEquals("node2.getOTU should be chimp", chimp, target
				.getOTU());
		
		//System.out.println("xmlstring: " + doc.getXmlString());

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
		OTUs otus = doc.createOTUs();
		TreeBlock treeBlock = doc.createTreeBlock(otus);
		Network<FloatEdge> floatNetwork = treeBlock.createFloatNetwork();
		Node floatNode1 = floatNetwork.createNode();
		Node floatNode2 = floatNetwork.createNode();
		FloatEdge floatEdge = floatNetwork.createEdge(floatNode1, floatNode2);
		Assert.assertEquals(
				"floatNode1 == floatEdge.getSource is what we want",
				floatNode1, floatEdge.getSource());
		Assert.assertEquals(
				"floatNode2 == floatEdge.getTarget is what we want",
				floatNode2, floatEdge.getTarget());
	}
}
