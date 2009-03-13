package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An ordered set with named subsets.
 * 
 * @param <T> the type we're storing.
 */
abstract class SetManager<T> extends AnnotatableImpl {

	/** {@code mThings} is an ordered set - i.e. no duplicates. */
	private final List<T> mOrderedSet = new ArrayList<T>();

	/** Named subsets of {@code mThings}. */
	private final Map<String, Set<T>> mSubsets = new HashMap<String, Set<T>>();

	/**
	 * A work-around for no multiple inheritance: we need to pass this stuff up
	 * the type hierarchy to {@code NexmlWritableImpl}.
	 * 
	 * @param document this {@code NexmlWritable}'s root DOM {@code Document}.
	 */
	public SetManager(Document document) {
		super(document);
	}

	/**
	 * To work-around for no multiple inheritance: we need to pass this stuff up
	 * the type hierarchy to {@code NexmlWritableImpl}.
	 * 
	 * @param document this {@code NexmlWritable}'s root DOM {@code Document}.
	 * @param element the wrapped {@code Element} of this {@code NexmlWritable}.
	 */
	public SetManager(Document document, Element element) {
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
		for (Set<T> set : mSubsets.values()) {
			set.remove(thing);
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
		createSubset(subsetName);
		mSubsets.get(subsetName).add(thing);
	}

	/**
	 * Create subset named {@subsetName}.
	 * 
	 * @param subsetName the name of the subset we're creating.
	 */
	protected void createSubset(String subsetName) {
		if (!mSubsets.containsKey(subsetName)) {
			mSubsets.put(subsetName, new HashSet<T>());
		}
	}

	/**
	 * Get the subset named {@subsetName}.
	 * 
	 * @param subsetName see description.
	 */
	protected List<T> getSubset(String subsetName) {
		List<T> list = new ArrayList<T>();
		for (T thing : mOrderedSet) {
			if (mSubsets.get(subsetName).contains(thing)) {
				list.add(thing);
			}
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Get the names of the subsets.
	 * 
	 * @return the names of the subset.
	 */
	public Set<String> getSubsetNames() {
		return mSubsets.keySet();
	}
}