package org.nexml.model;

import org.junit.Assert;
import org.junit.Test;

public class TestOTUs {
	@Test
	public void makeOTUs() {
		Document doc = DocumentFactory.createDocument();
		OTUs mammals = doc.createOTUs();
		OTU hippopotamus = mammals.createOTU();
		hippopotamus.setLabel("hippo");

		OTU gorilla = mammals.createOTU();
		gorilla.setLabel("gorilla");
		
		OTU chimp = mammals.createOTU();
		chimp.setLabel("chimp");
		
		mammals.createOTUSet("primates");
			
		mammals.addOTUToSet("primates", gorilla);
		mammals.addOTUToSet("primates", chimp);
		
		Assert.assertEquals("should be 3 otus", 3, mammals.getAllOTUs().size());
		
	}
}
