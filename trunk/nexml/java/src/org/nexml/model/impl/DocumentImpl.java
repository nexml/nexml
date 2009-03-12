package org.nexml.model.impl;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.Document;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;

public class DocumentImpl extends NexmlWritableImpl implements Document {

	public OTUs createOTUs() {
		return new OTUsImpl();
	}

	public TreeBlock createTreeBlock() {
		return new TreeBlockImpl();
	}

	@Override
	String getTagName() {
		return "nexml";
	}

	public CategoricalMatrix createCategoricalMatrix() {
		return new CategoricalMatrixImpl();
	}

}
