package org.nexml.model;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author pmidford
 * created Mar 12, 2009
 *
 */
public class TestMolecularMatrix {

    @Test
    public void testDNAMatrix() {
        Document doc = null;
        try {
            doc =  DocumentFactory.createDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        OTUs mammals = doc.createOTUs();
        MolecularMatrix molecularMatrix = doc
                .createMolecularMatrix(mammals,"DNASeqs");
        mammals.setLabel("mammals");
        OTU chimp = mammals.createOTU();
        chimp.setLabel("chimp");
        molecularMatrix.setOTUs(mammals);
        Assert.assertEquals("categoricalMatrix.getOTUS should be mammals",
                mammals, molecularMatrix.getOTUs());

        CharacterStateSet characterMolecularStateSet = molecularMatrix.getDNACharacterStateSet();


        CharacterState aState = characterMolecularStateSet.lookupCharacterStateBySymbol("A");
        CharacterState cState = characterMolecularStateSet.lookupCharacterStateBySymbol("C");
        CharacterState gState = characterMolecularStateSet.lookupCharacterStateBySymbol("G");
        CharacterState tState = characterMolecularStateSet.lookupCharacterStateBySymbol("T");
        CharacterState kState = characterMolecularStateSet.lookupCharacterStateBySymbol("K");
        CharacterState mState = characterMolecularStateSet.lookupCharacterStateBySymbol("M");
        CharacterState rState = characterMolecularStateSet.lookupCharacterStateBySymbol("R");
        CharacterState sState = characterMolecularStateSet.lookupCharacterStateBySymbol("S");
        CharacterState wState = characterMolecularStateSet.lookupCharacterStateBySymbol("W");
        CharacterState yState = characterMolecularStateSet.lookupCharacterStateBySymbol("Y");
        CharacterState bState = characterMolecularStateSet.lookupCharacterStateBySymbol("B");
        CharacterState dState = characterMolecularStateSet.lookupCharacterStateBySymbol("D");
        CharacterState hState = characterMolecularStateSet.lookupCharacterStateBySymbol("H");
        CharacterState vState = characterMolecularStateSet.lookupCharacterStateBySymbol("V");
        CharacterState gapState = characterMolecularStateSet.lookupCharacterStateBySymbol("-");
        CharacterState missingState = characterMolecularStateSet.lookupCharacterStateBySymbol("?");
        Assert.assertTrue("State containing A is not null", (aState != null));
        Assert.assertTrue("State containing C is not null", (cState != null));
        Assert.assertTrue("State containing G is not null", (gState != null));
        Assert.assertTrue("State containing T is not null", (tState != null));
        Assert.assertTrue("State containing K is not null", (kState != null));
        Assert.assertTrue("State containing M is not null", (mState != null));
        Assert.assertTrue("State containing R is not null", (rState != null));
        Assert.assertTrue("State containing S is not null", (sState != null));
        Assert.assertTrue("State containing W is not null", (wState != null));
        Assert.assertTrue("State containing Y is not null", (yState != null));
        Assert.assertTrue("State containing B is not null", (bState != null));
        Assert.assertTrue("State containing D is not null", (dState != null));
        Assert.assertTrue("State containing H is not null", (hState != null));
        Assert.assertTrue("State containing V is not null", (vState != null));
        Assert.assertTrue("State containing - is not null", (gapState != null));
        Assert.assertTrue("State containing ? is not null", (missingState != null));

        Character pos1 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos2 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos3 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos4 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos5 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos6 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos7 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos8 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos9 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos10 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos11= molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos12= molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos13 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos14 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos15 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos16 = molecularMatrix.createCharacter(characterMolecularStateSet);

        MatrixCell<CharacterState> cell1 = molecularMatrix.getCell(chimp,pos1);
        MatrixCell<CharacterState> cell2 = molecularMatrix.getCell(chimp,pos2);
        MatrixCell<CharacterState> cell3 = molecularMatrix.getCell(chimp,pos3);
        MatrixCell<CharacterState> cell4 = molecularMatrix.getCell(chimp,pos4);
        MatrixCell<CharacterState> cell5 = molecularMatrix.getCell(chimp,pos5);
        MatrixCell<CharacterState> cell6 = molecularMatrix.getCell(chimp,pos6);
        MatrixCell<CharacterState> cell7 = molecularMatrix.getCell(chimp,pos7);
        MatrixCell<CharacterState> cell8 = molecularMatrix.getCell(chimp,pos8);
        MatrixCell<CharacterState> cell9 = molecularMatrix.getCell(chimp,pos9);
        MatrixCell<CharacterState> cell10 = molecularMatrix.getCell(chimp,pos10);
        MatrixCell<CharacterState> cell11 = molecularMatrix.getCell(chimp,pos11);
        MatrixCell<CharacterState> cell12 = molecularMatrix.getCell(chimp,pos12);
        MatrixCell<CharacterState> cell13 = molecularMatrix.getCell(chimp,pos13);
        MatrixCell<CharacterState> cell14 = molecularMatrix.getCell(chimp,pos14);
        MatrixCell<CharacterState> cell15 = molecularMatrix.getCell(chimp,pos15);
        MatrixCell<CharacterState> cell16 = molecularMatrix.getCell(chimp,pos16);

        cell1.setValue(aState);
        cell2.setValue(cState);
        cell3.setValue(gState);
        cell4.setValue(tState);
        cell5.setValue(kState);
        cell6.setValue(mState);
        cell7.setValue(rState);
        cell8.setValue(sState);
        cell9.setValue(wState);
        cell10.setValue(yState);
        cell11.setValue(bState);
        cell12.setValue(dState);
        cell13.setValue(hState);
        cell14.setValue(vState);
        cell15.setValue(gapState);
        cell16.setValue(missingState);
        
        Assert.assertEquals("cell1.getValue should be A",aState, cell1.getValue());
        Assert.assertEquals("cell1.getValue.getSymbol() should be A","A", cell1.getValue().getSymbol());

        Assert.assertEquals("cell2.getValue should be C",cState, cell2.getValue());
        Assert.assertEquals("cell2.getValue.getSymbol() should be C","C", cell2.getValue().getSymbol());

        Assert.assertEquals("cell3.getValue should be G",gState, cell3.getValue());
        Assert.assertEquals("cell3.getValue.getSymbol() should be G","G", cell3.getValue().getSymbol());

        Assert.assertEquals("cell4.getValue should be T",tState, cell4.getValue());
        Assert.assertEquals("cell4.getValue.getSymbol() should be T","T", cell4.getValue().getSymbol());

        Assert.assertEquals("cell5.getValue should be K",kState, cell5.getValue());
        Assert.assertEquals("cell5.getValue.getSymbol() should be K","K", cell5.getValue().getSymbol());

        Assert.assertEquals("cell6.getValue should be M",mState, cell6.getValue());
        Assert.assertEquals("cell6.getValue.getSymbol() should be M","M", cell6.getValue().getSymbol());

        Assert.assertEquals("cell7.getValue should be R",rState, cell7.getValue());
        Assert.assertEquals("cell7.getValue.getSymbol() should be R","R", cell7.getValue().getSymbol());

        Assert.assertEquals("cell8.getValue should be S",sState, cell8.getValue());
        Assert.assertEquals("cell8.getValue.getSymbol() should be S","S", cell8.getValue().getSymbol());

        Assert.assertEquals("cell9.getValue should be W",wState, cell9.getValue());
        Assert.assertEquals("cell9.getValue.getSymbol() should be W","W", cell9.getValue().getSymbol());

        Assert.assertEquals("cell10.getValue should be Y",yState, cell10.getValue());
        Assert.assertEquals("cell10.getValue.getSymbol() should be Y","Y", cell10.getValue().getSymbol());

        Assert.assertEquals("cell11.getValue should be B",bState, cell11.getValue());
        Assert.assertEquals("cell11.getValue.getSymbol() should be B","B", cell11.getValue().getSymbol());

        Assert.assertEquals("cell12.getValue should be D",dState, cell12.getValue());
        Assert.assertEquals("cell12.getValue.getSymbol() should be D","D", cell12.getValue().getSymbol());

        Assert.assertEquals("cell13.getValue should be H",hState, cell13.getValue());
        Assert.assertEquals("cell13.getValue.getSymbol() should be H","H", cell13.getValue().getSymbol());

        Assert.assertEquals("cell14.getValue should be V",vState, cell14.getValue());
        Assert.assertEquals("cell14.getValue.getSymbol() should be V","V", cell14.getValue().getSymbol());

        Assert.assertEquals("cell15.getValue should be gap",gapState, cell15.getValue());
        Assert.assertEquals("cell15.getValue.getSymbol() should be -","-", cell15.getValue().getSymbol());

        Assert.assertEquals("cell16.getValue should be missing",missingState, cell16.getValue());
        Assert.assertEquals("cell16.getValue.getSymbol() should be ?","?", cell16.getValue().getSymbol());

        
    }
    @Test
    public void testRNAMatrix() {
        Document doc = null;
        try {
            doc = DocumentFactory.createDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        OTUs mammals = doc.createOTUs();
        MolecularMatrix molecularMatrix = doc
                .createMolecularMatrix(mammals,"RNASeqs");
        mammals.setLabel("mammals");
        OTU chimp = mammals.createOTU();
        chimp.setLabel("chimp");
        molecularMatrix.setOTUs(mammals);
        Assert.assertEquals("MolecularMatrix.getOTUS should be mammals",
                mammals, molecularMatrix.getOTUs());

        CharacterStateSet characterMolecularStateSet = molecularMatrix.getRNACharacterStateSet();

//      Assert.assertEquals("characterStateSet should be in MolecularMatrix",
//              characterMolecularStateSet, MolecularMatrix.getCharacterStateSets()
//                      .iterator().next());


        CharacterState aState = characterMolecularStateSet.lookupCharacterStateBySymbol("A");
        CharacterState cState = characterMolecularStateSet.lookupCharacterStateBySymbol("C");
        CharacterState gState = characterMolecularStateSet.lookupCharacterStateBySymbol("G");
        CharacterState uState = characterMolecularStateSet.lookupCharacterStateBySymbol("U");
        Assert.assertTrue("State containing A is not null", (aState != null));
        Assert.assertTrue("State containing C is not null", (cState != null));
        Assert.assertTrue("State containing G is not null", (gState != null));
        Assert.assertTrue("State containing U is not null", (uState != null));

        Character pos1 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos2 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos3 = molecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos4 = molecularMatrix.createCharacter(characterMolecularStateSet);

        MatrixCell<CharacterState> cell1 = molecularMatrix.getCell(chimp,pos1);
        MatrixCell<CharacterState> cell2 = molecularMatrix.getCell(chimp,pos2);
        MatrixCell<CharacterState> cell3 = molecularMatrix.getCell(chimp,pos3);
        MatrixCell<CharacterState> cell4 = molecularMatrix.getCell(chimp,pos4);

        cell1.setValue(aState);
        cell2.setValue(cState);
        cell3.setValue(gState);
        cell4.setValue(uState);
        
        Assert.assertEquals("cell1.getValue should be A",aState, cell1.getValue());

        Assert.assertEquals("cell1.getValue.getSymbol() should be A","A", cell1.getValue().getSymbol());

        Assert.assertEquals("cell2.getValue should be C",cState, cell2.getValue());

        Assert.assertEquals("cell2.getValue.getSymbol() should be C","C", cell2.getValue().getSymbol());

        Assert.assertEquals("cell3.getValue should be G",gState, cell3.getValue());

        Assert.assertEquals("cell3.getValue.getSymbol() should be G","G", cell3.getValue().getSymbol());

        Assert.assertEquals("cell4.getValue should be U",uState, cell4.getValue());

        Assert.assertEquals("cell4.getValue.getSymbol() should be U","U", cell4.getValue().getSymbol());

        
    }
    
    @Test
    public void testProteinMatrix() {
        Document doc = null;
        try {
            doc = DocumentFactory.createDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        OTUs mammals = doc.createOTUs();
        MolecularMatrix MolecularMatrix = doc
                .createMolecularMatrix(mammals,"ProteinSeqs");
        mammals.setLabel("mammals");
        OTU chimp = mammals.createOTU();
        chimp.setLabel("chimp");
        MolecularMatrix.setOTUs(mammals);
        Assert.assertEquals("MolecularMatrix.getOTUS should be mammals",
                mammals, MolecularMatrix.getOTUs());

        CharacterStateSet characterMolecularStateSet = MolecularMatrix.getProteinCharacterStateSet();

//      Assert.assertEquals("characterStateSet should be in MolecularMatrix",
//              characterMolecularStateSet, MolecularMatrix.getCharacterStateSets()
//                      .iterator().next());


        CharacterState aState = characterMolecularStateSet.lookupCharacterStateBySymbol("A");
        CharacterState cState = characterMolecularStateSet.lookupCharacterStateBySymbol("C");
        CharacterState dState = characterMolecularStateSet.lookupCharacterStateBySymbol("D");
        CharacterState eState = characterMolecularStateSet.lookupCharacterStateBySymbol("E");
        CharacterState fState = characterMolecularStateSet.lookupCharacterStateBySymbol("F");
        CharacterState gState = characterMolecularStateSet.lookupCharacterStateBySymbol("G");
        CharacterState hState = characterMolecularStateSet.lookupCharacterStateBySymbol("H");
        CharacterState iState = characterMolecularStateSet.lookupCharacterStateBySymbol("I");
        CharacterState kState = characterMolecularStateSet.lookupCharacterStateBySymbol("K");
        CharacterState lState = characterMolecularStateSet.lookupCharacterStateBySymbol("L");
        CharacterState mState = characterMolecularStateSet.lookupCharacterStateBySymbol("M");
        CharacterState nState = characterMolecularStateSet.lookupCharacterStateBySymbol("N");
        CharacterState pState = characterMolecularStateSet.lookupCharacterStateBySymbol("P");
        CharacterState qState = characterMolecularStateSet.lookupCharacterStateBySymbol("Q");
        CharacterState rState = characterMolecularStateSet.lookupCharacterStateBySymbol("R");
        CharacterState sState = characterMolecularStateSet.lookupCharacterStateBySymbol("S");
        CharacterState tState = characterMolecularStateSet.lookupCharacterStateBySymbol("T");
        CharacterState vState = characterMolecularStateSet.lookupCharacterStateBySymbol("V");
        CharacterState wState = characterMolecularStateSet.lookupCharacterStateBySymbol("W");
        CharacterState yState = characterMolecularStateSet.lookupCharacterStateBySymbol("Y");
        Assert.assertTrue("State containing A is not null", (aState != null));
        Assert.assertTrue("State containing C is not null", (cState != null));
        Assert.assertTrue("State containing D is not null", (dState != null));
        Assert.assertTrue("State containing E is not null", (eState != null));
        Assert.assertTrue("State containing F is not null", (fState != null));
        Assert.assertTrue("State containing G is not null", (gState != null));
        Assert.assertTrue("State containing H is not null", (hState != null));
        Assert.assertTrue("State containing I is not null", (iState != null));
        Assert.assertTrue("State containing K is not null", (kState != null));
        Assert.assertTrue("State containing L is not null", (lState != null));
        Assert.assertTrue("State containing M is not null", (mState != null));
        Assert.assertTrue("State containing N is not null", (nState != null));
        Assert.assertTrue("State containing P is not null", (pState != null));
        Assert.assertTrue("State containing Q is not null", (qState != null));
        Assert.assertTrue("State containing R is not null", (rState != null));
        Assert.assertTrue("State containing S is not null", (sState != null));
        Assert.assertTrue("State containing T is not null", (tState != null));
        Assert.assertTrue("State containing V is not null", (vState != null));
        Assert.assertTrue("State containing W is not null", (wState != null));
        Assert.assertTrue("State containing Y is not null", (yState != null));
        
        
        Character pos1 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos2 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos3 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos4 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos5 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos6 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos7 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos8 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos9 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos10 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos11 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos12 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos13 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos14 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos15 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos16 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos17= MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos18 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos19 = MolecularMatrix.createCharacter(characterMolecularStateSet);
        Character pos20= MolecularMatrix.createCharacter(characterMolecularStateSet);

        MatrixCell<CharacterState> cell1 = MolecularMatrix.getCell(chimp,pos1);
        MatrixCell<CharacterState> cell2 = MolecularMatrix.getCell(chimp,pos2);
        MatrixCell<CharacterState> cell3 = MolecularMatrix.getCell(chimp,pos3);
        MatrixCell<CharacterState> cell4 = MolecularMatrix.getCell(chimp,pos4);
        MatrixCell<CharacterState> cell5 = MolecularMatrix.getCell(chimp,pos5);
        MatrixCell<CharacterState> cell6 = MolecularMatrix.getCell(chimp,pos6);
        MatrixCell<CharacterState> cell7 = MolecularMatrix.getCell(chimp,pos7);
        MatrixCell<CharacterState> cell8 = MolecularMatrix.getCell(chimp,pos8);
        MatrixCell<CharacterState> cell9 = MolecularMatrix.getCell(chimp,pos9);
        MatrixCell<CharacterState> cell10 = MolecularMatrix.getCell(chimp,pos10);
        MatrixCell<CharacterState> cell11 = MolecularMatrix.getCell(chimp,pos11);
        MatrixCell<CharacterState> cell12 = MolecularMatrix.getCell(chimp,pos12);
        MatrixCell<CharacterState> cell13 = MolecularMatrix.getCell(chimp,pos13);
        MatrixCell<CharacterState> cell14 = MolecularMatrix.getCell(chimp,pos14);
        MatrixCell<CharacterState> cell15 = MolecularMatrix.getCell(chimp,pos15);
        MatrixCell<CharacterState> cell16 = MolecularMatrix.getCell(chimp,pos16);
        MatrixCell<CharacterState> cell17 = MolecularMatrix.getCell(chimp,pos17);
        MatrixCell<CharacterState> cell18 = MolecularMatrix.getCell(chimp,pos18);
        MatrixCell<CharacterState> cell19 = MolecularMatrix.getCell(chimp,pos19);
        MatrixCell<CharacterState> cell20 = MolecularMatrix.getCell(chimp,pos20);

        cell1.setValue(aState);
        cell2.setValue(cState);
        cell3.setValue(dState);
        cell4.setValue(eState);
        cell5.setValue(fState);
        cell6.setValue(gState);
        cell7.setValue(hState);
        cell8.setValue(iState);
        cell9.setValue(kState);
        cell10.setValue(lState);
        cell11.setValue(mState);
        cell12.setValue(nState);
        cell13.setValue(pState);
        cell14.setValue(qState);
        cell15.setValue(rState);
        cell16.setValue(sState);
        cell17.setValue(tState);
        cell18.setValue(vState);
        cell19.setValue(wState);
        cell20.setValue(yState);
        

        Assert.assertEquals("cell1.getValue should be A",aState, cell1.getValue());
        Assert.assertEquals("cell1.getValue.getSymbol() should be A","A", cell1.getValue().getSymbol());

        Assert.assertEquals("cell2.getValue should be C",cState, cell2.getValue());
        Assert.assertEquals("cell2.getValue.getSymbol() should be C","C", cell2.getValue().getSymbol());

        Assert.assertEquals("cell3.getValue should be D",dState, cell3.getValue());
        Assert.assertEquals("cell3.getValue.getSymbol() should be D","D", cell3.getValue().getSymbol());

        Assert.assertEquals("cell4.getValue should be E",eState, cell4.getValue());
        Assert.assertEquals("cell4.getValue.getSymbol() should be E","E", cell4.getValue().getSymbol());

        Assert.assertEquals("cell5.getValue should be F",fState, cell5.getValue());
        Assert.assertEquals("cell5.getValue.getSymbol() should be F","F", cell5.getValue().getSymbol());

        Assert.assertEquals("cell6.getValue should be G",gState, cell6.getValue());
        Assert.assertEquals("cell6.getValue.getSymbol() should be G","G", cell6.getValue().getSymbol());

        Assert.assertEquals("cell7.getValue should be H",hState, cell7.getValue());
        Assert.assertEquals("cell7.getValue.getSymbol() should be H","H", cell7.getValue().getSymbol());

        Assert.assertEquals("cell8.getValue should be I",iState, cell8.getValue());
        Assert.assertEquals("cell8.getValue.getSymbol() should be I","I", cell8.getValue().getSymbol());      
        
        Assert.assertEquals("cell9.getValue should be K",kState, cell9.getValue());
        Assert.assertEquals("cell9.getValue.getSymbol() should be K","K", cell9.getValue().getSymbol());

        Assert.assertEquals("cell10.getValue should be L",lState, cell10.getValue());
        Assert.assertEquals("cell10.getValue.getSymbol() should be L","L", cell10.getValue().getSymbol());

        Assert.assertEquals("cell11.getValue should be M",mState, cell11.getValue());
        Assert.assertEquals("cell11.getValue.getSymbol() should be M","M", cell11.getValue().getSymbol());

        Assert.assertEquals("cell12.getValue should be N",nState, cell12.getValue());
        Assert.assertEquals("cell12.getValue.getSymbol() should be N","N", cell12.getValue().getSymbol());

        Assert.assertEquals("cell13.getValue should be P",pState, cell13.getValue());
        Assert.assertEquals("cell13.getValue.getSymbol() should be P","P", cell13.getValue().getSymbol());

        Assert.assertEquals("cell14.getValue should be Q",qState, cell14.getValue());
        Assert.assertEquals("cell14.getValue.getSymbol() should be Q","Q", cell14.getValue().getSymbol());

        Assert.assertEquals("cell15.getValue should be R",rState, cell15.getValue());
        Assert.assertEquals("cell15.getValue.getSymbol() should be R","R", cell15.getValue().getSymbol());

        Assert.assertEquals("cell16.getValue should be S",sState, cell16.getValue());
        Assert.assertEquals("cell16.getValue.getSymbol() should be S","S", cell16.getValue().getSymbol());

        Assert.assertEquals("cell17.getValue should be T",tState, cell17.getValue());
        Assert.assertEquals("cell17.getValue.getSymbol() should be T","T", cell17.getValue().getSymbol());

        Assert.assertEquals("cell18.getValue should be V",vState, cell18.getValue());
        Assert.assertEquals("cell18.getValue.getSymbol() should be V","V", cell18.getValue().getSymbol());

        Assert.assertEquals("cell19.getValue should be W",wState, cell19.getValue());
        Assert.assertEquals("cell19.getValue.getSymbol() should be W","W", cell19.getValue().getSymbol());

        Assert.assertEquals("cell20.getValue should be Y",yState, cell20.getValue());
        Assert.assertEquals("cell20.getValue.getSymbol() should be Y","Y", cell20.getValue().getSymbol());


        
    }

}
