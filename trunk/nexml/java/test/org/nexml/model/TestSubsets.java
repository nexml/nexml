package org.nexml.model;

import junit.framework.Assert;

import org.junit.Test;

public class TestSubsets {
	@Test
	public void otuSubsetTest () {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		OTU a = otus.createOTU();
		a.setLabel("a");
		OTU b = otus.createOTU();
		b.setLabel("b");
		OTU c = otus.createOTU();
		c.setLabel("c");
		OTU d = otus.createOTU();
		d.setLabel("d");
		
		//
		Subset ab = otus.createSubset("ab");
		ab.addThing(a);
		ab.addThing(b);
		Assert.assertEquals(2, ab.getThings().size());
		Assert.assertTrue(ab.getThings().contains(a));
		Assert.assertTrue(ab.getThings().contains(b));
		Assert.assertTrue(ab.containsThing(a));
		Assert.assertTrue(ab.containsThing(b));
		Assert.assertEquals(ab, otus.getSubset("ab"));
		
		//
		Subset bc = otus.createSubset("bc");
		bc.addThing(b);
		bc.addThing(c);
		Assert.assertEquals(2, bc.getThings().size());
		Assert.assertTrue(bc.getThings().contains(b));
		Assert.assertTrue(bc.getThings().contains(c));	
		Assert.assertTrue(bc.containsThing(b));
		Assert.assertTrue(bc.containsThing(c));
		Assert.assertEquals(bc, otus.getSubset("bc"));
		
		//
		Subset cd = otus.createSubset("cd");
		cd.addThing(c);
		cd.addThing(d);
		Assert.assertEquals(2, cd.getThings().size());
		Assert.assertTrue(cd.getThings().contains(c));
		Assert.assertTrue(cd.getThings().contains(d));
		Assert.assertTrue(cd.containsThing(c));
		Assert.assertTrue(cd.containsThing(d));
		Assert.assertEquals(cd, otus.getSubset("cd"));
		
		OTU e = otus.createOTU();
		e.setLabel("e");
		
		System.out.println(doc.getXmlString());
	}
	
	@Test
	public void treeSubsetTest () {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		TreeBlock treeBlock = doc.createTreeBlock(otus);
		Tree<?> tree = treeBlock.createIntTree();
		Network<?> network = treeBlock.createIntNetwork();
		
		//
		Subset treeset = treeBlock.createSubset("treeset");
		treeset.addThing(tree);
		treeset.addThing(network);
		Assert.assertEquals(2, treeset.getThings().size());
		Assert.assertTrue(treeset.getThings().contains(tree));
		Assert.assertTrue(treeset.getThings().contains(network));
		Assert.assertTrue(treeset.containsThing(tree));
		Assert.assertTrue(treeset.containsThing(network));
		Assert.assertEquals(treeset, treeBlock.getSubset("treeset"));
		
		//
		Subset nodeset = tree.createSubset("nodeset");
		Node node1 = tree.createNode();
		Node node2 = tree.createNode();
		Edge edge = tree.createEdge(node1, node2);
		nodeset.addThing(edge);
		nodeset.addThing(node1);
		Assert.assertEquals(2, nodeset.getThings().size());
		Assert.assertTrue(nodeset.getThings().contains(edge));
		Assert.assertTrue(nodeset.getThings().contains(node1));
		Assert.assertTrue(nodeset.containsThing(edge));
		Assert.assertTrue(nodeset.containsThing(node1));
		Assert.assertEquals(nodeset, tree.getSubset("nodeset"));
		
		Tree<?> floatTree = treeBlock.createFloatTree();
		floatTree.setLabel("floatTree");
		
		System.out.println(doc.getXmlString());
	}	
	
