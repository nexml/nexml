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
		
		
	}
}
