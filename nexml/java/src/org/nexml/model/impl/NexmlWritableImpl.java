package org.nexml.model.impl;

import java.util.UUID;

import org.nexml.model.NexmlWritable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class NexmlWritableImpl implements NexmlWritable {
	private Document mDocument = null;
	private String mId;
	private Element mElement;
	protected static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
	protected static String XSI_PREFIX = "xsi";
	protected static String NEX_PREFIX = "nex";

	protected NexmlWritableImpl() {
	}
	
	/**
	 * This constructor is intended for situations where we create
	 * a document from scratch: the DocumentFactory will pass in a
	 * DOM Document object, and subsequently created objects will
	 * all carry around a reference to it so that they can instantiate
	 * Element objects from the right Document object and insert them
	 * in the right location in the element tree. @author rvosa
	 * @param document
	 */
	public NexmlWritableImpl(Document document) {
		mId = "a" + UUID.randomUUID();
		mDocument = document;
		mElement = document.createElementNS(DEFAULT_NAMESPACE, getTagName());
		getElement().setAttribute("id", getId());
	}	
	
	/**
	 * This constructor is intended for calls while we are traversing
	 * an existing DOM tree: in this constructor we set up the references
	 * between our Impl objects and their equivalent DOM Element objects
	 * (so that we can modify them in place). We make one immediate 
	 * modification to the Element object: we change the id attribute, so that
	 * we don't have to worry about id clashes.
	 * @param document
	 * @param element
	 */
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
	
	public String getId() { 
		return mId;
	}
	
	
	abstract String getTagName();

		
}
