package org.nexml.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private static long objectCounter = 0;
	private static Map<String,Boolean> seenIdStrings = new HashMap<String,Boolean>();
	
	protected static String XSI_URI = javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
	protected static String XMLNS_URI = javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
	protected static String XS_URI = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
	protected static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns";

	protected static String XMLNS_PRE = javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
	protected static String XSI_PRE = "xsi";
	protected static String XSD_PRE = "xsd";
	protected static String NEX_PRE = "nex";
	protected static String RDF_PRE = "rdf";
	
	protected static String XSI_TYPE = XSI_PRE + ":type";
	
	private Map<String,String> prefixForNs = new HashMap<String,String>();
	private Map<String, String> nsForPrefix = new HashMap<String,String>();
	private URI mBaseURI;

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
		if ( isIdentifiable() ) {
			identify(mElement);
		}
	}
	
	/**
	 * Generates and attaches an id attribute to the provided element.
	 * The id is intended to be unique within document scope.
	 * 
	 * @param element
	 */
	protected String identify(Element element,boolean setIdAttribute) {
		String prefix = element.getTagName();
		String id = prefix + objectCounter;
		while ( seenIdStrings.containsKey(id) ) {
			objectCounter++;
			id = prefix + objectCounter;
		}
		if ( setIdAttribute ) {
			element.setAttribute("id",id);
		}
		seenIdStrings.put(id,new Boolean(true));
		objectCounter++;	
		return id;
	}
	
	protected boolean isIdentifiable() {
		return true;
	}
	
	protected String identify(Element element) {
		return identify(element,true);
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
		seenIdStrings.put(getId(),new Boolean(true));
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
	 * Fundamental data elements must be attached to their parent elements
	 * after semantic metadata annotations and before subsets. This method
	 * ensures they are attached in the correct location
	 * 
	 * @param element
	 */
	protected void attachFundamentalDataElement(Element element) {
		attachFundamentalDataElement(getElement(), element);
	}
	
	protected void attachFundamentalDataElement(Element parent, Element child) {
		List<Element> sets = getChildrenByTagName(parent, "set");
		if ( ! sets.isEmpty() ) {
			parent.insertBefore(child, sets.get(0));			
		}
		else {
			parent.appendChild(child);
		}		
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
	 * Attaches an attribute node to the current element,
	 * such that name="value"
	 * @param name
	 * @param value
	 */
	protected void setAttribute(String name,String value) {
		getElement().setAttribute(name, value);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#setId(java.lang.String)
	 */
	public void setId(String id) {
		getElement().setAttribute("id", id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#getAbout()
	 */
	public String getAbout() {
		return getElement().getAttribute("about");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#setAbout(java.lang.String)
	 */
	public void setAbout(String about) {
		getElement().setAttribute("about", about);
	}
	
	/**
	 * This method returns the NeXML element name that the
	 * {@code NexmlWritable} object is equivalent to.
	 * @return the (XML) tag name of this {@code NexmlWritable}.
	 */
	abstract String getTagName();
	
	protected void setNameSpace(Element element,String prefix, String nameSpaceURI) {
		setNameSpace(element, prefix, nameSpaceURI, true);
	}
	
	protected void setNameSpace(Element element,String prefix, String nameSpaceURI, boolean optimize) {
		if ( null == prefix || null == nameSpaceURI ) {
			return;
		}
		if ( optimize ) {
			if ( nsForPrefix.containsKey(prefix) && prefixForNs.containsKey(nameSpaceURI) ) {				
				if ( prefixForNs.get(nameSpaceURI).equals(prefix) && nsForPrefix.get(prefix).equals(nameSpaceURI) ) {
					// here the method call simply re-affirms an existing namespace/prefix mapping
					return; // do nothing
				}
				else {
					// either the namespace of the prefix are bound to something else
					// create a new NS statement on the argument node
					element.setAttributeNS(	XMLNS_URI, XMLNS_PRE + ":" + prefix, nameSpaceURI );
				}
			}		
			else if ( ! nsForPrefix.containsKey(prefix) && ! prefixForNs.containsKey(nameSpaceURI) ) {
				// neither have been seen before, create de novo on the root element
				nsForPrefix.put(prefix, nameSpaceURI);
				prefixForNs.put(nameSpaceURI, prefix);
				while ( element.getParentNode() != null ) {
					element = (Element) element.getParentNode();
				}
				element.setAttributeNS( XMLNS_URI, XMLNS_PRE + ":" + prefix, nameSpaceURI );
			}	
			else {
				element.setAttributeNS(	XMLNS_URI, XMLNS_PRE + ":" + prefix, nameSpaceURI );
			}
		}
		else {
			element.setAttributeNS( XMLNS_URI, XMLNS_PRE + ":" + prefix, nameSpaceURI );			
		}
	}	
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#getBaseURI()
	 */
	public URI getBaseURI() {
		if ( null == mBaseURI ) {
			Element element = getElement();
			while ( element != null ) {
				String baseURIString = element.getAttribute("xml:base");
				if ( baseURIString != null && baseURIString.length() > 0 ) {
					try {
						mBaseURI = new URI(baseURIString);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				element = (Element) element.getParentNode();
			}			
		}
		return mBaseURI;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.NexmlWritable#setBaseURI(java.net.URI)
	 */
	public void setBaseURI(URI baseURI) {
		mBaseURI = baseURI;
		getElement().setAttribute("xml:base",baseURI.toString());
	}
	
	
}
