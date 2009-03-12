package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.PolymorphicCharacterState;
import org.nexml.model.UncertainCharacterState;
import org.w3c.dom.Document;

class CharacterStateSetImpl extends
		SetManager<CharacterState> implements CharacterStateSet {

	public CharacterStateSetImpl(Document document) {
		super(document);
	}

	private Set<CharacterState> mCharacterStates;

	@Override
	String getTagName() {
		return "states";
	}

	public Set<CharacterState> getCharacterStates() {
		return mCharacterStates;
	}

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
		CharacterStateImpl characterStateImpl = new CharacterStateImpl(getDocument());
		getElement().appendChild(characterStateImpl.getElement());
		characterStateImpl.setSymbol(symbol);
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
		return polymorphicCharacterStateImpl;
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
		return uncertainCharacterStateImpl;
	}

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

    public CharacterState lookupCharacterStateBySymbol(String symbol){
        if (symbol == null){
            return null;
        }
        for (CharacterState cs : getCharacterStates()){
            if (symbol.equals(cs.getSymbol())){
                return cs;
            }
        }
        return null;
    }

	
}
