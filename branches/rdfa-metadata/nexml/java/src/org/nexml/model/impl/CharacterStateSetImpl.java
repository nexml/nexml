package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.CompoundCharacterState;
import org.nexml.model.PolymorphicCharacterState;
import org.nexml.model.UncertainCharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CharacterStateSetImpl extends
		SetManager<CharacterState> implements CharacterStateSet {
	Set<CharacterState> mCharacterStates = new HashSet<CharacterState>();
	private UncertainCharacterState mMissing;
	private UncertainCharacterState mGap;
	
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
	protected CharacterStateSetImpl(Document document) {
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
	protected CharacterStateSetImpl(Document document,Element element) {
		super(document,element);
	}		
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}
	
	static String getTagNameClass() {
		return "states";
	}	

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.CharacterStateSet#getCharacterStates()
	 */
	public Set<CharacterState> getCharacterStates() {
		return mCharacterStates;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.CharacterStateSet#setCharacterStates(java.util.Set)
	 */
	public void setCharacterStates(Set<CharacterState> characterStates) {
		mCharacterStates = characterStates;
	}

	/**
	 * This method creates the state element (i.e. a single state definition
	 * within a state set, inside a format element). Because state elements
	 * require a symbol attribute it needs to be passed in here.
	 * @author rvosa
	 */
	public CharacterState createCharacterState(Object symbol) {
		CharacterStateImpl characterStateImpl = null;
		if ( null != symbol && symbol.toString().equals("?") ) {			
			mMissing = createUncertainCharacterState(symbol, getCharacterStates());
			return mMissing;
		}
		else if ( null != symbol && symbol.toString().equals("-") ) {
			Set<CharacterState> members = new HashSet<CharacterState>();
			mGap = createUncertainCharacterState(symbol, members);
			addStateToMissing(mGap);
			return mGap;
		}
		else {
			characterStateImpl = new CharacterStateImpl(getDocument());
			getElement().appendChild(characterStateImpl.getElement());
			characterStateImpl.setSymbol(symbol);
			getCharacterStates().add(characterStateImpl);
			addStateToMissing(characterStateImpl);
			return characterStateImpl;
		}
	}
	
	protected void addStateToMissing(CharacterState state) {
		if ( null != mMissing ) {
		    Set<CharacterState> currentMembersOfMissing = mMissing.getStates();
		    currentMembersOfMissing.add(state);
		    mMissing.setStates(currentMembersOfMissing);
		}
	}
	
	protected CharacterState createCharacterState(Element stateElement) {
		CharacterStateImpl characterStateImpl = new CharacterStateImpl(getDocument(),stateElement);
		characterStateImpl.setSymbol(stateElement.getAttribute("symbol"));
		getCharacterStates().add(characterStateImpl);
		return characterStateImpl;
	}

	/**
	 * The method creates the polymorphic_state_set element. Because state 
	 * elements require a symbol attribute it needs to be passed in here.
	 * Polymorphic_state_set elements have two or more members, these need
	 * to be passed in here. XXX In discussion with Jeet, we concluded that 
	 * polymorphic state sets biologically are a combination of fundamental
	 * states (whereas uncertain state sets can also contain polymorphic states).
	 * THis, however, is very much open to debate. In any case, the method
	 * at present doesn't distinguish between CharacterState subclasses, but
	 * perhaps that needs to change.
	 * @author rvosa
	 */
	public PolymorphicCharacterState createPolymorphicCharacterState(
		Object symbol,
		Set<CharacterState> members) {
		PolymorphicCharacterStateImpl polymorphicCharacterStateImpl = new PolymorphicCharacterStateImpl(getDocument());
		polymorphicCharacterStateImpl.setSymbol(symbol);
		polymorphicCharacterStateImpl.setStates(members);
		getCharacterStates().add(polymorphicCharacterStateImpl);
		return polymorphicCharacterStateImpl;
	}
	
	protected PolymorphicCharacterState createPolymorphicCharacterState(Element element) {
		PolymorphicCharacterStateImpl polymorphicCharacterStateImpl = new PolymorphicCharacterStateImpl(getDocument(),element);
		populateCompoundCharacterState(polymorphicCharacterStateImpl, element);
		getCharacterStates().add(polymorphicCharacterStateImpl);
		return polymorphicCharacterStateImpl;
	}
	
	private void populateCompoundCharacterState(CompoundCharacterState state, Element element) {
		state.setSymbol(element.getAttribute("symbol"));
		Set<CharacterState> memberStates = new HashSet<CharacterState>();
		for ( Element memberElement : getChildrenByTagName(element, "member") ) {
			String memberId = memberElement.getAttribute("state");
			CharacterState memberState = lookupCharacterStateById(memberId);
			if ( null == memberState ) {
				throw new RuntimeException("Can't find member state with id "+memberId);
			}
			else {
				memberStates.add(memberState);
			}
		}
		state.setStates(memberStates);		
	}

	/**
	 * XXX see discussion for createPolymorphicCharacterState()
	 */
	public UncertainCharacterState createUncertainCharacterState(
		Object symbol,
		Set<CharacterState> members) {
		UncertainCharacterStateImpl uncertainCharacterStateImpl = new UncertainCharacterStateImpl(getDocument());
		getElement().appendChild(uncertainCharacterStateImpl.getElement());
		uncertainCharacterStateImpl.setSymbol(symbol);
		uncertainCharacterStateImpl.setStates(members);
		getCharacterStates().add(uncertainCharacterStateImpl);
		return uncertainCharacterStateImpl;
	}
	
	protected UncertainCharacterState createUncertainCharacterState(Element element) {
		UncertainCharacterState uncertainCharacterStateImpl = new UncertainCharacterStateImpl(getDocument(),element);
		populateCompoundCharacterState(uncertainCharacterStateImpl, element);
		getCharacterStates().add(uncertainCharacterStateImpl);
		return uncertainCharacterStateImpl;
	}	
	
    /**
     * Makes working with predefined sets, (e.g. molecular) easier by allowing searching for states
     * @author rvosa
     */	
	public CharacterState lookupCharacterStateById(String id) {
		if (id == null) {
			return null;
		}
        for (CharacterState cs : getCharacterStates()){
            if (id.equals(cs.getId())){
                return cs;
            }
        }
        return null;		
	}

    /**
     * Makes working with predefined sets, (e.g. molecular) easier by allowing searching for states
     * @author pmidford
     */
	public CharacterState lookupCharacterStateByLabel(String label){
        if (label == null){
            return null;
        }
        for (CharacterState cs : getCharacterStates()){
            if (label.equals(cs.getLabel())){
                return cs;
            }
        }
        return null;
    }

    /**
     * Makes working with predefined sets, (e.g. molecular) easier by allowing searching for states
     * @author pmidford
     */
    public CharacterState lookupCharacterStateBySymbol(String symbol){
        if (symbol == null){
            return null;
        }
        for (CharacterState cs : getCharacterStates()){
            if (symbol.equals(cs.getSymbol())){
                return cs;
            }
        }
        return createCharacterState(symbol);
    }

	
}
