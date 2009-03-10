package org.nexml.model.impl;

import org.nexml.model.Dictionary;
import org.nexml.model.Document;
import org.nexml.model.Matrix;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;

public class DocumentImpl extends NexmlWritableImpl implements Document {

	public Matrix createMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	public OTUs createOTUs() {
		return new OTUsImpl();
	}

	public TreeBlock createTreeBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addDictionary(Dictionary dictionary) {
		// TODO Auto-generated method stub

	}

	public Dictionary getDictionary() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLabel(String label) {
		// TODO Auto-generated method stub

	}

	@Override
	String getTagName() { 
		return "nexml"; 
	}
	
}
