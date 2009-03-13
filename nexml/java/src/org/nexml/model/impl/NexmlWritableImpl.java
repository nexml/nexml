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
	private String mId;
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
	public NexmlWritableImpl(Document document) {
		mId = "a" + UUID.randomUUID();
		mDocument = document;
		mElement = document.createElementNS(DEFAULT_NAMESPACE, getTagName());
		getElement().setAttribute("id", getId());
	}

	/**
	 * This constructor is intended for calls while we are traversing an
	 * existing DOM tree: in this constructor we set up the references between
	 * our Impl objects and their equivalent DOM Element objects (so that we can
	 * modify them in place). We make one immediate modification to the Element
	 * object: we change the id attribute, so that we don't have to worry about
	 * id clashes.
	 * 
	 * @param document
	 * @param element
	 */
	public NexmlWritableImpl(Document document, Element element) {
		mId = "a" + UUID.randomUUID();
		mDocument = document;
		mElement = element;
		getElement().setAttribute("id", getId());
	}

	/**
	 * Get the root DOM document of this {@code NexmlWritable}.
	 * 
	 * @return the root DOM document of this {@code NexmlWritable}.
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
	public final Element getElement() {
		return mElement;
	}

	/**
	 * Getter.
	 * 
	 * @return the label.
	 */
	public String getLabel() {
		return getElement().getAttribute("label");
	}

	/**
	 * Setter.
	 * 
	 * @param value.
	 */
	public void setLabel(String label) {
		getElement().setAttribute("label", label);
	}

	/**
	 * Getter.
	 * 
	 * @return the id.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Get the (XML) tag name of this {@code NexmlWritable}.
	 * 
	 * @return the (XML) tag name of this {@code NexmlWritable}.
	 */
	abstract String getTagName();

}
