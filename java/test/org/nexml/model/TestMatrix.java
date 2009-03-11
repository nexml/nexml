package org.nexml.model;

import junit.framework.Assert;

import org.junit.Test;

public class TestMatrix {

	@Test
	public void testMatrix() {
		Document doc = DocumentFactory.createDocument();
		CategoricalMatrix categoricalMatrix = doc.createCategoricalMatrix();
		OTUs mammals = doc.createOTUs();
		mammals.setLabel("mammals");
		OTU chimp = mammals.createOTU();
		chimp.setLabel("chimp");
		categoricalMatrix.setOTUs(mammals);
		Assert.assertEquals("categoricalMatrix.getOTUS should be mammals",
				mammals, categoricalMatrix.getOTUs());

		CharacterStateSet characterStateSet = categoricalMatrix
				.createCategoricalCharacterStateSet();

		Assert.assertEquals("characterStateSet should be in categoricalMatrix", characterStateSet,
				categoricalMatrix.getCategoricalCharacterStateSets().iterator().next());
		
		CharacterState red = characterStateSet.createCharacterState();
		red.setLabel("red");
		red.setSymbol(1);
		Assert.assertEquals("red.getLabel should be red", "red", red.getLabel());
		Assert.assertEquals("red.getSymbol should be 1", 1, red.getSymbol());

		Character hairColor = categoricalMatrix.createCharacter();
		
		MatrixCell<CharacterState> cell = categoricalMatrix.getCell(chimp, hairColor);
		cell.setValue(red);
		Assert.assertEquals("cell.getValue should be red", red, cell.getValue()); 
	}
}
