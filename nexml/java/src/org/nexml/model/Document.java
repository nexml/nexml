package org.nexml.model;

public interface Document extends NexmlWritable {
	TreeBlock createTreeBlock(OTUs otus);

	OTUs createOTUs();

	CategoricalMatrix createCategoricalMatrix(OTUs otus);
	
	String getXmlString();
	
}
