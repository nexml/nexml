package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CategoricalMatrixImpl extends
		MatrixImpl<CharacterState> implements CategoricalMatrix {
	private Element mFormatElement;
	private Element mMatrixElement;	
	
	public CategoricalMatrixImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	private Set<CharacterStateSet> mCharacterStateSets = new HashSet<CharacterStateSet>();

	public CharacterStateSet createCharacterStateSet() {
		CharacterStateSetImpl characterStateSet = new CharacterStateSetImpl(getDocument());
		mCharacterStateSets.add(characterStateSet);
		if ( null == getFormatElement() ) {
			setFormatElement( getDocument().createElement("format") );
			getElement().insertBefore( getFormatElement(), getElement().getFirstChild() );
		}
		getFormatElement().insertBefore( characterStateSet.getElement(), getFormatElement().getFirstChild() );
		return characterStateSet;
	}

	public Set<CharacterStateSet> getCharacterStateSets() {
		return Collections.unmodifiableSet(mCharacterStateSets);
	}
	
	private Element getFormatElement() {
		return mFormatElement;
	}
	
	private void setFormatElement(Element formatElement) {
		mFormatElement = formatElement;
	}
	
	private Element getMatrixElement() {
		return mMatrixElement;
	}

    public CharacterStateSet getDNACharacterStateSet() {
        // TODO Auto-generated method stub
        return null;
    }
    public CharacterStateSet getRNACharacterStateSet() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public CharacterStateSet getProteinCharacterStateSet(){

        return null;
    }
}
