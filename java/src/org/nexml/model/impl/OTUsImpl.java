package org.nexml.model.impl;

import java.util.Iterator;
import java.util.List;

import org.nexml.model.Annotation;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.w3c.dom.Document;

class OTUsImpl extends SetManager<OTU> implements OTUs {

	
	public OTUsImpl(Document document) {
		super(document);		
	}

	private void addOTU(OTU otu) {
		addThing(otu);
	}

	public void addOTUToSet(String setName, OTU otu) {
		addToSet(setName, otu);
	}

	public OTU createOTU() {
		OTUImpl otu = new OTUImpl(getDocument());
		addOTU(otu);
		getElement().appendChild(otu.getElement());
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

    public void addAnnotationToSet(String setName, Annotation annotation) {
        // TODO Auto-generated method stub
        
    }

    public void removeAnnotationFromSet(Annotation annotation) {
        // TODO Auto-generated method stub
        
    }
}
