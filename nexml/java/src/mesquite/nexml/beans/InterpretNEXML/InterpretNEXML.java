/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.0, September 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.nexml.beans.InterpretNEXML;
/*~~  */

//$Id: InterpretNEXML.java 472 2008-03-05 20:00:35Z pmidford $


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mesquite.Mesquite;
import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.RNAData;
import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteProject;
import mesquite.lib.ProgressIndicator;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.TaxaManager;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;

import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractCells;
import org.nexml.x10.AbstractChar;
import org.nexml.x10.AbstractFormat;
import org.nexml.x10.AbstractSeqMatrix;
import org.nexml.x10.AbstractSeqRow;
import org.nexml.x10.AbstractSeqs;
import org.nexml.x10.ContinuousCells;
import org.nexml.x10.ContinuousSeq;
import org.nexml.x10.ContinuousSeqs;
import org.nexml.x10.DnaCells;
import org.nexml.x10.DnaSeqs;
import org.nexml.x10.Nexml;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.ProteinCells;
import org.nexml.x10.ProteinSeqs;
import org.nexml.x10.RestrictionCells;
import org.nexml.x10.RestrictionSeqs;
import org.nexml.x10.RnaCells;
import org.nexml.x10.RnaSeqs;
import org.nexml.x10.Taxa;
import org.nexml.x10.impl.StandardCellsImpl;
import org.nexml.x10.impl.StandardSeqsImpl;

/** A file interpreter for a NEXML file format.  */
public class InterpretNEXML extends FileInterpreterI {  

    private HashMap taxaById;  //It would be nice to upgrade these to generics someday
    private HashMap taxonMapByTaxaId;
    private HashMap stateMapById;
    private HashMap stateSetByCharacter;
    /*.................................................................................................................*/

    
    
