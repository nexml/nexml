package org.nexml.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestParse {
	@Test
	public void parse() throws Throwable {
		Document document = DocumentFactory
				.parse("http://dbhack1.googlecode.com/svn/trunk/data/nexml/02_dogfish_no_taxrefs.xml");
		Assert.assertEquals("should be one tree", 1, document
				.getTreeBlockList().size());
		Assert.assertEquals("should be an int tree", "'the tree'", document
				.getTreeBlockList().get(0).iterator().next().getLabel());
		TreeBlock treeBlock = document.getTreeBlockList().get(0);
		Tree<IntEdge> tree = (Tree<IntEdge>) treeBlock.iterator().next();
		boolean foundRoot = false;
		for (Node node : tree.getNodes()) {
			if (node.isRoot()) {
				foundRoot = true;
			}
			Assert.assertNotNull(node.getLabel());
			System.out.println("edge.getLabel(): " + node.getLabel());
		}
		Assert.assertTrue("should have found the root", foundRoot);

		for (Edge edge : tree.getEdges()) {
			Assert.assertTrue("length should be >=1", ((IntEdge) edge)
					.getLength() >= 1);
		}

		Map<Node, List<IntEdge>> nodeToEdge = new HashMap<Node, List<IntEdge>>();
		for (Node node : tree.getNodes()) {
			findEdges(node, tree, nodeToEdge);
		}
		System.out.println("nodeToEdge" + nodeToEdge);
		System.out.println("xmlString: " + document.getXmlString());
	}

	void findEdges(Node node, Tree<IntEdge> tree,
			Map<Node, List<IntEdge>> nodeToEdge) {
		if (!nodeToEdge.containsKey(node)) {
			nodeToEdge.put(node, new ArrayList<IntEdge>());
		}
		for (IntEdge edge : tree.getEdges()) {
			if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
				nodeToEdge.get(node).add(edge);
			}
		}
	}
}
