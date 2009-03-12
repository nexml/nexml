package org.nexml.model;

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
		}
		Assert.assertTrue("should have found the root", foundRoot);

		for (Edge edge : tree.getEdges()) {
			
		}

		System.out.println("xmlString: " + document.getXmlString());
	}
}
