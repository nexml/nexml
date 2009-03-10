package org.nexml.model.impl;

import java.util.UUID;

import org.nexml.model.Dictionary;
import org.nexml.model.NexmlWritable;

abstract class NexmlWritableImpl implements NexmlWritable {

	private String mLabel;
	private String mId;
	private Dictionary mDictionary;

	public NexmlWritableImpl() {
		mId = "a" + UUID.randomUUID();
	}
	
	public String getLabel() {
		return mLabel;
	}

	public void setLabel(String label) {
		mLabel = label;
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
	
	String getId() { 
		return mId;
	}
	
	
	abstract String getTagName();

		
}
