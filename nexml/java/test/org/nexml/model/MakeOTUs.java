package org.nexml.model;

import org.junit.Test;

public class MakeOTUs {
	@Test
	void makeOTUs() {
		Document doc = DocumentFactory.createDocument();
		OTUs mammals = doc.createOTUs();
		OTU hippopotamus = mammals.createSegment();
		hippopotamus.setLabel("hippo");

		OTU gorilla = mammals.createSegment();
		gorilla.setLabel("gorilla");
		
		OTU chimp = mammals.createSegment();
		chimp.setLabel("chimp");
		
		mammals.createSegmentSet("primates");
			
		mammals.addSegmentToSet("primates", gorilla);
		mammals.addSegmentToSet("chimp", chimp);
		
		
	}
}
