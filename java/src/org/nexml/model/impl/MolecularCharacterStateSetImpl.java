package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
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

	protected MolecularCharacterStateSetImpl(Document document) {
        super(document);
    }
	
	protected MolecularCharacterStateSetImpl(Document document,Element element) {
        super(document,element);
    }	

    private static MolecularCharacterStateSetImpl DNAStateSet = null;
    private static MolecularCharacterStateSetImpl RNAStateSet = null;
    private static MolecularCharacterStateSetImpl ProteinStateSet = null;

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
        if (DNAStateSet == null){
            DNAStateSet = new MolecularCharacterStateSetImpl(getDocument());
        }
        return (CharacterStateSet)DNAStateSet;
    }

    /**
     * fills the preexisting DNAStateSet.  See comments above.
     */
    void fillDNAStateSet(){
        DNAStateSet.mCharacterStates = new HashSet<CharacterState>();
        CharacterState aState = DNAStateSet.createCharacterState("A");
        DNAStateSet.getCharacterStates().add(aState);
        aState.setLabel("s1");
        CharacterState cState = DNAStateSet.createCharacterState("C");
        DNAStateSet.getCharacterStates().add(cState);
        cState.setLabel("s2");
        CharacterState gState = DNAStateSet.createCharacterState("G");
        DNAStateSet.getCharacterStates().add(gState);
        gState.setLabel("s3");
        CharacterState tState = DNAStateSet.createCharacterState("T");
        DNAStateSet.getCharacterStates().add(tState);
        tState.setLabel("s4");
        Set<CharacterState> kSet = new HashSet<CharacterState>(2);
        kSet.add(gState);
        kSet.add(tState);
        UncertainCharacterState kState = DNAStateSet.createUncertainCharacterState("K", kSet);
        DNAStateSet.getCharacterStates().add(kState);
        kState.setLabel("s5");
        Set<CharacterState> mSet = new HashSet<CharacterState>(2);
        mSet.add(aState);
        mSet.add(cState);
        UncertainCharacterState mState = DNAStateSet.createUncertainCharacterState("M", mSet);
        DNAStateSet.getCharacterStates().add(mState);
        mState.setLabel("s6");
        Set<CharacterState> rSet = new HashSet<CharacterState>(2);
        rSet.add(aState);
        rSet.add(gState);
        UncertainCharacterState rState = DNAStateSet.createUncertainCharacterState("R", rSet);
        DNAStateSet.getCharacterStates().add(rState);
        rState.setLabel("s7");
        Set<CharacterState> sSet = new HashSet<CharacterState>(2);
        sSet.add(cState);
        sSet.add(gState);
        UncertainCharacterState sState = DNAStateSet.createUncertainCharacterState("S", sSet);
        DNAStateSet.getCharacterStates().add(sState);
        sState.setLabel("s8");
        Set<CharacterState> wSet = new HashSet<CharacterState>(2);
        wSet.add(aState);
        wSet.add(tState);
        UncertainCharacterState wState = DNAStateSet.createUncertainCharacterState("W", wSet);
        DNAStateSet.getCharacterStates().add(wState);
        wState.setLabel("s9");
        Set<CharacterState> ySet = new HashSet<CharacterState>(2);
        ySet.add(aState);
        ySet.add(tState);
        UncertainCharacterState yState = DNAStateSet.createUncertainCharacterState("Y",ySet);
        DNAStateSet.getCharacterStates().add(yState);
        yState.setLabel("s10");
        Set<CharacterState> bSet = new HashSet<CharacterState>(3);
        bSet.add(cState);
        bSet.add(gState);
        bSet.add(tState);
        UncertainCharacterState bState = DNAStateSet.createUncertainCharacterState("B",bSet);
        DNAStateSet.getCharacterStates().add(bState);
        bState.setLabel("s11");
        Set<CharacterState> dSet = new HashSet<CharacterState>(3);
        dSet.add(aState);
        dSet.add(gState);
        dSet.add(tState);
        UncertainCharacterState dState = DNAStateSet.createUncertainCharacterState("D",dSet);
        DNAStateSet.getCharacterStates().add(dState);
        dState.setLabel("s12");
        Set<CharacterState> hSet = new HashSet<CharacterState>(3);
        hSet.add(aState);
        hSet.add(cState);
        hSet.add(tState);
        UncertainCharacterState hState = DNAStateSet.createUncertainCharacterState("H",dSet);
        DNAStateSet.getCharacterStates().add(hState);
        hState.setLabel("s13");
        Set<CharacterState> vSet = new HashSet<CharacterState>(3);
        vSet.add(aState);
        vSet.add(cState);
        vSet.add(gState);
        UncertainCharacterState vState = DNAStateSet.createUncertainCharacterState("V",vSet);
        DNAStateSet.getCharacterStates().add(vState);
        vState.setLabel("s14");
        Set<CharacterState> nSet = new HashSet<CharacterState>(4);
        nSet.add(aState);
        nSet.add(cState);
        nSet.add(gState);
        nSet.add(tState);
        UncertainCharacterState nState = DNAStateSet.createUncertainCharacterState("N",nSet);
        DNAStateSet.getCharacterStates().add(nState);
        nState.setLabel("s15");
        UncertainCharacterState xState = DNAStateSet.createUncertainCharacterState("X",nSet);
        DNAStateSet.getCharacterStates().add(xState);
        xState.setLabel("s16");
        Set <CharacterState> gapSet = new HashSet<CharacterState>();
        UncertainCharacterState gapState = DNAStateSet.createUncertainCharacterState("-",gapSet);
        DNAStateSet.getCharacterStates().add(gapState);
        gapState.setLabel("s17");
        Set <CharacterState> unKnownSet = new HashSet<CharacterState>();
        unKnownSet.add(aState);
        unKnownSet.add(cState);
        unKnownSet.add(gState);
        unKnownSet.add(tState); //others, through gap? XXX maybe just fundamental states?
        unKnownSet.add(kState);
        unKnownSet.add(mState);
        unKnownSet.add(rState);
        unKnownSet.add(sState);
        unKnownSet.add(wState);
        unKnownSet.add(yState);
        unKnownSet.add(bState);
        unKnownSet.add(dState);
        unKnownSet.add(hState);
        unKnownSet.add(vState);
        unKnownSet.add(nState);
        unKnownSet.add(gapState);
        UncertainCharacterState unKnownState = DNAStateSet.createUncertainCharacterState("?",unKnownSet);
        DNAStateSet.getCharacterStates().add(unKnownState);
        unKnownState.setLabel("s18");
    }

    /**
     * 
     * @return returns the singleton RNAStateSet
     *
     * See above comments for DNAStateSet
    */
    CharacterStateSet getRNAStateSet(){
        if (RNAStateSet == null){
            RNAStateSet = new MolecularCharacterStateSetImpl(getDocument());
        }
        return (CharacterStateSet)RNAStateSet;
    }

    /**
     * Fills the preexisting RNAStateSet
     * 
     * See the comments for DNAStateSet.  
     * @note copying from the DNAStateSet and changing T -> U, and in all the uncertain sets
     * seems more trouble than the cut and paste.
     */
    void fillRNAStateSet(){
        RNAStateSet.mCharacterStates = new HashSet<CharacterState>();
        CharacterState aState = RNAStateSet.createCharacterState("A");
        RNAStateSet.getCharacterStates().add(aState);
        aState.setLabel("s1");
        CharacterState cState = RNAStateSet.createCharacterState("C");
        RNAStateSet.getCharacterStates().add(cState);
        cState.setLabel("s2");
        CharacterState gState = RNAStateSet.createCharacterState("G");
        RNAStateSet.getCharacterStates().add(gState);
        gState.setLabel("s3");
        CharacterState uState = RNAStateSet.createCharacterState("U");
        RNAStateSet.getCharacterStates().add(uState);
        uState.setLabel("s4");
        Set<CharacterState> kSet = new HashSet<CharacterState>(2);
        kSet.add(gState);
        kSet.add(uState);
        UncertainCharacterState kState = RNAStateSet.createUncertainCharacterState("K", kSet);
        RNAStateSet.getCharacterStates().add(kState);
        kState.setLabel("s5");
        Set<CharacterState> mSet = new HashSet<CharacterState>(2);
        mSet.add(aState);
        mSet.add(cState);
        UncertainCharacterState mState = RNAStateSet.createUncertainCharacterState("M", mSet);
        RNAStateSet.getCharacterStates().add(mState);
        mState.setLabel("s6");
        Set<CharacterState> rSet = new HashSet<CharacterState>(2);
        rSet.add(aState);
        rSet.add(gState);
        UncertainCharacterState rState = RNAStateSet.createUncertainCharacterState("R", rSet);
        RNAStateSet.getCharacterStates().add(rState);
        rState.setLabel("s7");
        Set<CharacterState> sSet = new HashSet<CharacterState>(2);
        sSet.add(cState);
        sSet.add(gState);
        UncertainCharacterState sState = RNAStateSet.createUncertainCharacterState("S", sSet);
        RNAStateSet.getCharacterStates().add(sState);
        sState.setLabel("s8");
        Set<CharacterState> wSet = new HashSet<CharacterState>(2);
        wSet.add(aState);
        wSet.add(uState);
        UncertainCharacterState wState = RNAStateSet.createUncertainCharacterState("W", wSet);
        RNAStateSet.getCharacterStates().add(wState);
        wState.setLabel("s9");
        Set<CharacterState> ySet = new HashSet<CharacterState>(2);
        ySet.add(aState);
        ySet.add(uState);
        UncertainCharacterState yState = RNAStateSet.createUncertainCharacterState("Y",ySet);
        RNAStateSet.getCharacterStates().add(yState);
        yState.setLabel("s10");
        Set<CharacterState> bSet = new HashSet<CharacterState>(3);
        bSet.add(cState);
        bSet.add(gState);
        bSet.add(uState);
        UncertainCharacterState bState = RNAStateSet.createUncertainCharacterState("B",bSet);
        RNAStateSet.getCharacterStates().add(bState);
        bState.setLabel("s11");
        Set<CharacterState> dSet = new HashSet<CharacterState>(3);
        dSet.add(aState);
        dSet.add(gState);
        dSet.add(uState);
        UncertainCharacterState dState = RNAStateSet.createUncertainCharacterState("D",dSet);
        RNAStateSet.getCharacterStates().add(dState);
        dState.setLabel("s12");
        Set<CharacterState> hSet = new HashSet<CharacterState>(3);
        hSet.add(aState);
        hSet.add(cState);
        hSet.add(uState);
        UncertainCharacterState hState = RNAStateSet.createUncertainCharacterState("H",dSet);
        RNAStateSet.getCharacterStates().add(hState);
        hState.setLabel("s13");
        Set<CharacterState> vSet = new HashSet<CharacterState>(3);
        vSet.add(aState);
        vSet.add(cState);
        vSet.add(gState);
        UncertainCharacterState vState = RNAStateSet.createUncertainCharacterState("V",vSet);
        RNAStateSet.getCharacterStates().add(vState);
        vState.setLabel("s14");
        Set<CharacterState> nSet = new HashSet<CharacterState>(4);
        nSet.add(aState);
        nSet.add(cState);
        nSet.add(gState);
        nSet.add(uState);
        UncertainCharacterState nState = RNAStateSet.createUncertainCharacterState("N",nSet);
        RNAStateSet.getCharacterStates().add(nState);
        nState.setLabel("s15");
        UncertainCharacterState xState = RNAStateSet.createUncertainCharacterState("X",nSet);
        RNAStateSet.getCharacterStates().add(xState);
        xState.setLabel("s16");
        Set <CharacterState> gapSet = new HashSet<CharacterState>();
        UncertainCharacterState gapState = RNAStateSet.createUncertainCharacterState("-",gapSet);
        RNAStateSet.getCharacterStates().add(gapState);
        gapState.setLabel("s17");
        Set <CharacterState> unKnownSet = new HashSet<CharacterState>();
        unKnownSet.add(aState);
        unKnownSet.add(cState);
        unKnownSet.add(gState);
        unKnownSet.add(uState); //others, through gap? XXX maybe just fundamental states?
        unKnownSet.add(kState);
        unKnownSet.add(mState);
        unKnownSet.add(rState);
        unKnownSet.add(sState);
        unKnownSet.add(wState);
        unKnownSet.add(yState);
        unKnownSet.add(bState);
        unKnownSet.add(dState);
        unKnownSet.add(hState);
        unKnownSet.add(vState);
        unKnownSet.add(nState);
        unKnownSet.add(gapState);
        UncertainCharacterState unKnownState = RNAStateSet.createUncertainCharacterState("?",unKnownSet);
        RNAStateSet.getCharacterStates().add(unKnownState);
        unKnownState.setLabel("s18");
        unKnownState.setSymbol("?");
    }
    
    
    /**
     * 
     * @return returns the singleton ProteinStateSet
     *
     * See above comments for DNAStateSet
    */

    public CharacterStateSet getProteinStateSet(){
        if (ProteinStateSet == null){
            ProteinStateSet = new MolecularCharacterStateSetImpl(getDocument());
        }
        return (CharacterStateSet)ProteinStateSet;
    }
        
    /**
     * Fills the pre-existing ProteinStateSet
     * 
     * See the comments for DNAStateSet.  
     */
    void fillProteinStateSet(){    
        ProteinStateSet.mCharacterStates = new HashSet<CharacterState>();
        CharacterState aState = ProteinStateSet.createCharacterState("A");
        ProteinStateSet.getCharacterStates().add(aState);
        aState.setLabel("s1");
        CharacterState cState = ProteinStateSet.createCharacterState("C");
        ProteinStateSet.getCharacterStates().add(cState);
        cState.setLabel("s2");
        CharacterState dState = ProteinStateSet.createCharacterState("D");
        ProteinStateSet.getCharacterStates().add(dState);
        dState.setLabel("s3");
        CharacterState eState = ProteinStateSet.createCharacterState("E");
        ProteinStateSet.getCharacterStates().add(eState);
        eState.setLabel("s4");
        CharacterState fState = ProteinStateSet.createCharacterState("F");
        ProteinStateSet.getCharacterStates().add(fState);
        fState.setLabel("s5");
        CharacterState gState = ProteinStateSet.createCharacterState("G");
        ProteinStateSet.getCharacterStates().add(gState);
        gState.setLabel("s6");
        CharacterState hState = ProteinStateSet.createCharacterState("H");
        ProteinStateSet.getCharacterStates().add(hState);
        hState.setLabel("s7");
        CharacterState iState = ProteinStateSet.createCharacterState("I");
        ProteinStateSet.getCharacterStates().add(iState);
        iState.setLabel("s8");
        CharacterState kState = ProteinStateSet.createCharacterState("K");
        ProteinStateSet.getCharacterStates().add(kState);
        kState.setLabel("s9");
        CharacterState lState = ProteinStateSet.createCharacterState("L");
        ProteinStateSet.getCharacterStates().add(lState);
        lState.setLabel("s10");
        CharacterState mState = ProteinStateSet.createCharacterState("M");
        ProteinStateSet.getCharacterStates().add(mState);
        mState.setLabel("s11");
        CharacterState nState = ProteinStateSet.createCharacterState("N");
        ProteinStateSet.getCharacterStates().add(nState);
        nState.setLabel("s12");
        CharacterState pState = ProteinStateSet.createCharacterState("P");
        ProteinStateSet.getCharacterStates().add(pState);
        pState.setLabel("s13");
        CharacterState qState = ProteinStateSet.createCharacterState("Q");
        ProteinStateSet.getCharacterStates().add(qState);
        qState.setLabel("s14");
        CharacterState rState = ProteinStateSet.createCharacterState("R");
        ProteinStateSet.getCharacterStates().add(rState);
        rState.setLabel("s15");
        CharacterState sState = ProteinStateSet.createCharacterState("S");
        ProteinStateSet.getCharacterStates().add(sState);
        sState.setLabel("s16");
        CharacterState tState = ProteinStateSet.createCharacterState("T");
        ProteinStateSet.getCharacterStates().add(tState);
        tState.setLabel("s17");
        CharacterState vState = ProteinStateSet.createCharacterState("V");
        ProteinStateSet.getCharacterStates().add(vState);
        vState.setLabel("s18");
        CharacterState wState = ProteinStateSet.createCharacterState("W");
        ProteinStateSet.getCharacterStates().add(wState);
        wState.setLabel("s19");
        CharacterState yState = ProteinStateSet.createCharacterState("Y");
        ProteinStateSet.getCharacterStates().add(yState);
        yState.setLabel("s20");

    }
  
    
}
