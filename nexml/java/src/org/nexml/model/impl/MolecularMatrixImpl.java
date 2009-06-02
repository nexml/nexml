package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nexml.model.Character;
import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.MatrixCell;
import org.nexml.model.MolecularMatrix;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MolecularMatrixImpl extends
		MatrixImpl<CharacterState> implements MolecularMatrix {
	
	private Set<CharacterStateSet> mCharacterStateSets = new HashSet<CharacterStateSet>();
	private MolecularCharacterStateSetImpl mMolecularCharacterStates = null;
	public static final String DNA = "Dna";
	public static final String RNA = "Rna";
	public static final String Protein = "Protein";
	
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
	protected MolecularMatrixImpl(Document document, Element element, OTUsImpl otus, String typeName) {
		super(document, element);
		for ( Element stateSetElement : getChildrenByTagName( getFormatElement(), CharacterStateSetImpl.getTagNameClass() ) ) {
			mMolecularCharacterStates = createCharacterStateSet(stateSetElement,typeName);  // make specific types
		}
		for ( Element characterElement : getChildrenByTagName( getFormatElement(), CharacterImpl.getTagNameClass() ) ) {
			createCharacter(characterElement);
		}
		for ( Element row : getChildrenByTagName( getMatrixElement(), "row") ) {
			OTU otu = otus.getThingById(row.getAttribute("otu"));
			for (Element cellElement : getChildrenByTagName(row, MatrixCellImpl.getTagNameClass() ) ) {
				Character character = getThingById(cellElement.getAttribute("char"));
				String stateId = cellElement.getAttribute("state");
				CharacterState state = character.getCharacterStateSet().lookupCharacterStateById(stateId);
				MatrixCell<CharacterState> matrixCell = createMatrixCell(otu, character, cellElement);				
				matrixCell.setValue(state);
			}
			for ( Element seqElement : getChildrenByTagName(row, "seq")) {
				String seq = seqElement.getTextContent();
				String[] states = element.getAttribute(XSI_PREFIX+":type").indexOf("Standard") > 0 
					? seq.split("\\s+") : seq.split("\\s*");
				int k = 0;
				STATE: for ( int j = 0; j < states.length; j++ ) {
					if ( states[j].length() == 0 ) {
						continue STATE;
					}
					Character character = getCharacterByIndex(k);
					CharacterState state = character.getCharacterStateSet().lookupCharacterStateBySymbol(states[j]);
					getCell(otu, character).setValue(state);							
					k++;
				}
				row.removeChild(seqElement);
			}
		}
		setOTUs(otus);
	}	
	
	protected MolecularCharacterStateSetImpl createCharacterStateSet(Element statesElement,String typeName) {
		MolecularCharacterStateSetImpl charStateSet = new MolecularCharacterStateSetImpl(getDocument(),statesElement);
		for ( Element stateElement : getChildrenByTagName(statesElement,"state") ) {
			CharacterState characterState = charStateSet.createCharacterState(stateElement);
			charStateSet.addThing(characterState);
		}
		for ( Element stateElement : getChildrenByTagName(statesElement,"uncertain_state_set") ) {
			CharacterState characterState = charStateSet.createUncertainCharacterState(stateElement);
			charStateSet.addThing(characterState);
		}	
		for ( Element stateElement : getChildrenByTagName(statesElement,"polymorphic_state_set") ) {
			CharacterState characterState = charStateSet.createPolymorphicCharacterState(stateElement);
			charStateSet.addThing(characterState);
		}
		mCharacterStateSets.add(charStateSet); // XXX Make this into a setter?
		return charStateSet;
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
	
	protected Character createCharacter(Element element) {
		CharacterImpl character = new CharacterImpl(getDocument(),element);
		addThing(character);
		String stateSetId = element.getAttribute("states");
		CharacterStateSet stateSet = lookupCharacterStateSetById(stateSetId);
		character.setCharacterStateSet(stateSet);
		return character;
	}	
	
	protected CharacterStateSet lookupCharacterStateSetById(String stateSetId) {
		if ( null == stateSetId ) {
			return null;
		}
		for ( CharacterStateSet stateSet : mCharacterStateSets ) {
			if ( stateSetId.equals(stateSet.getId()) ) {
				return stateSet;
			}
		}
		return null;
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

	public CharacterState parseSymbol(String symbol) {
		CharacterState state = null;
		FIND_IN_SETS: for ( CharacterStateSet stateSet : getCharacterStateSets() ) {
			state = stateSet.lookupCharacterStateBySymbol(symbol);
			if ( null != state ) {
				return state;
			}
			break FIND_IN_SETS;
		}
		state = mMolecularCharacterStates.lookupCharacterStateBySymbol(symbol);
		return state;
	}

 }
