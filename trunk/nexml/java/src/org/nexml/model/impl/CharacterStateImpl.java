package org.nexml.model.impl;

import org.nexml.model.CharacterState;

public class CharacterStateImpl extends NexmlWritableImpl implements
		CharacterState {

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
