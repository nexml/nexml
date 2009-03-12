package org.nexml.model.impl;

import java.util.Iterator;
import java.util.List;

import org.nexml.model.Dictionary;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;

class OTUsImpl extends SetManager<OTU> implements OTUs {

	
	public void addDictionaryToSet(String setName, Dictionary dictionary) {
		// TODO Auto-generated method stub

	}

	private void addOTU(OTU otu) {
		addThing(otu);
	}

	public void addOTUToSet(String setName, OTU otu) {
		addToSet(setName, otu);
	}

	public OTU createOTU() {
		OTU otu = new OTUImpl();
		addOTU(otu);
		return otu;
	}

	public void createOTUSet(String setName) {
		createSet(setName);
	}

	public List<OTU> getAllOTUs() {
		return getThings();
	}

	public List<OTU> getOTUsFromSet(String setName) {
		return getFromSet(setName);
	}

	public void removeDictionaryFromSet(String setName) {
		// TODO Auto-generated method stub
	}

	public void removeOTU(OTU otu) {
		removeThing(otu);
	}

	public void removeOTUFromSet(String setName, OTU otu) {
		// TODO Auto-generated method stub

	}

	public Iterator<OTU> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	String getTagName() { 
		return "otus";
	}
}
