package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.w3c.dom.Document;

class CharacterStateSetImpl extends
		SetManager<CharacterState> implements CharacterStateSet {

	public CharacterStateSetImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
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

	public CharacterState createCharacterState() {
		return new CharacterStateImpl(getDocument());
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
