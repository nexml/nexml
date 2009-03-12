package org.nexml.model;

import org.junit.Test;

public class TestParse {
	@Test
	public void parse() {
		try {
			Document document = DocumentFactory
					.parse("http://dbhack1.googlecode.com/svn/trunk/data/nexml/02_dogfish_no_taxrefs.xml");
			
			document.getXmlString();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
