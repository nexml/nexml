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

/*
 * Assuming you have a workspace in which you've checked out mesquite as 
 * "workspace/Mesquite Project" and you have a all of nexml (i.e. trunk +
 * branches) next to it in the workspace in "workspace/nexml", and you have
 * eclipse set up to build the nexml stuff in nexml/bin, here's what you have
 * to add to the classpaths.xml file in "workspace/Mesquite Project/Mesquite_folder"
 *      <classpath>../../nexml/trunk/nexml/java/jars/jsr173_1.0_api.jar</classpath>
        <classpath>../../nexml/trunk/nexml/java/jars/nexmlbeans.jar</classpath>
        <classpath>../../nexml/trunk/nexml/java/jars/resolver.jar</classpath>
        <classpath>../../nexml/trunk/nexml/java/jars/xbean_xpath.jar</classpath>
        <classpath>../../nexml/trunk/nexml/java/jars/xbean.jar</classpath>
        <classpath>../../nexml/trunk/nexml/java/jars/xmlpublic.jar</classpath>
        <classpath>../../nexml/trunk/nexml/java/jars/xmlbeans-qname.jar</classpath>
        <classpath>../../nexml/bin</classpath>
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import mesquite.Mesquite;
import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.RNAData;
import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;
import mesquite.lib.CommandRecord;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTree;
import mesquite.lib.ProgressIndicator;
import mesquite.lib.TreeVector;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.TreesManager;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.TaxaManager;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;

import org.nexml.x10.*;
import org.nexml.x10.impl.StandardCellsImpl;
import org.nexml.x10.impl.StandardSeqsImpl;
import org.nexml.x10.DnaCells;

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
    public boolean canExportData(java.lang.Class dataClass) {  
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
            errorReport(e);
        } catch (IOException e) {
        	errorReport(e);
        }
        
        Nexml n = nDoc.getNexml();
        
        processOTUBlocks(n,file);
        processCharactersBlocks(n,file);
        processTreesBlocks(n,file);
        
    }
    
    private void processTreesBlocks(Nexml nexml, MesquiteFile file) {
    	Trees[] treeBlocks = nexml.getTreesArray();
    	if ( treeBlocks != null && treeBlocks.length > 0 ) {
    		TreesManager treeTask = (TreesManager)findElementManager(TreeVector.class); 
    		int numTaxas = getProject().getNumberTaxas(); 
    		for ( int i = 0; i < treeBlocks.length; i++ ) {
    			Trees currentBlock = treeBlocks[i];
    			String otusIdRef = currentBlock.getOtus();
    			mesquite.lib.Taxa referencedTaxa = new mesquite.lib.Taxa(0);
    			for ( int j = 0; j > numTaxas; j++ ) {
    				mesquite.lib.Taxa taxa = getProject().getTaxa(j);
    				if ( taxa.getAssignedID().equals(otusIdRef) ) {
    					referencedTaxa = taxa;
    					break;
    				}    				
    			}
    			AbstractTree[] trees = currentBlock.getTreeArray();
    			TreeVector treevector = treeTask.makeNewTreeBlock(referencedTaxa, currentBlock.getLabel(), file);
    			if ( trees != null && trees.length > 0 ) {
    				for ( int j = 0; j < trees.length; j++ ) {
    					if ( trees[j] instanceof FloatTree || trees[j] instanceof IntTree ) {
    						MesquiteTree tree = new MesquiteTree(referencedTaxa);
    						tree.setName(trees[j].getId());    					
    						treevector.addElement(tree, false);
    						TreeNode root = (TreeNode)trees[j].getNodeArray(0);
    						tree.setRooted(root.getRoot(), false);
    						buildTree(tree,root,-1,(FloatTree)trees[j]);
    					}
    				}    				
    			}
    		}
    	}
    }    
    
    private static void buildTree (MesquiteTree mesquiteTree, TreeNode currentXmlNode, int parentNode, FloatTree xmlTree) {
    	int currentNode = mesquiteTree.sproutDaughter(parentNode, false);
    	String otuIdRef = currentXmlNode.getOtu();
    	String nodeId = currentXmlNode.getId();
    	if ( otuIdRef != null ) {
    		mesquite.lib.Taxa taxa = mesquiteTree.getTaxa();
    		for ( int i = 0; i < taxa.getNumTaxa(); i++ ) {
    			if ( otuIdRef.equals(taxa.getTaxon(i).getUniqueID()) ) {
    				mesquiteTree.setTaxonNumber(currentNode, taxa.getTaxon(i).getNumber(), false);
    				break;
    			}
    		}
    	}
    	mesquiteTree.setNodeLabel(currentXmlNode.getLabel(), currentNode);
    	TreeNode[] xmlnodes = (TreeNode[])xmlTree.getNodeArray();
    	TreeFloatEdge[] xmledges = (TreeFloatEdge[])xmlTree.getEdgeArray();
    	Vector<String> children = new Vector();
    	for ( int i = 0; i < xmledges.length; i++ ) {
    		if ( xmledges[i].getTarget().equals(nodeId) ) {
    			mesquiteTree.setBranchLength(currentNode, Double.parseDouble(xmledges[i].getLength().getStringValue()), false);
    		}
    		if ( xmledges[i].getSource().equals(nodeId) ) {
    			children.add(xmledges[i].getTarget());
    		}
    	}
    	for ( int i = 0; i < xmlnodes.length; i++ ) {
    		for ( int j = 0; j < children.size(); j++ ) {
    			if ( children.get(j).equals(xmlnodes[i].getId()) ) {
    				buildTree(mesquiteTree, xmlnodes[i], currentNode, xmlTree);
    			}
    		}
    	}
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
        AbstractStates states [] = null;
        AbstractChar chars [] = null;
        
        if (currentBlock.isSetFormat()){
            f = currentBlock.getFormat();
            states = f.getStatesArray();
            chars = f.getCharArray();
        }
        if (states != null){
            for(int i=0;i<states.length;i++){
                if (states[i] instanceof StandardStates){
                    StandardStates theseStates = (StandardStates)states[i];
                    String statesID = theseStates.getId();
                    HashMap stateSymbolbyID = new HashMap();
                    AbstractState stateSet [] = theseStates.getStateArray();
                    for(int j=0;j<stateSet.length;j++){
                        if (stateSet[j] instanceof StandardState){
                            StandardState aState = (StandardState)stateSet[j];
                            //AbstractMapping[] mapA = aState.getMappingArray();
                            String stateID = aState.getId();
                            String stateSymbol = aState.getSymbol().getStringValue();
                            stateSymbolbyID.put(stateID, stateSymbol);
                        }
                        else
                            MesquiteMessage.warnProgrammer("Expected each state to be a StandardState, but got " + stateSet[j]);
                    }
                }
                else 
                    MesquiteMessage.warnProgrammer("Expected states to be StandardStates, but got " + states[i]);
                
            }
        }
        if (chars != null){
            for(int i= 0;i<chars.length;i++){
                if (chars[i] instanceof StandardChar){
                    StandardChar thisChar = (StandardChar)chars[i];
                    String thisStateSet = thisChar.getStates();
                }
            
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
        HashMap linkedTaxonMap = (HashMap)taxonMapByTaxaId.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  // Make this type sensitive
        AbstractSeqMatrix matrix = ((AbstractSeqs)currentBlock).getMatrix();
        AbstractSeqRow [] rows = matrix.getRowArray();
        for (int i=0;i<rows.length;i++){
            AbstractSeqRow curRow = rows[i];
            String curOtu = curRow.getOtu();
            mesquite.lib.Taxon t = (mesquite.lib.Taxon)linkedTaxonMap.get(curOtu);
            int it = linkedTaxa.whichTaxonNumber(t);  //TODO how to handle -1 returns?
            XmlAnySimpleType x = curRow.getSeq();
            if (x instanceof DNASeq){
                DNASeq curSeq = (DNASeq)x;
                String curString = curSeq.getStringValue();
                int ic = 0;
                for(int pos=0;pos<curString.length();pos++){
                    char curChar = curString.charAt(pos);
                    if (!Character.isSpaceChar(curChar)){
                        if (data.getNumChars() <= ic)
                            data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
                        ((CategoricalData)data).setState(ic++, it, curChar);
                    }
                }
            }
        }
        return data;
    }

    private mesquite.lib.characters.CharacterData addDNACellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        HashMap linkedTaxonMap = (HashMap)taxonMapByTaxaId.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  //TODO so how to make this RNA specific?
        return data;
    }


    private mesquite.lib.characters.CharacterData addProteinSeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        HashMap linkedTaxonMap = (HashMap)taxonMapByTaxaId.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, ProteinData.DATATYPENAME);  // Make this type sensitive
        AbstractSeqMatrix matrix = ((AbstractSeqs)currentBlock).getMatrix();
        if (matrix instanceof AASeqMatrix){
            AASeqMatrix curAASeqs = (AASeqMatrix)matrix;
            AbstractSeqRow[] aarows = curAASeqs.getRowArray();
            for(int k=0;k<aarows.length;k++){
                if (aarows[k] instanceof AAMatrixSeqRow){
                    AAMatrixSeqRow curRow = (AAMatrixSeqRow)aarows[k];
                    String curOtu = curRow.getOtu();
                    mesquite.lib.Taxon t = (mesquite.lib.Taxon)linkedTaxonMap.get(curOtu);
                    int it = linkedTaxa.whichTaxonNumber(t);  //TODO how to handle -1 returns?
                    XmlAnySimpleType q = curRow.getSeq();
                    if (q instanceof AASeq){
                        AASeq qAA = (AASeq)q;
                        String curString = qAA.getStringValue();
                        int ic = 0;
                        for(int pos=0;pos<curString.length();pos++){
                            char curChar = curString.charAt(pos);
                            if (Character.isLetter(curChar)){
                                if (data.getNumChars() <= ic)
                                    data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
                                ((CategoricalData)data).setState(ic++, it, curChar);
                            }
                        }
                    }

                }
            }
        }
        return data;
    }

    private mesquite.lib.characters.CharacterData addProteinCellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, ProteinData.DATATYPENAME);  // Make this type sensitive
        return data;
    }


    private mesquite.lib.characters.CharacterData addRNASeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  //TODO so how to make this RNA specific?
        return data;
    }

    private mesquite.lib.characters.CharacterData addRNACellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        HashMap linkedTaxonMap = (HashMap)taxonMapByTaxaId.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, DNAData.DATATYPENAME);  //TODO so how to make this RNA specific?
        return data;
    }

    
    private mesquite.lib.characters.CharacterData addContinuousSeqCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock){
        mesquite.lib.Taxa linkedTaxa = (mesquite.lib.Taxa)taxaById.get(linkedTaxaId);
        HashMap linkedTaxonMap = (HashMap)taxonMapByTaxaId.get(linkedTaxaId);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, ContinuousData.DATATYPENAME);  // Make this type sensitive
        AbstractSeqMatrix matrix = ((AbstractSeqs)currentBlock).getMatrix();
        AbstractSeqRow [] rows = matrix.getRowArray();
        for (int i= 0; i< rows.length;i++){
            AbstractSeqRow curRow = rows[i];
            String curOtu = curRow.getOtu(); 
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
                MesquiteMessage.warnProgrammer("Expected a ContinuousSeq, but got " + x);
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
    	MesquiteProject project = file.getProject();
    	NexmlDocument doc = NexmlDocument.Factory.newInstance();    
    	Nexml nexml = doc.addNewNexml();
    	nexml.setVersion(new java.math.BigDecimal(1.0));
    	nexml.setGenerator("mesquite");
    	addOtusElements(project, nexml);
    	addCharactersElements(project, nexml);
    	addTreesElements(project, nexml);
    	
    }
    
    private static void addTreesElements (MesquiteProject project, Nexml nexml) {
    	for ( int i = 0; i < project.getNumberTaxas(); i++ ) {
    		mesquite.lib.Taxa taxa = project.getTaxa(i);
    		Listable[] treeVectors = project.getCompatibleFileElements(TreeVector.class, taxa);    		
    		for ( int j = 0; j < treeVectors.length; j++ ) {
    			TreeVector treeVector = (TreeVector)treeVectors[j];    
    			addTreesElement(nexml, taxa, treeVector);
    		}
    	}    	
    }
    
    private static void addTreesElement(Nexml nexml, mesquite.lib.Taxa taxa, TreeVector treeVector) {
    	Trees trees = nexml.addNewTrees();
    	addCommonAttributes(trees, treeVector.getName(), treeVector.getAssignedID());
    	trees.setOtus(taxa.getAssignedID());
    	for ( int i = 0; i < treeVector.getNumberOfTrees(); i++ ) {
    		mesquite.lib.Tree tree = treeVector.getTree(i);
    		FloatTree xmltree = (FloatTree)trees.addNewTree();
    		addCommonAttributes(xmltree, tree.getName(), "" + tree.getID());
    		addTreeElement(xmltree, tree, tree.getRoot());
    	}
    }
    
    private static void addTreeElement(FloatTree xmltree, mesquite.lib.Tree tree, int N) {
		TreeNode node = (TreeNode)xmltree.addNewNode();
		addCommonAttributes(node, tree.getNodeLabel(N), "node" + N);   
		int mother = tree.motherOfNode(N);
		int taxon  = tree.taxonNumberOfNode(N);
		if ( mother != 0 ) {
			TreeFloatEdge edge = (TreeFloatEdge)xmltree.addNewEdge();
			edge.setSource("node" + mother);
			edge.setTarget("node" + N);
			XmlAnySimpleType length = XmlAnySimpleType.Factory.newInstance();
			length.setStringValue("" + tree.getBranchLength(N));
			edge.setLength(length);
			edge.setId("e" + N);
		}	
		else { // node is root
			node.setRoot(tree.rootIsReal());
		}
		if ( taxon != -1 ) {
			node.setOtu(tree.getTaxa().getTaxon(taxon).getUniqueID());
		}
    	for ( int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d) ) {
    		addTreeElement(xmltree, tree, d);
    	}
    }
    
    private static void addCommonAttributes(IDTagged obj, String label, String id) {
    	obj.setLabel(label);
    	obj.setId(id);
    }
    
    private static void addOtusElements (MesquiteProject project, Nexml nexml) {
    	for ( int i = 0; i < project.getNumberTaxas(); i++ ) {
    		mesquite.lib.Taxa taxa = project.getTaxa(i);
    		Taxa xmltaxa = nexml.addNewOtus();
    		addCommonAttributes(xmltaxa, taxa.getName(), taxa.getAssignedID());
    		for ( int j = 0; j < taxa.getNumTaxa(); j++ ) {
    			mesquite.lib.Taxon taxon = taxa.getTaxon(j);
    			Taxon xmltaxon = xmltaxa.addNewOtu();
    			addCommonAttributes(xmltaxon, taxon.getName(), taxon.getUniqueID());
    		} 
    	}    	
    }
    
    private static void addCharactersElements (MesquiteProject project, Nexml nexml) {
    	for ( int i = 0; i < project.getNumberCharMatrices(); i++ ) {
    		mesquite.lib.characters.CharacterData data = project.getCharacterMatrix(i);
    		String dataType = data.getDataTypeName();
    		AbstractBlock xmlcharacters = nexml.addNewCharacters();
    		HashMap idForState = new HashMap();
    		addCommonAttributes(xmlcharacters, data.getName(), data.getAssignedID());
    		xmlcharacters.setOtus(data.getTaxa().getAssignedID());
    		if ( dataType.equalsIgnoreCase(DNAData.DATATYPENAME) ) {
    			idForState = addFormatElement((DnaCells)xmlcharacters, data);
    		}
    		else if ( dataType.equalsIgnoreCase(RNAData.DATATYPENAME) ) {
    			idForState = addFormatElement((RnaCells)xmlcharacters, data);
    		}    		
    		else if ( dataType.equalsIgnoreCase(ProteinData.DATATYPENAME) ) {
    			idForState = addFormatElement((ProteinCells)xmlcharacters, data);
    		}
    		else if ( dataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
    			idForState = addFormatElement((ContinuousCells)xmlcharacters, data);
    		}
    		else if ( dataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {
    			idForState = addFormatElement((StandardCells)xmlcharacters, data);
    		}    		    		
    		addMatrix(xmlcharacters, data, idForState);
    	}
    }   
    
    private static HashMap addStateDefinitions(AbstractFormat format, CategoricalData data) {
    	AbstractStates states = format.addNewStates();
    	states.setId("states1");
    	String[] labels = data.getSymbols();
    	HashMap idForState = new HashMap();
    	for ( int i = 0; i < labels.length; i++ ) {
    		String stateId = "state" + i;
    		AbstractState state = states.addNewState();
    		XmlAnySimpleType symbol = XmlAnySimpleType.Factory.newInstance();    		
    		symbol.setStringValue(labels[i]);
    		state.setSymbol(symbol);
    		state.setId(stateId);
    		idForState.put(labels[i], stateId);
    	}
    	return idForState;
    }
    
    private static void addCharacterLabels (AbstractFormat format, CharacterData data) {
    	for ( int i = 0; i < data.getNumChars(); i++ ) {
    		AbstractChar c = format.addNewChar();
    		XmlAnySimpleType id = XmlAnySimpleType.Factory.newInstance();
    		id.setStringValue("c" + i);
    		String charName = data.getCharacterName(i);
    		c.setLabel(charName);
	    	if ( data instanceof CategoricalData ) {
	    		c.setStates("states1");
	    	}
    	}
    }
    
    private static HashMap addFormatElement(StandardCells characters, CharacterData data) {
    	StandardFormat format = (StandardFormat)characters.addNewFormat();
    	HashMap idForState = addStateDefinitions(format, (CategoricalData)data);
    	addCharacterLabels(format, data);
    	return idForState;
    }
    
    private static HashMap addFormatElement(DnaCells characters, CharacterData data) {
    	DNAFormat format = (DNAFormat)characters.addNewFormat();  	
    	HashMap idForState = addStateDefinitions(format, (CategoricalData)data);
    	addCharacterLabels(format, data);
    	return idForState;
    }
    
    private static HashMap addFormatElement(RnaCells characters, CharacterData data) {
    	RNAFormat format = (RNAFormat)characters.addNewFormat();
    	HashMap idForState = addStateDefinitions(format, (CategoricalData)data);
    	addCharacterLabels(format, data);
    	return idForState;
    }    
    
    private static HashMap addFormatElement(ProteinCells characters, CharacterData data) {
    	AAFormat format = (AAFormat)characters.addNewFormat();
    	HashMap idForState = addStateDefinitions(format, (CategoricalData)data);
    	addCharacterLabels(format, data);
    	return idForState;
    } 
    
    private static HashMap addFormatElement(ContinuousCells characters, CharacterData data) {
    	ContinuousFormat format = (ContinuousFormat)characters.addNewFormat();
    	// no state definitions here
    	addCharacterLabels(format, data);
    	return new HashMap();
    }    
    
    private static void addMatrix(AbstractBlock characters, CharacterData data, HashMap idForState) {
    	AbstractObsMatrix matrix = ((AbstractCells)characters).addNewMatrix();    
    	AbstractFormat format = characters.getFormat();
    	for ( int i = 0; i < data.getNumTaxa(); i++ ) {
    		AbstractObsRow row = matrix.addNewRow();
    		for ( int j = 0; j < data.getNumChars(); j++ ) {    
    			StringBuffer sb = new StringBuffer(10);
    			data.statesIntoStringBuffer(j, i, sb, false);
    			AbstractObs cell = row.addNewCell();
    			XmlAnySimpleType state = XmlAnySimpleType.Factory.newInstance();
    			XmlAnySimpleType character = XmlAnySimpleType.Factory.newInstance();
    			character.setStringValue("c" + j);
    			state.setStringValue((String)idForState.get(sb.toString()));
    			cell.setChar(character);
    			cell.setState(state);
    		}
    	}
    }
    
    public String getName() {
        // TODO Auto-generated method stub
        return "Nexml import and export";
    }
}