    /**
     * @param arguments
     * @param condition
     * @param hiredByName
     * @return boolean
     */
    /*.................................................................................................................*/
    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
        taxaById = new HashMap();
        taxonMapByTaxaId = new HashMap();
        stateMapById = new HashMap();
        stateSetByCharacter = new HashMap();
        return true;
    }   
    
    
    /*.................................................................................................................*/
    public boolean canExportEver() {  
        return true;
    }
    /*.................................................................................................................*/
    public boolean canExportProject(MesquiteProject project) {  
        return true;
    }
    /*.................................................................................................................*/
    public boolean canExportData(Class dataClass) {  
        return true;
    }
    /*.................................................................................................................*/
    public boolean canImport() {  
        return true;
    }
    /*.................................................................................................................*/
    public boolean canImport(String arguments){
        return true;
    }
    /*.................................................................................................................*/
    public void readFile(MesquiteProject project, MesquiteFile file, String arguments) {
        incrementMenuResetSuppression();
        ProgressIndicator progIndicator = new ProgressIndicator(project,"Importing File "+ file.getName(), file.existingLength());
        progIndicator.start();
        file.linkProgressIndicator(progIndicator);
        if (file.openReading()) {
            boolean abort = false;
            try {
                processFile(project,file);
            } catch (Exception e) {
                errorReport(e);
            }
            finishImport(progIndicator, file, abort);
        }
        decrementMenuResetSuppression();
    }
    
    private void errorReport(Exception e){
        System.out.println("An exception was thrown: " + e);
        e.printStackTrace();
    }
    
    
    
    /**
     * This uses the document factory to parse the file into an XMLBeans Document
     * and farms out creating the Mesquite Blocks
     * @param project
     * @param file
     */
    public void processFile(MesquiteProject project,MesquiteFile file){
        
        NexmlDocument nDoc = null;
        try {
            nDoc = NexmlDocument.Factory.parse(new File(file.getPath()));
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Nexml n = nDoc.getNexml();
        
        processOTUBlocks(n,file);
        processCharactersBlocks(n,file);
        
    }
    
    
    private void processOTUBlocks(Nexml n,MesquiteFile file){
        Taxa[] o = n.getOtusArray();
        if (o != null && o.length > 0){
            TaxaManager taxaTask = (TaxaManager)findElementManager(mesquite.lib.Taxa.class);
            for(int taxaCount = 0;taxaCount < o.length; taxaCount++){
                Taxa currentBlock = o[taxaCount];
                int taxaInBlock = currentBlock.getOtuArray().length;
                mesquite.lib.Taxa taxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName(currentBlock.getLabel()), taxaInBlock, false);
                if (taxa !=null){
                    taxa.addToFile(file, getProject(), taxaTask);
                    taxaById.put(currentBlock.getId(), taxa);
                    HashMap myTaxonMap = new HashMap();
                    taxonMapByTaxaId.put(currentBlock.getId(), myTaxonMap);
                    for(int taxaCounter = 0;taxaCounter<taxaInBlock;taxaCounter++){
                        mesquite.lib.Taxon t = taxa.getTaxon(taxaCounter);
                        if (t!=null) {
                            t.setName(currentBlock.getOtuArray(taxaCounter).getLabel());
                            myTaxonMap.put(currentBlock.getOtuArray(taxaCounter).getId(), t);
                        }
                    }
                }
            }
        }
    }
    
    
    /* Notes from CharactersFactory.java - part of the SAX parser
     * To parse a nexml characters block, the following (may) need to happen:
     * 
     * - parse out the data type (xsi:type) and translate it to the equivalent
     *   mesquite data type, instantiate the appropriate matrix object and link
     *   it up with the appropriate taxa object
     *   
     * - for each &lt;states&gt; element, create a new slot in the stateMap
     *   hashtable and record the id of that element, the state set
     *   
     * - for each &lt;state&gt; element, create a nested slot in the stateMap
     *   hashtable and populate it with the symbol, keyed on the state ID
     *   
     * - for each &lt;char&gt; element, create a new slot in the stateSetForCharacter
     *   hashtable, and populate it with the appropriate stateMap entry
     * 
     * Nexml character data types, + indicates data types supported by Mesquite:
     * + nex:ContinuousSeqs
     * + nex:ContinuousCells
     * + nex:DnaSeqs
     * + nex:DnaCells
     * + nex:ProteinSeqs
     * + nex:ProteinCells
     * - nex:RestrictionSeqs
     * - nex:RestrictionCells
     * + nex:RnaSeqs
     * + nex:RnaCells
     * + nex:StandardSeqs
     * + nex:StandardCells
     * 
     */
    private void processCharactersBlocks(Nexml n, MesquiteFile file){
        AbstractBlock[] c = n.getCharactersArray();
        if (c != null && c.length > 0){
            CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);

            for(int charBlockCount = 0; charBlockCount< c.length;charBlockCount++){
                AbstractBlock currentBlock = c[charBlockCount];
                String linkedTaxaId = currentBlock.getOtus();
                if (taxaById.get(linkedTaxaId) == null){                
                    MesquiteMessage.warnProgrammer("Character block " + currentBlock.getId() + " links to taxa block with label " + linkedTaxaId + " which could not be found");
                    return;
                }
                mesquite.lib.characters.CharacterData data = null;
                if (currentBlock instanceof AbstractSeqs){
                    if (currentBlock instanceof ContinuousSeqs){
                        data = addContinuousSeqCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof DnaSeqs){
                        data = addDNASeqCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof ProteinSeqs){
                        data = addProteinSeqCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof RestrictionSeqs){
                        MesquiteMessage.warnUser("Restriction data not recognized by mesquite in block " + currentBlock.getId());                       
                    }
                    else if (currentBlock instanceof RnaSeqs){
                        data = addRNASeqCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof StandardSeqsImpl){
                        data = addStandardSeqCharBlock(charTask,linkedTaxaId,currentBlock);
                    } 
                    else 
                        MesquiteMessage.warnProgrammer("Unrecognized character matrix type " + currentBlock.getClass() + " in block " + currentBlock.getId());
                }
                else if (currentBlock instanceof AbstractCells){
                    if (currentBlock instanceof ContinuousCells){
                        data = addContinuousCellCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof DnaCells){
                        data = addDNACellCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof ProteinCells){
                        data = addProteinCellCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof RestrictionCells){
                        MesquiteMessage.warnUser("Restriction data not recognized by mesquite in block " + currentBlock.getId());                       
                    }
                    else if (currentBlock instanceof RnaCells){
                        data = addRNASeqCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else if (currentBlock instanceof StandardCellsImpl){
                        data = addStandardCellCharBlock(charTask,linkedTaxaId,currentBlock);
                    }
                    else
                        MesquiteMessage.warnProgrammer("Unrecognized character matrix type " + currentBlock.getClass() + " in block " + currentBlock.getId());
                }
                else
                    MesquiteMessage.warnProgrammer("Unrecognized character matrix type " + currentBlock.getClass() + " in block " + currentBlock.getId());
                if (data != null){
                    data.setName(currentBlock.getLabel());
                    data.addToFile(file, getProject(), null);
                }

            }
        }
    }


    

    private mesquite.lib.characters.CharacterData addStandardCellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        AbstractFormat f = null;
        AbstractChar chars [] = null;
        if (currentBlock.isSetFormat()){
            f = currentBlock.getFormat();
            chars = f.getCharArray();
        }
        if (chars != null){
            for(int i= 0;i<chars.length;i++){
            }
        }
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, CategoricalData.DATATYPENAME);  // Make this type sensitive
        return data;
    }

    private mesquite.lib.characters.CharacterData addContinuousCellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, ContinuousData.DATATYPENAME);  // Make this type sensitive
        return data;
    }

    private mesquite.lib.characters.CharacterData addDNASeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  // Make this type sensitive
        return data;
    }

    private mesquite.lib.characters.CharacterData addDNACellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  // Make this type sensitive
        return data;
    }


    private mesquite.lib.characters.CharacterData addProteinSeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  // Make this type sensitive
        return data;
    }

    private mesquite.lib.characters.CharacterData addProteinCellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  // Make this type sensitive
        return data;
    }


    private mesquite.lib.characters.CharacterData addRNASeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  //TODO so how to make this RNA specific?
        return data;
    }

    private mesquite.lib.characters.CharacterData addRNACellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  //TODO so how to make this RNA specific?
        return data;
    }

    
    private mesquite.lib.characters.CharacterData addContinuousSeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, ContinuousData.DATATYPENAME);  // Make this type sensitive
        AbstractSeqMatrix matrix = ((AbstractSeqs)currentBlock).getMatrix();
        AbstractSeqRow [] rows = matrix.getRowArray();
        for (int i= 0; i< rows.length;i++){
            AbstractSeqRow curRow = rows[i];
            String curOtu = curRow.getOtu(); 
            HashMap linkedTaxonMap = (HashMap)taxonMapByTaxaId.get(linkedTaxaId);
            mesquite.lib.Taxon t = (mesquite.lib.Taxon)linkedTaxonMap.get(curOtu);
            int it = linkedTaxa.whichTaxonNumber(t);  //TODO how to handle -1 returns?
            XmlAnySimpleType x = curRow.getSeq();
            if (x instanceof ContinuousSeq){
                ContinuousSeq curSeq = (ContinuousSeq)x;
                List seqList = curSeq.getListValue();
                int ic = 0;
                Iterator elementItr = seqList.iterator();
                while (elementItr.hasNext()){
                    Float element = (Float)elementItr.next();
                    ContinuousState elementState = new ContinuousState(element.doubleValue());
                    elementState.setNumItems(1);
                    if (data.getNumChars() <= ic) {
                        data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
                    }
                    data.setState(ic,it,elementState);
                    ic++;
                }
            }
            else{
                MesquiteMessage.println("Expected a ContinuousSeq, but got " + x);
            }
        }
        return data;
    }

    private mesquite.lib.characters.CharacterData addStandardSeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, CategoricalData.DATATYPENAME);  // Make this type sensitive
        return data;
    }

    

    public void exportFile(MesquiteFile file, String arguments) {
        // TODO Auto-generated method stub
        
    }

    public String getName() {
        // TODO Auto-generated method stub
        return "Nexml Import";
    }
}