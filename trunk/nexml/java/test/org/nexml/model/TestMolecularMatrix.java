package org.nexml.model;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;

public class TestMolecularMatrix {

    @Test
    public void testDNAMatrix() {
        Document doc = null;
        try {
            doc = DocumentFactory.createDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        OTUs mammals = doc.createOTUs();
        CategoricalMatrix categoricalMatrix = doc
                .createCategoricalMatrix(mammals);
        mammals.setLabel("mammals");
        OTU chimp = mammals.createOTU();
        chimp.setLabel("chimp");
        categoricalMatrix.setOTUs(mammals);
        Assert.assertEquals("categoricalMatrix.getOTUS should be mammals",
                mammals, categoricalMatrix.getOTUs());

        CharacterStateSet characterMolecularStateSet = categoricalMatrix.getDNACharacterStateSet();

//      Assert.assertEquals("characterStateSet should be in categoricalMatrix",
//              characterMolecularStateSet, categoricalMatrix.getCharacterStateSets()
//                      .iterator().next());


        CharacterState aState = characterMolecularStateSet.lookupCharacterStateBySymbol("A");
        CharacterState cState = characterMolecularStateSet.lookupCharacterStateBySymbol("C");
        CharacterState gState = characterMolecularStateSet.lookupCharacterStateBySymbol("G");
        CharacterState tState = characterMolecularStateSet.lookupCharacterStateBySymbol("T");
        Assert.assertTrue("State containing A is not null", (aState != null));
        Assert.assertTrue("State containing C is not null", (cState != null));
        Assert.assertTrue("State containing G is not null", (gState != null));
        Assert.assertTrue("State containing T is not null", (tState != null));

        Character pos1 = categoricalMatrix.createCharacter(characterMolecularStateSet);
        Character pos2 = categoricalMatrix.createCharacter(characterMolecularStateSet);
        Character pos3 = categoricalMatrix.createCharacter(characterMolecularStateSet);
        Character pos4 = categoricalMatrix.createCharacter(characterMolecularStateSet);

        MatrixCell<CharacterState> cell1 = categoricalMatrix.getCell(chimp,pos1);
        MatrixCell<CharacterState> cell2 = categoricalMatrix.getCell(chimp,pos2);
        MatrixCell<CharacterState> cell3 = categoricalMatrix.getCell(chimp,pos3);
        MatrixCell<CharacterState> cell4 = categoricalMatrix.getCell(chimp,pos4);

        cell1.setValue(aState);
        cell2.setValue(cState);
        cell3.setValue(gState);
        cell4.setValue(tState);
        
        Assert.assertEquals("cell1.getValue should be A",aState, cell1.getValue());

        Assert.assertEquals("cell1.getValue.getSymbol() should be A","A", cell1.getValue().getSymbol());

        Assert.assertEquals("cell2.getValue should be C",cState, cell2.getValue());

        Assert.assertEquals("cell2.getValue.getSymbol() should be C","C", cell2.getValue().getSymbol());

        Assert.assertEquals("cell3.getValue should be G",gState, cell3.getValue());

        Assert.assertEquals("cell3.getValue.getSymbol() should be G","G", cell3.getValue().getSymbol());

        Assert.assertEquals("cell4.getValue should be T",tState, cell4.getValue());

        Assert.assertEquals("cell4.getValue.getSymbol() should be T","T", cell4.getValue().getSymbol());

        
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
        CategoricalMatrix categoricalMatrix = doc
                .createCategoricalMatrix(mammals);
        mammals.setLabel("mammals");
        OTU chimp = mammals.createOTU();
        chimp.setLabel("chimp");
        categoricalMatrix.setOTUs(mammals);
        Assert.assertEquals("categoricalMatrix.getOTUS should be mammals",
                mammals, categoricalMatrix.getOTUs());

        CharacterStateSet characterMolecularStateSet = categoricalMatrix.getRNACharacterStateSet();

//      Assert.assertEquals("characterStateSet should be in categoricalMatrix",
//              characterMolecularStateSet, categoricalMatrix.getCharacterStateSets()
//                      .iterator().next());


        CharacterState aState = characterMolecularStateSet.lookupCharacterStateBySymbol("A");
        CharacterState cState = characterMolecularStateSet.lookupCharacterStateBySymbol("C");
        CharacterState gState = characterMolecularStateSet.lookupCharacterStateBySymbol("G");
        CharacterState uState = characterMolecularStateSet.lookupCharacterStateBySymbol("U");
        Assert.assertTrue("State containing A is not null", (aState != null));
        Assert.assertTrue("State containing C is not null", (cState != null));
        Assert.assertTrue("State containing G is not null", (gState != null));
        Assert.assertTrue("State containing U is not null", (uState != null));

        Character pos1 = categoricalMatrix.createCharacter(characterMolecularStateSet);
        Character pos2 = categoricalMatrix.createCharacter(characterMolecularStateSet);
        Character pos3 = categoricalMatrix.createCharacter(characterMolecularStateSet);
        Character pos4 = categoricalMatrix.createCharacter(characterMolecularStateSet);

        MatrixCell<CharacterState> cell1 = categoricalMatrix.getCell(chimp,pos1);
        MatrixCell<CharacterState> cell2 = categoricalMatrix.getCell(chimp,pos2);
        MatrixCell<CharacterState> cell3 = categoricalMatrix.getCell(chimp,pos3);
        MatrixCell<CharacterState> cell4 = categoricalMatrix.getCell(chimp,pos4);

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
}
