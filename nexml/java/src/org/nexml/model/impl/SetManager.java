package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SetManager<T> extends NexmlWritableImpl {
	private final Map<String, Set<T>> mSets = new HashMap<String, Set<T>>();
	private final List<T> mThings = new ArrayList<T>();

	protected void addThing(T thing) {
		if (!mThings.contains(thing)) {
			mThings.add(thing);
		}
	}
	
	protected void removeThing(T thing) { 
		for(Set<T> set : mSets.values()) { 
			set.remove(thing);
		}
		mThings.remove(thing);
	}

	protected List<T> getThings() {
		return mThings;
	}

	protected void addToSet(String setName, T thing) {
		createSet(setName);
		mSets.get(setName).add(thing);
	}
	
	protected void createSet(String setName) {
		if (!mSets.containsKey(setName)) {
			mSets.put(setName, new HashSet<T>());
		}
	}

	protected List<T> getFromSet(String setName) {
		List<T> list = new ArrayList<T>();
		for (T thing : mThings) {
			if (mSets.get(setName).contains(thing)) {
				list.add(thing);
			}
		}
		return list;
	}
	
	public Set<String> getSetNames() { 
		return mSets.keySet();
	}
}