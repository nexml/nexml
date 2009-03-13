package org.nexml.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.UncertainCharacterState;
import org.w3c.dom.Document;

class MolecularCharacterStateSetImpl extends CharacterStateSetImpl{

    public MolecularCharacterStateSetImpl(Document document) {
        super(document);
        // TODO Auto-generated constructor stub
    }

    private static MolecularCharacterStateSetImpl DNAStateSet = null;
    private static MolecularCharacterStateSetImpl RNAStateSet = null;
    private static MolecularCharacterStateSetImpl ProteinStateSet = null;

    public Set<CharacterState> getCharacterStates() {
        return mCharacterStates;
    }

    public void setCharacterStates(Set<CharacterState> characterStates) {
    }

    public CharacterState createCharacterState() {
        return null;
    }


    
    CharacterStateSet getDNAStateSet(){
        if (DNAStateSet == null){
            DNAStateSet = new MolecularCharacterStateSetImpl(getDocument());
        }
        return (CharacterStateSet)DNAStateSet;
    }

    void fillDNAStateSet(){
        DNAStateSet.mCharacterStates = new HashSet<CharacterState>();
        CharacterState aState = DNAStateSet.createCharacterState("A");
        DNAStateSet.mCharacterStates.add(aState);
        System.out.println("Lookup returned: " + DNAStateSet.lookupCharacterStateBySymbol("A"));
        aState.setLabel("s1");
        System.out.println("Element is " + DNAStateSet.getElement());
        CharacterState cState = DNAStateSet.createCharacterState("C");
        DNAStateSet.getCharacterStates().add(cState);
        cState.setLabel("s2");
        System.out.println("Element is " + DNAStateSet.getElement());
        System.out.println("FirstChild is " + DNAStateSet.getElement().getFirstChild());
        CharacterState gState = createCharacterState("G");
        DNAStateSet.getCharacterStates().add(gState);
        gState.setLabel("s3");
        CharacterState tState = createCharacterState("T");
        DNAStateSet.getCharacterStates().add(tState);
        tState.setLabel("s4");
        Set<CharacterState> kSet = new HashSet<CharacterState>(2);
        kSet.add(gState);
        kSet.add(tState);
        UncertainCharacterState kState = createUncertainCharacterState("K", kSet);
        DNAStateSet.getCharacterStates().add(kState);
        kState.setLabel("s5");
        Set<CharacterState> mSet = new HashSet<CharacterState>(2);
        mSet.add(aState);
        mSet.add(cState);
        UncertainCharacterState mState = createUncertainCharacterState("M", mSet);
        DNAStateSet.getCharacterStates().add(mState);
        mState.setLabel("s6");
        Set<CharacterState> rSet = new HashSet<CharacterState>(2);
        rSet.add(aState);
        rSet.add(gState);
        UncertainCharacterState rState = createUncertainCharacterState("R", rSet);
        DNAStateSet.getCharacterStates().add(rState);
        rState.setLabel("s7");
        Set<CharacterState> sSet = new HashSet<CharacterState>(2);
        sSet.add(cState);
        sSet.add(gState);
        UncertainCharacterState sState = createUncertainCharacterState("S", sSet);
        DNAStateSet.getCharacterStates().add(sState);
        sState.setLabel("s8");
        Set<CharacterState> wSet = new HashSet<CharacterState>(2);
        wSet.add(aState);
        wSet.add(tState);
        UncertainCharacterState wState = createUncertainCharacterState("W", wSet);
        DNAStateSet.getCharacterStates().add(wState);
        wState.setLabel("s9");
        Set<CharacterState> ySet = new HashSet<CharacterState>(2);
        ySet.add(aState);
        ySet.add(tState);
        UncertainCharacterState yState = createUncertainCharacterState("Y",ySet);
        DNAStateSet.getCharacterStates().add(yState);
        yState.setLabel("s10");
        Set<CharacterState> bSet = new HashSet<CharacterState>(3);
        bSet.add(cState);
        bSet.add(gState);
        bSet.add(tState);
        UncertainCharacterState bState = createUncertainCharacterState("B",bSet);
        DNAStateSet.getCharacterStates().add(bState);
        bState.setLabel("s11");
        Set<CharacterState> dSet = new HashSet<CharacterState>(3);
        dSet.add(aState);
        dSet.add(gState);
        dSet.add(tState);
        UncertainCharacterState dState = createUncertainCharacterState("D",dSet);
        DNAStateSet.getCharacterStates().add(dState);
        dState.setLabel("s12");
        Set<CharacterState> hSet = new HashSet<CharacterState>(3);
        hSet.add(aState);
        hSet.add(cState);
        hSet.add(tState);
        UncertainCharacterState hState = createUncertainCharacterState("H",dSet);
        DNAStateSet.getCharacterStates().add(hState);
        hState.setLabel("s13");
        Set<CharacterState> vSet = new HashSet<CharacterState>(3);
        vSet.add(aState);
        vSet.add(cState);
        vSet.add(gState);
        UncertainCharacterState vState = createUncertainCharacterState("V",vSet);
        DNAStateSet.getCharacterStates().add(vState);
        vState.setLabel("s14");
        Set<CharacterState> nSet = new HashSet<CharacterState>(4);
        nSet.add(aState);
        nSet.add(cState);
        nSet.add(gState);
        nSet.add(tState);
        UncertainCharacterState nState = createUncertainCharacterState("N",nSet);
        DNAStateSet.getCharacterStates().add(nState);
        nState.setLabel("s15");
        UncertainCharacterState xState = createUncertainCharacterState("X",nSet);
        DNAStateSet.getCharacterStates().add(xState);
        xState.setLabel("s16");
        Set <CharacterState> gapSet = new HashSet<CharacterState>();
        UncertainCharacterState gapState = createUncertainCharacterState("-",gapSet);
        DNAStateSet.getCharacterStates().add(gapState);
        gapState.setLabel("s17");
        Set <CharacterState> unKnownSet = new HashSet<CharacterState>();
        unKnownSet.add(aState);
        unKnownSet.add(cState);
        unKnownSet.add(gState);
        unKnownSet.add(tState); //others, through gap?
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
        UncertainCharacterState unKnownState = createUncertainCharacterState("?",unKnownSet);
        DNAStateSet.getCharacterStates().add(unKnownState);
        unKnownState.setLabel("s18");
        unKnownState.setSymbol("?");
    }

