package org.nexml.model;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TestSerialization {
	@Test
	public void MakeNeXML () {
		Document nexmlDocument = null;
		try {
			nexmlDocument = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertNotNull("document != null", nexmlDocument);
		Assert.assertEquals("label is null", null, nexmlDocument.getLabel());
		Assert.assertEquals("id is null", null, nexmlDocument.getId());
		Assert.assertNotNull("xml output != null", nexmlDocument.getXmlString());
	}
	@Test
	public void ValidateNeXML() {
		
		/** 
		 * First, let's create a document object. If this throws
		 * an exception, I guess that means we failed our first test.
		 */		
		Document nexmlDocument = null;
		try {
			nexmlDocument = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
		
		/**
		 * Create an OTUs block
		 */
		OTUs otus = nexmlDocument.createOTUs();
		otus.setLabel("bar");
		OTU otu = otus.createOTU();
		otu.setLabel("foo");
		
		/**
		 * Create a TreeBlock
		 */
		TreeBlock treeBlock = nexmlDocument.createTreeBlock(otus);
		
		/**
		 * Create a FloatTree
		 */
		Tree<FloatEdge> tree = treeBlock.createFloatTree();
		tree.setLabel("baz");
		Node source = tree.createNode();
		source.setOTU(otu);
		Node target = tree.createNode();
		FloatEdge edge = (FloatEdge)tree.createEdge(source,target);
		edge.setLength(0.2342);
		
		/**
		 * Create a categorical matrix
		 */
		CategoricalMatrix categoricalMatrix = nexmlDocument.createCategoricalMatrix(otus);
		CharacterStateSet characterStateSet = categoricalMatrix.createCharacterStateSet();
		CharacterState characterState = characterStateSet.createCharacterState(1);
		Set<CharacterState> members = new HashSet<CharacterState>();
		members.add(characterState);
		UncertainCharacterState uncertain = characterStateSet.createUncertainCharacterState(2, members);
		Character character1 = categoricalMatrix.createCharacter(characterStateSet);
		Character character2 = categoricalMatrix.createCharacter(characterStateSet);
		categoricalMatrix.getCell(otu, character1).setValue(characterState);
		categoricalMatrix.getCell(otu, character2).setValue(uncertain);
		
		/**
		 * Create a DNA matrix
		 */
        MolecularMatrix DNAMatrix = nexmlDocument.createMolecularMatrix(otus, MolecularMatrix.DNA);
        DNAMatrix.setLabel("Molecular Matrix");
        CharacterStateSet DNAStateSet = DNAMatrix.getDNACharacterStateSet();
        CharacterState aState = DNAStateSet.lookupCharacterStateBySymbol("A");
        CharacterState bState = DNAStateSet.lookupCharacterStateBySymbol("B");
        Character pos1 = DNAMatrix.createCharacter(DNAStateSet);
        DNAMatrix.getCell(otu, pos1).setValue(aState);
        Character pos2 = DNAMatrix.createCharacter(DNAStateSet);
        DNAMatrix.getCell(otu, pos2).setValue(bState);
        Character pos3 = DNAMatrix.createCharacter(DNAStateSet);
        DNAMatrix.getCell(otu, pos3).setValue(aState);
        
		/**
		 * Now we're going to validate the output, so first we need
		 * to jump through the xml parsing hoops.
		 */
		DocumentBuilderFactory factory =
		    DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setAttribute(
		    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
		    "http://www.w3.org/2001/XMLSchema");
		factory.setAttribute(
		    "http://java.sun.com/xml/jaxp/properties/schemaSource",
		    "http://nexml-dev.nescent.org/1.0/nexml.xsd");
		DocumentBuilder builder = null;
		
		/**
		 * We're going to turn the string output of our created
		 * NeXML into an InputStream for the parser. If we don't
		 * have valid UTF-8, we've failed this test.
		 */
		InputStream is = null;
		try {
		    //System.out.println(nexmlDocument.getXmlString());
			is = new ByteArrayInputStream(nexmlDocument.getXmlString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			Assert.assertFalse(e1.getMessage(), false);
			e1.printStackTrace();
		}
		
		/**
		 * Let's see if we get through this hoop. Is not our
		 * fault if we don't.
		 */
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
		
		/**
		 * Now let's parse our produced xl
		 */
		try {
			builder.parse(is);
		} catch (SAXException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		} catch (IOException e) {
			Assert.assertTrue(e.getMessage(), false);
			e.printStackTrace();
		}
	}

}
