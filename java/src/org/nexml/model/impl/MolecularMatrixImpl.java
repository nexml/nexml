package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nexml.model.Character;
import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.MolecularMatrix;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MolecularMatrixImpl extends
		MatrixImpl<CharacterState> implements MolecularMatrix {
	
	private Set<CharacterStateSet> mCharacterStateSets = new HashSet<CharacterStateSet>();
	private MolecularCharacterStateSetImpl mMolecularCharacterStates = null;
	
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
	protected MolecularMatrixImpl(Document document) {
		super(document);
	}
	
	/**
	 * This is equivalent to creating a <states> element, i.e.
	 * a container for state elements, polymorphic_state_set elements
	 * and uncertain_state_set elements. The states elements are children
	 * of the format element (to which the matrix holds a reference).
	 * If the format element object doesn't exist yet it's created here
	 * @author rvosa
	 */
	public CharacterStateSet createCharacterStateSet() {
		CharacterStateSetImpl characterStateSet = new CharacterStateSetImpl(getDocument());
		mCharacterStateSets.add(characterStateSet);
		getFormatElement().insertBefore( characterStateSet.getElement(), getFormatElement().getFirstChild() );
		return characterStateSet;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MolecularMatrix#getCharacterStateSets()
	 */
	public Set<CharacterStateSet> getCharacterStateSets() {
		return Collections.unmodifiableSet(mCharacterStateSets);
	}
	
	/**
	 * This method creates a char element, i.e. a column definition.
	 * Because NeXML requires for categorical matrices that these
	 * column definitions have an attribute to reference the 
	 * applicable state set, the state set object needs to be passed
	 * in here, from which the attribute's value is set. 
	 * @author rvosa
	 */
	public Character createCharacter(CharacterStateSet characterStateSet) {
		CharacterImpl character = new CharacterImpl(getDocument());
		addThing(character);
		character.getElement().setAttribute("states", characterStateSet.getId());
		getFormatElement().appendChild(character.getElement());
		return character;
	}	

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MolecularMatrix#getDNACharacterStateSet()
	 */
	public CharacterStateSet getDNACharacterStateSet() {
	    if (mMolecularCharacterStates == null){
	        mMolecularCharacterStates = new MolecularCharacterStateSetImpl(getDocument());
	    }
	    CharacterStateSet result = mMolecularCharacterStates.getDNAStateSet();
	    CharacterStateSetImpl characterStateSet = (CharacterStateSetImpl)result;
	    if (mCharacterStateSets.add(characterStateSet)){
	    	Element characterStateSetElement = characterStateSet.getElement();
	    	Element formatElementChild = (Element) getFormatElement().getFirstChild();
	    	if ( characterStateSetElement.getOwnerDocument() != getDocument() ) {
	    		/**
	    		 * XXX
	    		 * Under some obscure conditions (i.e. running the whole test suite at once)
	    		 * an exception is thrown saying that characterStateSetElement originates from
	    		 * a different DOM document then the current one UNLESS we do the import here.
	    		 * Obviously there's something wrong elsewhere in the code - but I can't figure
	    		 * it out.
	    		 */
	    		characterStateSetElement = (Element) getDocument().importNode(characterStateSetElement,true);
	    	}
	    	getFormatElement().insertBefore( characterStateSetElement, formatElementChild );
	        mMolecularCharacterStates.fillDNAStateSet();
	    }
	    return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MolecularMatrix#getRNACharacterStateSet()
	 */
	public CharacterStateSet getRNACharacterStateSet() {
        if (mMolecularCharacterStates == null){
            mMolecularCharacterStates = new MolecularCharacterStateSetImpl(getDocument());
        }
        CharacterStateSet result = mMolecularCharacterStates.getRNAStateSet();
        CharacterStateSetImpl characterStateSet = (CharacterStateSetImpl)result;
        if (mCharacterStateSets.add(characterStateSet)){
	    	Element characterStateSetElement = characterStateSet.getElement();
	    	Element formatElementChild = (Element) getFormatElement().getFirstChild();
	    	if ( characterStateSetElement.getOwnerDocument() != getDocument() ) {
	    		/**
	    		 * XXX
	    		 * Under some obscure conditions (i.e. running the whole test suite at once)
	    		 * an exception is thrown saying that characterStateSetElement originates from
	    		 * a different DOM document then the current one UNLESS we do the import here.
	    		 * Obviously there's something wrong elsewhere in the code - but I can't figure
	    		 * it out.
	    		 */
	    		characterStateSetElement = (Element) getDocument().importNode(characterStateSetElement,true);
	    	}
	    	getFormatElement().insertBefore( characterStateSetElement, formatElementChild );        	
            mMolecularCharacterStates.fillRNAStateSet();
        }
        return result;
     }
    
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MolecularMatrix#getProteinCharacterStateSet()
	 */
    public CharacterStateSet getProteinCharacterStateSet(){
        if (mMolecularCharacterStates == null){
            mMolecularCharacterStates = new MolecularCharacterStateSetImpl(getDocument());
        }
        CharacterStateSet result = mMolecularCharacterStates.getProteinStateSet();
        CharacterStateSetImpl characterStateSet = (CharacterStateSetImpl)result;
        if (mCharacterStateSets.add(characterStateSet)){
	    	Element characterStateSetElement = characterStateSet.getElement();
	    	Element formatElementChild = (Element) getFormatElement().getFirstChild();
	    	if ( characterStateSetElement.getOwnerDocument() != getDocument() ) {
	    		/**
	    		 * XXX
	    		 * Under some obscure conditions (i.e. running the whole test suite at once)
	    		 * an exception is thrown saying that characterStateSetElement originates from
	    		 * a different DOM document then the current one UNLESS we do the import here.
	    		 * Obviously there's something wrong elsewhere in the code - but I can't figure
	    		 * it out.
	    		 */
	    		characterStateSetElement = (Element) getDocument().importNode(characterStateSetElement,true);
	    	}
	    	getFormatElement().insertBefore( characterStateSetElement, formatElementChild );        	
            mMolecularCharacterStates.fillProteinStateSet();
        }
        return result;
    }

 }
