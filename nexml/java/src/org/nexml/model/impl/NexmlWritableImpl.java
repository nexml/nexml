package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.nexml.model.NexmlWritable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Base DOM implementation of {@code NexmlWritable}.
 */
abstract class NexmlWritableImpl implements NexmlWritable {
	private Document mDocument = null;
	private Element mElement;
	protected static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
	protected static String XSI_PREFIX = "xsi";
	protected static String NEX_PREFIX = "nex";

	/** Default constructor. */
	protected NexmlWritableImpl() {
	}

	/**
	 * This constructor is intended for situations where we create a document
	 * from scratch: the DocumentFactory will pass in a DOM Document object, and
	 * subsequently created objects will all carry around a reference to it so
	 * that they can instantiate Element objects from the right Document object
	 * and insert them in the right location in the element tree. @author rvosa
	 * 
	 * @param document
	 */
	protected NexmlWritableImpl(Document document) {
		mDocument = document;
		mElement = document.createElementNS(DEFAULT_NAMESPACE, getTagName());
		getElement().setAttribute("id", "a" + UUID.randomUUID());
	}

    /**
     * Protected constructors are intended for recursive parsing, i.e.
     * starting from the root element (which maps onto DocumentImpl) we
     * traverse the element tree such that for every child element that maps
     * onto an Impl class the containing class calls that child's protected
     * constructor, passes in the element of the child. From there the 
     * child takes over, populates itself and calls the protected 
     * constructors of its children. These should probably be protected
     * because there is all sorts of opportunity for outsiders to call
     * these in the wrong context, passing in the wrong elements etc.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
	protected NexmlWritableImpl(Document document,Element element) {
		mDocument = document;
		mElement = element;
	}

	/**
	 * Get the root DOM document of this {@code NexmlWritable}.
	 * 
	 * @return the root DOM document of this {@code NexmlWritable}.
	 */
	/**
	 * The DOM document object here is typically used for creating
	 * new Element objects.
	 * @return A DOM document object
	 */
	protected final Document getDocument() {
		return mDocument;
	}

	/**
	 * Get all child elements of {@code element} named {@tagName}.
	 * 
	 * @param element see description.
	 * @param tagName see description.
	 * @return all child elements of {@code element} named {@tagName}.
	 */
	protected List<Element> getChildrenByTagName(Element element, String tagName) {
		List<Element> result = new ArrayList<Element>();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			String localName = children.item(i).getNodeName();
			if (null != localName && localName.equals(tagName)) {
				result.add((Element) children.item(i));
			}
		}
		return result;
	}

	/**
	 * Get the wrapped DOM element of this {@code NexmlWritable}.
	 * 
	 * @return the wrapped DOM element of this {@code NexmlWritable}.
	 */
	/**
	 * This returns the equivalent NeXML element for the
	 * invocant object.
	 * @return a DOM Element object
	 */
	protected final Element getElement() {
		return mElement;
	}

	/**
	 * Getter.
	 * 
	 * @return the label.
	 */
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#getLabel()
	 */
	public String getLabel() {
		return getElement().getAttribute("label");
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		getElement().setAttribute("label", label);
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#getId()
	 */
	public String getId() { 
		return getElement().getAttribute("id");
	}
	
	/**
	 * This method returns the NeXML element name that the
	 * {@code NexmlWritable} object is equivalent to.
	 * @return the (XML) tag name of this {@code NexmlWritable}.
	 */

	abstract String getTagName();
	
}
