package org.nexml.model.impl;

import org.nexml.model.CharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CharacterStateImpl extends AnnotatableImpl implements
		CharacterState {

	public CharacterStateImpl(Document document) {
		super(document);
	}
	
	public CharacterStateImpl(Document document,Element element) {
		super(document,element);
	}	

	private Object mSymbol;
	
	@Override
	String getTagName() {
		return "state";
	}

	public Object getSymbol() {
		return mSymbol;
	}

	/**
	 * This method sets the symbol for a state definition. These
	 * symbols are different types depending on the data type 
	 * (as follows: DNA, RNA and AA have the IUPAC single character
	 * codes, - and ? (i.e. Strings); Standard has Integers and ?;
	 * Restriction has Integers (0 and 1). The approach taken here is
	 * to just pass in Object and call toString() on it to set the
	 * value of the symbol attribute on the state element.
	 * @author rvosa
	 */
	public void setSymbol(Object symbol) {
		mSymbol = symbol;
		getElement().setAttribute("symbol", symbol.toString());
	}

}
