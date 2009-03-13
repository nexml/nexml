package org.nexml.model;

import java.util.List;

public interface Document extends NexmlWritable {
    
	TreeBlock createTreeBlock(OTUs otus);

	OTUs createOTUs();

	CategoricalMatrix createCategoricalMatrix(OTUs otus);
	
	ContinuousMatrix createContinuousMatrix(OTUs otus);

	MolecularMatrix createMolecularMatrix(OTUs otus, String type);

	List<OTUs> getOTUsList();
	
	List<Matrix<?>> getMatrices();

	List<TreeBlock> getTreeBlockList();

	String getXmlString();

}