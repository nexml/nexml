package org.nexml.model.impl;

import org.nexml.model.CharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CharacterStateImpl extends AnnotatableImpl implements
		CharacterState {

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
	protected CharacterStateImpl(Document document) {
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
	protected CharacterStateImpl(Document document,Element element) {
		super(document,element);
	}	

	private Object mSymbol;
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return "state";
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.CharacterState#getSymbol()
	 */
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