    public CharacterStateSet getRNAStateSet(){
        if (RNAStateSet == null){
            DNAStateSet = new MolecularCharacterStateSetImpl(getDocument());
        }
        return (CharacterStateSet)RNAStateSet;
    }
    
    void fillRNAStateSet(){
        if (DNAStateSet == null){
            getDNAStateSet();
            fillDNAStateSet();
        }
        Document document = getDocument();
        RNAStateSet = new MolecularCharacterStateSetImpl(document);
        RNAStateSet.mCharacterStates = new HashSet<CharacterState>();
        
        CharacterState tState = null;
        CharacterState uState = null;
        for (CharacterState c : DNAStateSet.getCharacterStates()){
            if ("T".equals(c.getSymbol())){
                tState = c;
                uState = new CharacterStateImpl(getDocument());
                uState.setLabel(c.getLabel());
                uState.setSymbol("U");
                RNAStateSet.mCharacterStates.add(uState);

            }
            else
                RNAStateSet.mCharacterStates.add(c);
        }
        for (CharacterState c : DNAStateSet.mCharacterStates){
            if (c instanceof UncertainCharacterState){
                UncertainCharacterState u = (UncertainCharacterState)c;
                if (u.getStates() != null){
                if (u.getStates().contains(tState)){
                    u.getStates().remove(tState);
                    u.getStates().add(uState);
                }
                }
                else
                    System.out.println("Empty symbol: " + u.getSymbol());
            }
        }
        if (tState != null)
            RNAStateSet.mCharacterStates.remove(tState);
    }
    
    

    public CharacterStateSet getProteinStateSet(){
        if (ProteinStateSet == null){
            ProteinStateSet = new MolecularCharacterStateSetImpl(getDocument());
        }
        return (CharacterStateSet)ProteinStateSet;
    }
        
        
    void fillProteinStateSet(){    
        Document document = getDocument();
        ProteinStateSet.mCharacterStates = new HashSet<CharacterState>();
        CharacterState aState = new CharacterStateImpl(document);
        ProteinStateSet.mCharacterStates.add(aState);
        aState.setLabel("s1");
        aState.setSymbol("A");
        CharacterState cState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(cState);
        cState.setLabel("s2");
        cState.setSymbol("C");
        CharacterState dState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(dState);
        dState.setLabel("s3");
        dState.setSymbol("D");
        CharacterState eState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(eState);
        eState.setLabel("s4");
        eState.setSymbol("E");
        CharacterState fState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(fState);
        fState.setLabel("s5");
        fState.setSymbol("F");
        CharacterState gState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(gState);
        gState.setLabel("s6");
        gState.setSymbol("G");
        CharacterState hState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(hState);
        hState.setLabel("s7");
        hState.setSymbol("H");
        CharacterState iState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(iState);
        iState.setLabel("s8");
        iState.setSymbol("I");
        CharacterState kState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(kState);
        kState.setLabel("s9");
        kState.setSymbol("K");
        CharacterState lState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(lState);
        lState.setLabel("s10");
        lState.setSymbol("L");
        CharacterState mState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(mState);
        mState.setLabel("s11");
        mState.setSymbol("M");
        CharacterState nState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(nState);
        nState.setLabel("s12");
        nState.setSymbol("N");
        CharacterState pState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(pState);
        pState.setLabel("s13");
        pState.setSymbol("P");
        CharacterState qState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(qState);
        qState.setLabel("s14");
        qState.setSymbol("Q");
        CharacterState rState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(rState);
        rState.setLabel("s15");
        rState.setSymbol("R");
        CharacterState sState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(sState);
        sState.setLabel("s16");
        sState.setSymbol("S");
        CharacterState tState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(tState);
        tState.setLabel("s17");
        tState.setSymbol("T");
        CharacterState vState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(vState);
        vState.setLabel("s18");
        vState.setSymbol("V");
        CharacterState wState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(wState);
        wState.setLabel("s19");
        wState.setSymbol("W");
        CharacterState yState = new CharacterStateImpl(document);
        ProteinStateSet.getCharacterStates().add(yState);
        yState.setLabel("s20");
        yState.setSymbol("Y");

    }

    
    
}