	@Test
	public void categoricalCharacterSubsetTest() {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		CategoricalMatrix matrix = doc.createCategoricalMatrix(otus);
		Subset charset = matrix.createSubset("charset");
		CharacterStateSet stateSet = matrix.createCharacterStateSet();		
		Character char1 = matrix.createCharacter(stateSet);
		Character char2 = matrix.createCharacter(stateSet);
		charset.addThing(char1);
		charset.addThing(char2);
		Assert.assertEquals(2, charset.getThings().size());
		Assert.assertTrue(charset.getThings().contains(char1));
		Assert.assertTrue(charset.getThings().contains(char2));
		Assert.assertTrue(charset.containsThing(char1));
		Assert.assertTrue(charset.containsThing(char2));
		Assert.assertEquals(charset, matrix.getSubset("charset"));
		
		System.out.println(doc.getXmlString());
	}
	
	@Test
	public void continuousCharacterSubsetTest() {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		ContinuousMatrix matrix = doc.createContinuousMatrix(otus);
		Subset charset = matrix.createSubset("charset");
		Character char1 = matrix.createCharacter();
		Character char2 = matrix.createCharacter();
		charset.addThing(char1);
		charset.addThing(char2);
		Assert.assertEquals(2, charset.getThings().size());
		Assert.assertTrue(charset.getThings().contains(char1));
		Assert.assertTrue(charset.getThings().contains(char2));
		Assert.assertTrue(charset.containsThing(char1));
		Assert.assertTrue(charset.containsThing(char2));
		Assert.assertEquals(charset, matrix.getSubset("charset"));
		
		System.out.println(doc.getXmlString());		
	}
	
	@Test
	public void dnaCharacterSubsetTest() {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		MolecularMatrix matrix = doc.createMolecularMatrix(otus, MolecularMatrix.DNA);
		CharacterStateSet stateSet = matrix.getDNACharacterStateSet();
		Subset charset = matrix.createSubset("charset");
		Character char1 = matrix.createCharacter(stateSet);
		Character char2 = matrix.createCharacter(stateSet);
		charset.addThing(char1);
		charset.addThing(char2);
		Assert.assertEquals(2, charset.getThings().size());
		Assert.assertTrue(charset.getThings().contains(char1));
		Assert.assertTrue(charset.getThings().contains(char2));
		Assert.assertTrue(charset.containsThing(char1));
		Assert.assertTrue(charset.containsThing(char2));
		Assert.assertEquals(charset, matrix.getSubset("charset"));
		
		System.out.println(doc.getXmlString());			
	}
	
	@Test
	public void proteinCharacterSubsetTest() {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		MolecularMatrix matrix = doc.createMolecularMatrix(otus, MolecularMatrix.Protein);
		CharacterStateSet stateSet = matrix.getProteinCharacterStateSet();
		Subset charset = matrix.createSubset("charset");
		Character char1 = matrix.createCharacter(stateSet);
		Character char2 = matrix.createCharacter(stateSet);
		charset.addThing(char1);
		charset.addThing(char2);
		Assert.assertEquals(2, charset.getThings().size());
		Assert.assertTrue(charset.getThings().contains(char1));
		Assert.assertTrue(charset.getThings().contains(char2));
		Assert.assertTrue(charset.containsThing(char1));
		Assert.assertTrue(charset.containsThing(char2));
		Assert.assertEquals(charset, matrix.getSubset("charset"));
		
		System.out.println(doc.getXmlString());			
	}	
	
	@Test
	public void rnaCharacterSubsetTest() {
		Document doc = DocumentFactory.safeCreateDocument();
		OTUs otus = doc.createOTUs();
		MolecularMatrix matrix = doc.createMolecularMatrix(otus, MolecularMatrix.RNA);
		CharacterStateSet stateSet = matrix.getRNACharacterStateSet();
		Subset charset = matrix.createSubset("charset");
		Character char1 = matrix.createCharacter(stateSet);
		Character char2 = matrix.createCharacter(stateSet);
		charset.addThing(char1);
		charset.addThing(char2);
		Assert.assertEquals(2, charset.getThings().size());
		Assert.assertTrue(charset.getThings().contains(char1));
		Assert.assertTrue(charset.getThings().contains(char2));
		Assert.assertTrue(charset.containsThing(char1));
		Assert.assertTrue(charset.containsThing(char2));
		Assert.assertEquals(charset, matrix.getSubset("charset"));
		
		System.out.println(doc.getXmlString());			
	}	
}
