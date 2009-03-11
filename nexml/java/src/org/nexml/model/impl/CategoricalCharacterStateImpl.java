package org.nexml.model.impl;

import org.nexml.model.CategoricalCharacterState;

public class CategoricalCharacterStateImpl extends NexmlWritableImpl implements
		CategoricalCharacterState {

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
