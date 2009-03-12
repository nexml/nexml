package org.nexml.model;

import java.util.List;

public interface Document extends NexmlWritable {
	TreeBlock createTreeBlock(OTUs otus);

	OTUs createOTUs();

	CategoricalMatrix createCategoricalMatrix(OTUs otus);

	List<OTUs> getOTUsList();

	List<TreeBlock> getTreeBlockList();

	String getXmlString();

}
