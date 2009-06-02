package org.nexml.model.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.nexml.model.Annotation;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An {@code OTUs} implemented with DOM objects.
 */
class OTUsImpl extends SetManager<OTU> implements OTUs {

	/**
	 * Get the (XML) tag name of otus.
	 * 
	 * @return the (XML) tag name of otus.
	 */
	public static String getTagNameClass() {
		return "otus";
	}

    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
     * @param document a DOM document object
     * @author rvosa
     */
	protected OTUsImpl(Document document) {
		super(document);
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
	protected OTUsImpl(Document document, Element item) {
		super(document, item);
		for ( Element oTUElement : getChildrenByTagName(item,OTUImpl.getTagNameClass())) {
			OTUImpl otu = new OTUImpl(document,oTUElement);
			addOTU(otu);
		}
	}

	private void addOTU(OTU otu) {
		addThing(otu);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#addOTUToSet(java.lang.String, org.nexml.model.OTU)
	 */
	/** {@inheritDoc} */
	public void addOTUToSubset(String setName, OTU otu) {
		addToSubset(setName, otu);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#createOTU()
	 */
	/** {@inheritDoc} */
	public OTU createOTU() {
		OTUImpl otu = new OTUImpl(getDocument());
		addOTU(otu);
		getElement().appendChild(otu.getElement());
		return otu;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#createOTUSet(java.lang.String)
	 */
	public void createOTUSubset(String setName) {
		createSubset(setName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#getAllOTUs()
	 */
	public List<OTU> getAllOTUs() {
		return Collections.unmodifiableList(getThings());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#getOTUsFromSet(java.lang.String)
	 */
	public List<OTU> getOTUsFromSubset(String setName) {
		return getSubset(setName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#removeOTU(org.nexml.model.OTU)
	 */
	public void removeOTU(OTU otu) {
		removeThing(otu);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#removeOTUFromSet(java.lang.String, org.nexml.model.OTU)
	 */
	public void removeOTUFromSubset(String setName, OTU otu) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<OTU> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#addAnnotationToSet(java.lang.String, org.nexml.model.Annotation)
	 */
	public void addAnnotationToSubset(String setName, Annotation annotation) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.OTUs#removeAnnotationFromSet(org.nexml.model.Annotation)
	 */
	public void removeAnnotationFromSubset(Annotation annotation) {
		// TODO Auto-generated method stub

	}
}
