package org.nexml.model.impl;

import java.util.UUID;

import org.nexml.model.Dictionary;
import org.nexml.model.NexmlWritable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class NexmlWritableImpl implements NexmlWritable {
	private Document mDocument = null;
	private String mId;
	private Dictionary mDictionary;
	private Element mElement;

	protected NexmlWritableImpl() {
	}
	
	public NexmlWritableImpl(Document document) {
		mId = "a" + UUID.randomUUID();
		mDocument = document;
		mElement = document.createElementNS(DEFAULT_NAMESPACE, getTagName());
		getElement().setAttribute("id", getId());
	}	
	
	public NexmlWritableImpl(Document document,Element element) {
		mId = "a" + UUID.randomUUID();
		mDocument = document;
		mElement = element;		
		getElement().setAttribute("id", getId());
	}	
	
	protected final Document getDocument() {
		return mDocument;
	}
	
	public final Element getElement() {
		return mElement;
	}
	
	public String getLabel() {
		return getElement().getAttribute("label");
	}

	public void setLabel(String label) {
		getElement().setAttribute("label", label);
	}
	
	public Dictionary getDictionary() { 
		return mDictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		mDictionary = dictionary; 
	}
	
	public Dictionary createDictionary() { 
		// TODO method stub
		return null;
	}
	
	public String getId() { 
		return mId;
	}
	
	
	abstract String getTagName();

		
}
