package org.nexml.model;

public interface Document extends NexmlWritable {
	TreeBlock createTreeBlock();

	OTUs createOTUs();

	CategoricalMatrix createCategoricalMatrix();
	
	
}
