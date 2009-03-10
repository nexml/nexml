package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nexml.model.Dictionary;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;

public class OTUsImpl extends NexmlWritableImpl  implements OTUs {

	private List<OTU> mOTUs = new ArrayList<OTU>();
	
	public void addDictionaryToSet(String setName, Dictionary dictionary) {
		// TODO Auto-generated method stub

	}

	public void addOTU(OTU otu) {
		mOTUs.add(otu);
	}

	public void addOTUToSet(String setName, OTU otu) {
		// TODO Auto-generated method stub

	}

	public OTU createOTU() {
		OTU otu = new OTUImpl();
		addOTU(otu);
		return otu;
	}

	public void createOTUSet(String setName) {
		// TODO Auto-generated method stub

	}

	public List<OTU> getAllOTUs() {
		return mOTUs;
	}

	public List<OTU> getOTUsFromSet(String setName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getSetNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeDictionaryFromSet(String setName) {
		// TODO Auto-generated method stub

	}

	public void removeOTU(OTU otu) {
		// TODO Auto-generated method stub

	}

	public void removeOTUFromSet(String setName, OTU otu) {
		// TODO Auto-generated method stub

	}

	public Iterator<OTU> iterator() {
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
		return "otus";
	}
}
