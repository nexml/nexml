package org.nexml.model;

import junit.framework.Assert;

import org.junit.Test;

public class TestMatrix {

	@Test
	public void testMatrix() {
		Document doc = DocumentFactory.createDocument();
		Matrix matrix = doc.createMatrix();
		OTUs mammals = doc.createOTUs();
		mammals.setLabel("mammals");
		OTU chimp = mammals.createOTU();
		chimp.setLabel("chimp");
		matrix.setOTUs(mammals);
		Assert.assertEquals("matrix.getOTUS should be mammals", mammals, matrix.getOTUs());
		
	}
}
