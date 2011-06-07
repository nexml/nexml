package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nexml.model.Annotatable;
import org.nexml.model.NexmlWritable;
import org.nexml.model.SetManager;
import org.nexml.model.Subset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An ordered set with named subsets.
 * 
 * @param <T> the type we're storing.
 */

abstract class SetManagerImpl<T> extends AnnotatableImpl implements SetManager {

    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
 	 * (A work-around for no multiple inheritance: we need to pass this stuff up
	 * the type hierarchy to {@code NexmlWritableImpl}.)
	 * 
	 * @param document this {@code NexmlWritable}'s root DOM {@code Document}.
	 */	
	protected SetManagerImpl(Document document) {
		super(document);
	}

	/** {@code mThings} is an ordered set - i.e. no duplicates. */
	private final List<T> mOrderedSet = new ArrayList<T>();

	/** Named subsets of {@code mThings}. */
	private final Map<String, Subset> mSubsets = new HashMap<String, Subset>();
	
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
     * @param document the containing DOM document object. Every {@code NexmlWritable} 
     * class needs a reference to this {@code Document} so that it can create DOM element
     * objects
     * @param element the equivalent NeXML {@code Element} (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
	protected SetManagerImpl(Document document, Element element) { 
		super(document, element);
	}

	/**
	 * Add {@code thing} to this set.
	 * 
	 * @param thing that which we're adding.
	 */
	protected void addThing(T thing) {
		if (!mOrderedSet.contains(thing)) {
			mOrderedSet.add(thing);
		}
	}

	/**
	 * Get all member of this set, in order.
	 * 
	 * @return all member of this set, in order.
	 */
	protected List<T> getThings() {
		return mOrderedSet;
	}

	/**
	 * Remove {@code thing} from this set.
	 * 
	 * @param thing to be removed.
	 */
	protected void removeThing(T thing) {
		for (Subset set : mSubsets.values()) {
			set.removeThing((Annotatable)thing);
		}
		mOrderedSet.remove(thing);
	}

	/**
	 * Add {@code thing} to the subset named {@code subsetName}.
	 * 
	 * @param setName see description.
	 * @param thing see description.
	 */
	protected void addToSubset(String subsetName, T thing) {
		if (!mOrderedSet.contains(thing)) {
			throw new IllegalArgumentException(
					"thing is not already in this SetManager.");
		}
		createSubset(subsetName).addThing((Annotatable)thing);
	}
	
	protected T getThingById(String id) {
		if ( null == id ) {
			return null;
		}
		for ( T thing : mOrderedSet ) {
			if ( thing instanceof NexmlWritable ) {
				if ( id.equals(((NexmlWritable)thing).getId()) ) {
					return thing;
				}
			}
		}
		return null;
	}

	
	/**
	 * Create subset named {@subsetName}.
	 * 
	 * @param subsetName the name of the subset we're creating.
	 */
	public Subset createSubset(String subsetName) {
		return createSubset(subsetName, getElement());
	}
	
	protected Subset createSubset(String subsetName, Element parent) {
		if (!mSubsets.containsKey(subsetName)) {
			SubsetImpl subset = new SubsetImpl(getDocument(),this);
			if ( null != subsetName ) {
				subset.setLabel(subsetName);
				mSubsets.put(subsetName, subset);
			}
			else {
				mSubsets.put(subset.getId(), subset);
			}
			parent.appendChild(subset.getElement());
		}
		return mSubsets.get(subsetName);		
	}

	/**
	 * Get the subset named {@subsetName}.
	 * 
	 * @param subsetName see description.
	 */
	public Subset getSubset(String subsetName) {
		return mSubsets.get(subsetName);
	}

	/**
	 * Get the names of the subsets.
	 * 
	 * @return the names of the subset.
	 */
	public Set<String> getSubsetNames() {
		return mSubsets.keySet();
	}
	
	public Collection<Subset> getSubsets() {
		return mSubsets.values();
	}
	
	protected abstract Set<String> getPermissibleSetContents();
	
	public int getSegmentCount() {
		return getThings().size();
	}
	
	public T getSegment(int index) {
		return getThings().get(index);
	}	
		
}