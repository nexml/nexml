package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.nexml.model.Annotatable;
import org.nexml.model.Subset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SubsetImpl extends AnnotatableImpl implements Subset {
	Set<Annotatable> mThings = new HashSet<Annotatable>();
	SetManagerImpl<?> mSetManager = null;
	
	/**
	 * Class version of {@code getTagName()}.
	 * 
	 * @return the tag name.
	 */
	static String getTagNameClass() {
		return "set";
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
	protected SubsetImpl(Document document,SetManagerImpl<?> setManager) {
		super(document);
		mSetManager = setManager;
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
	protected SubsetImpl(Document document, Element element,SetManagerImpl<?> setManager) {
		super(document, element);
		mSetManager = setManager;
	}	
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}	

	@Override
	public void addThing(Annotatable annotatable) {
		String tagName = ((NexmlWritableImpl)annotatable).getTagName();
		if ( getSetManager().getPermissibleSetContents().contains(tagName) ) {
			mThings.add(annotatable);	
			setIdrefs();
		}
		else {
			throw new Error("Can't add a "+tagName+" to this subset");
		}
	}

	@Override
	public void removeThing(Annotatable annotatable) {		
		mThings.remove(annotatable);
		setIdrefs();
	}
	
	private SetManagerImpl<?> getSetManager() {
		return mSetManager;
	}
	
	private void setIdrefs() {		
		Map<String,Set<String>> idRefs = new HashMap<String, Set<String>>();
		for ( Annotatable thing : mThings ) {
			String tagName = ((AnnotatableImpl)thing).getTagName();
			if ( ! idRefs.containsKey(tagName) ) {
				idRefs.put(tagName, new HashSet<String>());
			}
			Set<String> ids = idRefs.get(tagName);
			ids.add(thing.getId());
		}
		for ( String attributeName : idRefs.keySet() ) {
			Set<String> ids = idRefs.get(attributeName);
	        StringBuffer buffer = new StringBuffer();
	        Iterator<String> iter = ids.iterator();
	        while (iter.hasNext()) {
	            buffer.append(iter.next());
	            if (iter.hasNext()) {
	                buffer.append(' ');
	            }
	        }
	        getElement().setAttribute(attributeName, buffer.toString());
		}
	}

	@Override
	public Set<Annotatable> getThings() {
		Set<Annotatable> unmodifiableResultSet = new HashSet<Annotatable>();
		unmodifiableResultSet.addAll(mThings);		
		return Collections.unmodifiableSet(unmodifiableResultSet);
	}

	@Override
	public boolean containsThing(Annotatable annotatable) {
		return mThings.contains(annotatable);
	}

}
