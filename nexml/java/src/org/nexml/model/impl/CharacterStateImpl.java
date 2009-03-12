package org.nexml.model.impl;

import org.nexml.model.CharacterState;
import org.w3c.dom.Document;

class CharacterStateImpl extends NexmlWritableImpl implements
		CharacterState {

	public CharacterStateImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	private Object mSymbol;
	
	@Override
	String getTagName() {
		return "state";
	}

	public Object getSymbol() {
		return mSymbol;
	}

	public void setSymbol(Object symbol) {
		mSymbol = symbol;
	}

}
