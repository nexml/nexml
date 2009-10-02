package org.nexml.model;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;

public class TestMatrix {

	@Test
	public void testMatrix() {
		Document doc = null;
		try {
			doc = DocumentFactory.createDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		OTUs mammals = doc.createOTUs();
		CategoricalMatrix categoricalMatrix = doc
				.createCategoricalMatrix(mammals);
		mammals.setLabel("mammals");
		OTU chimp = mammals.createOTU();
		chimp.setLabel("chimp");
		categoricalMatrix.setOTUs(mammals);
		Assert.assertEquals("categoricalMatrix.getOTUS should be mammals",
				mammals, categoricalMatrix.getOTUs());

		CharacterStateSet characterStateSet = categoricalMatrix
				.createCharacterStateSet();

		Assert.assertEquals("characterStateSet should be in categoricalMatrix",
				characterStateSet, categoricalMatrix.getCharacterStateSets()
						.iterator().next());
		
		Assert.assertEquals("characterStateSet should be in categoricalMatrix", characterStateSet,
				categoricalMatrix.getCharacterStateSets().iterator().next());
		

		CharacterState red = characterStateSet.createCharacterState(1);
		red.setLabel("red");
		Assert
				.assertEquals("red.getLabel should be red", "red", red
						.getLabel());
		Assert.assertEquals("red.getSymbol should be 1", 1, red.getSymbol());

		Character hairColor = categoricalMatrix.createCharacter(characterStateSet);

		MatrixCell<CharacterState> cell = categoricalMatrix.getCell(chimp,
				hairColor);
		cell.setValue(red);
		Assert
				.assertEquals("cell.getValue should be red", red, cell
						.getValue());

		Assert.assertEquals("should be red", red, categoricalMatrix.getCell(
				chimp, hairColor).getValue());
	}
}
