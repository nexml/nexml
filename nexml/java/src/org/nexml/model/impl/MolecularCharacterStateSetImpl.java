package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.MolecularMatrix;
import org.nexml.model.UncertainCharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author pmidford
 * created Mar 12, 2009
 *
 */
class MolecularCharacterStateSetImpl extends CharacterStateSetImpl{
	private boolean isFilled = false;

	protected MolecularCharacterStateSetImpl(Document document) {
        super(document);
    }
	
	protected MolecularCharacterStateSetImpl(Document document, String type) {
		super(document);
		if ( MolecularMatrix.DNA.equals(type) ) {
			getDNAStateSet();
		}
		else if ( MolecularMatrix.RNA.equals(type) ) {
			getRNAStateSet();
		}
		else if ( MolecularMatrix.Protein.equals(type) ) {
			getProteinStateSet();
		}
	}
	
	protected MolecularCharacterStateSetImpl(Document document,Element element) {
        super(document,element);
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.impl.CharacterStateSetImpl#getCharacterStates()
     */
    public Set<CharacterState> getCharacterStates() {
        return mCharacterStates;
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.impl.CharacterStateSetImpl#setCharacterStates(java.util.Set)
     */
    /**
     * @param characterStates
     * @throws Error Always throws an error as molecular states can not be changed.
     *
     */
    public void setCharacterStates(Set<CharacterState> characterStates) {
        throw new Error("MolecularStates can not be set");
    }

    /**
     * 
     * @return returns the singleton DNAStateSet
     * 
     * It is uninitialized, since it seems to interact better with generation of format 
     * elements to separate generation from initialization.  
     * This separation might not work as expected if we have multiple matrices of the 
     * same molecular type.
     * TODO: Test this
     */
    CharacterStateSet getDNAStateSet(){
    	if ( ! isFilled ) {
    		fillDNAStateSet();
    		isFilled = true;
    	}
    	return this;
    }

    /**
     * fills the preexisting DNAStateSet.  See comments above.
     */
    private void fillDNAStateSet(){
        mCharacterStates = new HashSet<CharacterState>();    	
        
        // A
        CharacterState aState = createCharacterState("A");
        getCharacterStates().add(aState);

        // C
        CharacterState cState = createCharacterState("C");
        getCharacterStates().add(cState);

        // G
        CharacterState gState = createCharacterState("G");
        getCharacterStates().add(gState);

        // T
        CharacterState tState = createCharacterState("T");
        getCharacterStates().add(tState);

        // K => (G,T)
        Set<CharacterState> kSet = new HashSet<CharacterState>(2);
        kSet.add(gState);
        kSet.add(tState);
        getCharacterStates().add(createUncertainCharacterState("K", kSet));

        // M => (A,C)
        Set<CharacterState> mSet = new HashSet<CharacterState>(2);
        mSet.add(aState);
        mSet.add(cState);
        getCharacterStates().add(createUncertainCharacterState("M", mSet));

        // R => (A,G)
        Set<CharacterState> rSet = new HashSet<CharacterState>(2);
        rSet.add(aState);
        rSet.add(gState);
        getCharacterStates().add(createUncertainCharacterState("R", rSet));

        // S => (C,G)
        Set<CharacterState> sSet = new HashSet<CharacterState>(2);
        sSet.add(cState);
        sSet.add(gState);
        getCharacterStates().add(createUncertainCharacterState("S", sSet));

        // W => (A,T)
        Set<CharacterState> wSet = new HashSet<CharacterState>(2);
        wSet.add(aState);
        wSet.add(tState);
        getCharacterStates().add(createUncertainCharacterState("W", wSet));

        // Y => (A,T)
        Set<CharacterState> ySet = new HashSet<CharacterState>(2);
        ySet.add(cState);
        ySet.add(tState);
        getCharacterStates().add(createUncertainCharacterState("Y",ySet));

        // B => (C,G,T)
        Set<CharacterState> bSet = new HashSet<CharacterState>(3);
        bSet.add(cState);
        bSet.add(gState);
        bSet.add(tState);
        getCharacterStates().add(createUncertainCharacterState("B",bSet));

        // D => (A,G,T)
        Set<CharacterState> dSet = new HashSet<CharacterState>(3);
        dSet.add(aState);
        dSet.add(gState);
        dSet.add(tState);
        getCharacterStates().add(createUncertainCharacterState("D",dSet));

        // H => (A,C,T)
        Set<CharacterState> hSet = new HashSet<CharacterState>(3);
        hSet.add(aState);
        hSet.add(cState);
        hSet.add(tState);
        getCharacterStates().add(createUncertainCharacterState("H",dSet));

        // V => (A,C,G)
        Set<CharacterState> vSet = new HashSet<CharacterState>(3);
        vSet.add(aState);
        vSet.add(cState);
        vSet.add(gState);
        getCharacterStates().add(createUncertainCharacterState("V",vSet));

        // N => (A,C,G,T)
        Set<CharacterState> nSet = new HashSet<CharacterState>(4);
        nSet.add(aState);
        nSet.add(cState);
        nSet.add(gState);
        nSet.add(tState);
        UncertainCharacterState nState = this.createUncertainCharacterState("N",nSet);
        getCharacterStates().add(nState);

        // X => (A,C,G,T)
        getCharacterStates().add(createUncertainCharacterState("X",nSet));

        // - => ()
        Set <CharacterState> gapSet = new HashSet<CharacterState>();
        UncertainCharacterState gapState = createUncertainCharacterState("-",gapSet);
        getCharacterStates().add(gapState);

        // ? => (A,C,G,T,-)
        Set <CharacterState> unKnownSet = new HashSet<CharacterState>();
        unKnownSet.add(aState);
        unKnownSet.add(cState);
        unKnownSet.add(gState);
        unKnownSet.add(tState);
        unKnownSet.add(gapState);
        getCharacterStates().add(createUncertainCharacterState("?",unKnownSet));
    }

    /**
     * 
     * @return returns the singleton RNAStateSet
     *
     * See above comments for DNAStateSet
    */
    CharacterStateSet getRNAStateSet(){
    	if ( ! isFilled ) {
    		fillRNAStateSet();
    		isFilled = true;
    	}
    	return this;
    }

    /**
     * Fills the preexisting RNAStateSet
     * 
     * See the comments for DNAStateSet.  
     * @note copying from the DNAStateSet and changing T -> U, and in all the uncertain sets
     * seems more trouble than the cut and paste.
     */
    private void fillRNAStateSet(){
        mCharacterStates = new HashSet<CharacterState>();
        
        // A
        CharacterState aState = createCharacterState("A");
        getCharacterStates().add(aState);
        
        // C
        CharacterState cState = createCharacterState("C");
        getCharacterStates().add(cState);
        
        // G
        CharacterState gState = createCharacterState("G");
        getCharacterStates().add(gState);
        
        // U
        CharacterState uState = createCharacterState("U");
        getCharacterStates().add(uState);
        
        // K => (G,U)
        Set<CharacterState> kSet = new HashSet<CharacterState>(2);
        kSet.add(gState);
        kSet.add(uState);
        getCharacterStates().add(createUncertainCharacterState("K", kSet));
        
        // M => (A,C)
        Set<CharacterState> mSet = new HashSet<CharacterState>(2);
        mSet.add(aState);
        mSet.add(cState);
        getCharacterStates().add(createUncertainCharacterState("M", mSet));
        
        // R => (A,G)
        Set<CharacterState> rSet = new HashSet<CharacterState>(2);
        rSet.add(aState);
        rSet.add(gState);
        getCharacterStates().add(createUncertainCharacterState("R", rSet));
        
        // S => (C,G)
        Set<CharacterState> sSet = new HashSet<CharacterState>(2);
        sSet.add(cState);
        sSet.add(gState);
        getCharacterStates().add(createUncertainCharacterState("S", sSet));
        
        // W => (A,U)
        Set<CharacterState> wSet = new HashSet<CharacterState>(2);
        wSet.add(aState);
        wSet.add(uState);
        getCharacterStates().add(createUncertainCharacterState("W", wSet));
        
        // Y => (C,U)
        Set<CharacterState> ySet = new HashSet<CharacterState>(2);
        ySet.add(cState);
        ySet.add(uState);
        getCharacterStates().add(createUncertainCharacterState("Y",ySet));
        
        // B => (C,G,U)
        Set<CharacterState> bSet = new HashSet<CharacterState>(3);
        bSet.add(cState);
        bSet.add(gState);
        bSet.add(uState);
        getCharacterStates().add(createUncertainCharacterState("B",bSet));
        
        // D => (A,G,U)
        Set<CharacterState> dSet = new HashSet<CharacterState>(3);
        dSet.add(aState);
        dSet.add(gState);
        dSet.add(uState);
        getCharacterStates().add(createUncertainCharacterState("D",dSet));
        
        // H => (A,C,U)
        Set<CharacterState> hSet = new HashSet<CharacterState>(3);
        hSet.add(aState);
        hSet.add(cState);
        hSet.add(uState);
        getCharacterStates().add(createUncertainCharacterState("H",dSet));
        
        // V => (A,C,G)
        Set<CharacterState> vSet = new HashSet<CharacterState>(3);
        vSet.add(aState);
        vSet.add(cState);
        vSet.add(gState);
        getCharacterStates().add(createUncertainCharacterState("V",vSet));
        
        // N => (A,C,G,U)
        Set<CharacterState> nSet = new HashSet<CharacterState>(4);
        nSet.add(aState);
        nSet.add(cState);
        nSet.add(gState);
        nSet.add(uState);
        UncertainCharacterState nState = createUncertainCharacterState("N",nSet);
        getCharacterStates().add(nState);
        
        // X => (A,C,G,U)
        getCharacterStates().add(createUncertainCharacterState("X",nSet));
        
        // - => ()
        Set <CharacterState> gapSet = new HashSet<CharacterState>();
        UncertainCharacterState gapState = createUncertainCharacterState("-",gapSet);
        getCharacterStates().add(gapState);
        
        // ? => (A,C,G,U,-)
        Set <CharacterState> unKnownSet = new HashSet<CharacterState>();
        unKnownSet.add(aState);
        unKnownSet.add(cState);
        unKnownSet.add(gState);
        unKnownSet.add(uState);
        unKnownSet.add(gapState);
        UncertainCharacterState unKnownState = createUncertainCharacterState("?",unKnownSet);
        getCharacterStates().add(unKnownState);
    }
    
    
    /**
     * 
     * @return returns the singleton ProteinStateSet
     *
     * See above comments for DNAStateSet
    */

    public CharacterStateSet getProteinStateSet(){
    	if ( ! isFilled ) {
    		fillProteinStateSet();
    		isFilled = true;
    	}
    	return this;
    }
        
    /**
     * Fills the pre-existing ProteinStateSet
     * 
     * See the comments for DNAStateSet.  
     */
    private void fillProteinStateSet(){    
        mCharacterStates = new HashSet<CharacterState>();
        Set <CharacterState> xStateSet = new HashSet<CharacterState>();
        
        // Alanine
        CharacterState aState = createCharacterState("A");
        getCharacterStates().add(aState);
        xStateSet.add(aState);
        
        // Aspartic acid or asparagine
        CharacterState bState = createCharacterState("B");
        getCharacterStates().add(bState);
        xStateSet.add(bState);
        
        // Cysteine
        CharacterState cState = createCharacterState("C");
        getCharacterStates().add(cState);
        xStateSet.add(cState);
        
        // Aspartic acid
        CharacterState dState = createCharacterState("D");
        getCharacterStates().add(dState);
        xStateSet.add(dState);
        
        // Glutamic acid
        CharacterState eState = createCharacterState("E");
        getCharacterStates().add(eState);
        xStateSet.add(eState);
        
        // Phenylanine
        CharacterState fState = createCharacterState("F");
        getCharacterStates().add(fState);
        xStateSet.add(fState);
        
        // Glycine
        CharacterState gState = createCharacterState("G");
        getCharacterStates().add(gState);
        xStateSet.add(gState);
        
        // Histidine
        CharacterState hState = createCharacterState("H");
        getCharacterStates().add(hState);
        xStateSet.add(hState);
        
        // Isoleucine
        CharacterState iState = createCharacterState("I");
        getCharacterStates().add(iState);
        xStateSet.add(iState);
        
        // Lysine
        CharacterState kState = createCharacterState("K");
        getCharacterStates().add(kState);
        xStateSet.add(kState);
        
        // Leucine
        CharacterState lState = createCharacterState("L");
        getCharacterStates().add(lState);
        xStateSet.add(lState);
        
        // Methionine
        CharacterState mState = createCharacterState("M");
        getCharacterStates().add(mState);
        xStateSet.add(mState);
        
        // Asparagine
        CharacterState nState = createCharacterState("N");
        getCharacterStates().add(nState);
        xStateSet.add(nState);
        
        // Proline
        CharacterState pState = createCharacterState("P");
        getCharacterStates().add(pState);
        xStateSet.add(pState);
        
        // Glutamine
        CharacterState qState = createCharacterState("Q");
        getCharacterStates().add(qState);
        xStateSet.add(qState);
        
        // Arginine
        CharacterState rState = createCharacterState("R");
        getCharacterStates().add(rState);
        xStateSet.add(rState);
        
        // Serine
        CharacterState sState = createCharacterState("S");
        getCharacterStates().add(sState);
        xStateSet.add(sState);
        
        // Threonine
        CharacterState tState = createCharacterState("T");
        getCharacterStates().add(tState);
        xStateSet.add(tState);
        
        // Selenocysteine
        CharacterState uState = createCharacterState("U");
        getCharacterStates().add(uState);
        xStateSet.add(uState);
        
        // Valine
        CharacterState vState = createCharacterState("V");
        getCharacterStates().add(vState);
        xStateSet.add(vState);
        
        // Tryptophan
        CharacterState wState = createCharacterState("W");
        getCharacterStates().add(wState);
        xStateSet.add(wState);
        
        // Tyrosine
        CharacterState yState = createCharacterState("Y");
        getCharacterStates().add(yState);
        xStateSet.add(yState);
        
        // Glutamic acid or glutamine
        CharacterState zState = createCharacterState("Z");
        getCharacterStates().add(zState);
        xStateSet.add(zState);

        UncertainCharacterState xState = createUncertainCharacterState("X",xStateSet);
        getCharacterStates().add(xState);


    }

    /**
     * Makes working with predefined sets, (e.g. molecular) easier by allowing searching for states
     * @author rvos
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
        throw new Error("Symbol "+symbol+" not allowed in predefined state set");
    }
    
}
