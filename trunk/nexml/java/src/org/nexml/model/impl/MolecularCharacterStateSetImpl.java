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
        return getCharacterStates();
    }

    public void setCharacterStates(Set<CharacterState> characterStates) {
    }

    public CharacterState createCharacterState() {
        return null;
    }

    
    public CharacterStateSet getDNAStateSet(){
        if (DNAStateSet != null){
            return (CharacterStateSet)DNAStateSet;
        }
        Document document = getDocument();
        DNAStateSet = new MolecularCharacterStateSetImpl(document);
        DNAStateSet.setCharacterStates(new HashSet<CharacterState>());
        CharacterState aState = new CharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(aState);
        aState.setLabel("s1");
        aState.setSymbol("A");
        CharacterState cState = new CharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(cState);
        cState.setLabel("s2");
        cState.setSymbol("C");
        CharacterState gState = new CharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(gState);
        gState.setLabel("s3");
        gState.setSymbol("G");
        CharacterState tState = new CharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(tState);
        tState.setLabel("s4");
        tState.setSymbol("T");
        UncertainCharacterState kState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(kState);
        kState.setLabel("s5");
        kState.setSymbol("K");
        Set<CharacterState> kSet = new HashSet<CharacterState>(2);
        kSet.add(gState);
        kSet.add(tState);
        kState.setStates(kSet);
        UncertainCharacterState mState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(mState);
        mState.setLabel("s6");
        mState.setSymbol("M");
        Set<CharacterState> mSet = new HashSet<CharacterState>(2);
        mSet.add(aState);
        mSet.add(cState);
        mState.setStates(mSet);
        UncertainCharacterState rState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(rState);
        rState.setLabel("s7");
        rState.setSymbol("R");
        Set<CharacterState> rSet = new HashSet<CharacterState>(2);
        rSet.add(aState);
        rSet.add(gState);
        rState.setStates(rSet);
        UncertainCharacterState sState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(sState);
        sState.setLabel("s8");
        sState.setSymbol("S");
        Set<CharacterState> sSet = new HashSet<CharacterState>(2);
        sSet.add(cState);
        sSet.add(gState);
        sState.setStates(sSet);
        UncertainCharacterState wState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(wState);
        wState.setLabel("s9");
        wState.setSymbol("W");
        Set<CharacterState> wSet = new HashSet<CharacterState>(2);
        wSet.add(aState);
        wSet.add(tState);
        wState.setStates(wSet);
        UncertainCharacterState yState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(yState);
        yState.setLabel("s10");
        yState.setSymbol("Y");
        Set<CharacterState> ySet = new HashSet<CharacterState>(2);
        ySet.add(aState);
        ySet.add(tState);
        mState.setStates(ySet);
        UncertainCharacterState bState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(bState);
        bState.setLabel("s11");
        bState.setSymbol("B");
        Set<CharacterState> bSet = new HashSet<CharacterState>(3);
        bSet.add(cState);
        bSet.add(gState);
        bSet.add(tState);
        bState.setStates(bSet);
        UncertainCharacterState dState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(dState);
        dState.setLabel("s12");
        dState.setSymbol("D");
        Set<CharacterState> dSet = new HashSet<CharacterState>(3);
        dSet.add(aState);
        dSet.add(gState);
        dSet.add(tState);
        dState.setStates(dSet);
        UncertainCharacterState hState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(hState);
        hState.setLabel("s13");
        hState.setSymbol("H");
        Set<CharacterState> hSet = new HashSet<CharacterState>(3);
        hSet.add(aState);
        hSet.add(gState);
        hSet.add(tState);
        hState.setStates(hSet);
        UncertainCharacterState vState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(vState);
        vState.setLabel("s14");
        vState.setSymbol("V");
        Set<CharacterState> vSet = new HashSet<CharacterState>(3);
        vSet.add(aState);
        vSet.add(cState);
        vSet.add(gState);
        vState.setStates(vSet);
        UncertainCharacterState nState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(nState);
        tState.setLabel("s15");
        tState.setSymbol("N");
        Set<CharacterState> nSet = new HashSet<CharacterState>(4);
        nSet.add(aState);
        nSet.add(cState);
        nSet.add(gState);
        nSet.add(tState);
        nState.setStates(nSet);
        UncertainCharacterState xState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(xState);
        xState.setLabel("s16");
        xState.setSymbol("X");
        xState.setStates(nSet);   //safe to share these sets?
        UncertainCharacterState gapState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(gapState);
        tState.setLabel("s17");
        tState.setSymbol("-");
        Set <CharacterState> gapSet = new HashSet<CharacterState>();
        gapState.setStates(gapSet);   //want an empty set here, not null
        UncertainCharacterState unKnownState = new UncertainCharacterStateImpl(document);
        DNAStateSet.getCharacterStates().add(unKnownState);
        tState.setLabel("s18");
        tState.setSymbol("?");
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
        unKnownState.setStates(unKnownSet);   //want an empty set here, not null

        return (CharacterStateSet)DNAStateSet;
    }

    public CharacterStateSet getRNAStateSet(){
        if (RNAStateSet != null){
            return (CharacterStateSet)RNAStateSet;
        }
        Document document = getDocument();
        RNAStateSet = new MolecularCharacterStateSetImpl(document);
        RNAStateSet.setCharacterStates(new HashSet<CharacterState>());

        CharacterState tState = null;
        CharacterState uState = null;
        for (CharacterState c : DNAStateSet.getCharacterStates()){
            if ("T".equals(c.getSymbol())){
                tState = c;
                uState = new CharacterStateImpl(getDocument());
                uState.setLabel(c.getLabel());
                uState.setSymbol("U");
            }
            else
                RNAStateSet.getCharacterStates().add(c);
        }
        for (CharacterState c : DNAStateSet.getCharacterStates()){
            if (c instanceof UncertainCharacterState){
                UncertainCharacterState u = (UncertainCharacterState)c;
                if (u.getStates().contains(tState)){
                    u.getStates().remove(tState);
                    u.getStates().add(uState);
                }
            }
        }
        return (CharacterStateSet)RNAStateSet;
    }

    public CharacterStateSet getProteinStateSet(){
        if (ProteinStateSet != null){
            return (CharacterStateSet)ProteinStateSet;
        }
        Document document = getDocument();
        ProteinStateSet = new MolecularCharacterStateSetImpl(document);
        return (CharacterStateSet)ProteinStateSet;
    }

    
    
}
