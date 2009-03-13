package org.nexml.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestParseTrees {

	@Test
	public void parseFloatTree() throws Throwable {
		Document document = DocumentFactory.parse(getClass()
				.getResourceAsStream("/org/nexml/model/trees.xml"));
		System.out.println(document.getXmlString());
	}

	@Test
	public void parseIntTree() throws Throwable {
		Document document = DocumentFactory.parse(getClass()
				.getResourceAsStream(
						"/org/nexml/model/02_dogfish_no_taxrefs.xml"));
		Assert.assertEquals("should be one tree", 1, document
				.getTreeBlockList().size());
		Assert.assertEquals("should be an int tree", "'the tree'", document
				.getTreeBlockList().get(0).iterator().next().getLabel());
		TreeBlock treeBlock = document.getTreeBlockList().get(0);
		Assert.assertEquals("should have same OTU in both", document
				.getOTUsList().get(0), treeBlock.getOTUs());
		@SuppressWarnings("unchecked")
		Tree<IntEdge> tree = (Tree<IntEdge>) treeBlock.iterator().next();
		boolean foundRoot = false;
		for (Node node : tree.getNodes()) {
			if (node.isRoot()) {
				foundRoot = true;
			}
			Assert.assertNotNull(node.getLabel());
			//System.out.println("edge.getLabel(): " + node.getLabel());
		}
		Assert.assertTrue("should have found the root", foundRoot);

		for (Edge edge : tree.getEdges()) {
			Assert.assertTrue("length should be >=1", ((IntEdge) edge)
					.getLength() >= 1);
		}

		Map<Node, List<IntEdge>> nodeToEdge = new HashMap<Node, List<IntEdge>>();
		for (Node node : tree.getNodes()) {
			findEdges(node, tree, nodeToEdge);
			//System.out.println("node: " + node);
			// for (IntEdge edge : nodeToEdge.get(node)) {
			// System.out.println("    edge: " + edge);
			// }
		}
		Node brevParent = null;

		for (IntEdge edge : tree.getEdges()) {
			if (edge.getTarget().getLabel().equals("S. brevirostris")) {
				brevParent = edge.getSource();
			}
		}

		for (Node node : tree.getNodes()) {

			if ("S. brevirostris".equals(node.getLabel())) {
				tree.removeNode(node);
			}

			if ("S. megalops".equals(node.getLabel())) {
				node.setLabel("S. megalopsTESTMODIFYLABEL");
			}
		}

		Node newNode = tree.createNode();
		newNode.setLabel("TEST_NEW_NODE");
		IntEdge newEdge = tree.createEdge(brevParent, newNode);
		newEdge.setLabel("TEST_NEW_EDGE");
		newEdge.setLength(33);
		Assert.assertEquals("length should be 33", Integer.valueOf(33), newEdge
				.getLength());
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
