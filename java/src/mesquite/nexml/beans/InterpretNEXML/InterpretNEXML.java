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
        <classpath>../../nexml/trunk/nexml/java/jars/jsr173_1.0_api.jar</classpath>
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
import mesquite.categ.lib.*;
import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;
import mesquite.lib.CommandRecord;
import mesquite.lib.Listable;
import mesquite.lib.NameReference;
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

    /*.................................................................................................................*/
    
    
    /**
     * @param arguments
     * @param condition
     * @param hiredByName
     * @return boolean
     */
    /*.................................................................................................................*/
    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
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
            } 
            catch (Exception e) {
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
        } 
        catch (XmlException e) {
            errorReport(e);
        } 
        catch (IOException e) {
        	errorReport(e);
        }
        
        Nexml n = nDoc.getNexml();        
        processOTUBlocks(n,file);
        processCharactersBlocks(n,file);
        processTreesBlocks(n,file);
        
    }
    
    /**
     * Searches current project for a taxa block with the provided ID
     * @param id The value of the id attribute on the otus element
     * @param alsoCheckAssignedID If true, also search for taxa by their assignedID field
     * @return A taxa block
     */
    private mesquite.lib.Taxa getTaxaById(String id, boolean alsoCheckAssignedID ) {
    	mesquite.lib.Taxa theTaxa = null;
    	MesquiteProject project = getProject();
    	for ( int i = 0; i < project.getNumberTaxas(); i++ ) {
    		mesquite.lib.Taxa taxa = project.getTaxa(i);
    		XmlAttributes taxaAttrs = getAttachedXmlAttributes(taxa);
    		if ( taxaAttrs != null ) {
    			if ( id.equals( (String)taxaAttrs.get("id") ) ) {
    				theTaxa = taxa;
    				break;
    			}
    		}
    		if ( alsoCheckAssignedID && id.equals( taxa.getAssignedID() ) ) {
    			theTaxa = taxa;
    			break;
    		}
    	}    	
    	return theTaxa;
    }
    
    private static String getTaxaId(mesquite.lib.Taxa taxa) {
    	String id = null;
    	XmlAttributes attrs = getAttachedXmlAttributes(taxa);
    	if ( attrs != null ) {
    		if ( attrs.containsKey("id") ) {
    			if ( (String)attrs.get("id") != null ) {
    				id = (String)attrs.get("id");
    			}
    		}
    	}
    	if ( id == null ) {
    		id = taxa.getAssignedID();
    	}
    	return id;
    }
    
    private static String getTaxonId(mesquite.lib.Taxa taxa, mesquite.lib.Taxon taxon) {
    	String id = null;
    	int taxonNumber = taxa.whichTaxonNumber(taxon);
    	XmlAttributes attrs = getAssociatedXmlAttributes(taxa,taxonNumber);
    	if ( attrs != null ) {
    		if ( attrs.containsKey("id") ) {
    			if ( (String)attrs.get("id") != null ) {
    				id = (String)attrs.get("id");
    			}
    		}
    	}
    	if ( id == null ) {
    		id = taxon.getUniqueID();
    	}    	
    	return id;
    }
    
    private static String getCharactersId(mesquite.lib.characters.CharacterData data) {
    	String id = null;
    	XmlAttributes attrs = getAttachedXmlAttributes(data);
    	if ( attrs != null ) {
    		if ( attrs.containsKey("id") ) {
    			if ( (String)attrs.get("id") != null ) {
    				id = (String)attrs.get("id");
    			}
    		}
    	}
    	if ( id == null ) {
    		id = data.getAssignedID();
    	}    	
    	return id;
    }
    
    private static String getCharacterId(mesquite.lib.characters.CharacterData data,int ic) {
    	String id = null;
    	XmlAttributes attrs = getAssociatedXmlAttributes(data,ic);
    	if ( attrs != null ) {
    		if ( attrs.containsKey("id") ) {
    			if ( attrs.get("id") != null ) {
    				id = (String)attrs.get("id");
    			}
    		}
    	}
    	if ( id == null ) {
    		id = "c" + ic;
    	}
    	return id;
    }
    
    /**
     * Searches provided taxa block for a taxon with the provided ID
     * @param taxa A taxa block to search 
     * @param id The value of the id attribute of the otu element
     * @param alsoCheckUniqueId If true, also match against the uniqueID field
     * @return A matching taxon
     */
    private mesquite.lib.Taxon getTaxonById(mesquite.lib.Taxa taxa, String id, boolean alsoCheckUniqueId ) {
    	mesquite.lib.Taxon theTaxon = null;
    	NameReference nref = taxa.makeAssociatedObjects("NexmlAttributes");
		for ( int i = 0; i < taxa.getNumTaxa(); i++ ) {
			XmlAttributes taxonMap = (XmlAttributes) taxa.getAssociatedObject(nref,i);
			if ( taxonMap != null ) {
				String taxonId = (String)taxonMap.get("id");
				if ( taxonId != null ) {
					if ( id.equals(taxonId) ) {
						theTaxon = taxa.getTaxon(i);
						break;
					}
				}
			}
			if ( alsoCheckUniqueId && taxa.getTaxon(i).getUniqueID().equals(id) ) {
				theTaxon = taxa.getTaxon(i);
				break;
			}
		}    	
    	return theTaxon;
    }
    
    private void processTreesBlocks(Nexml nexml, MesquiteFile file) {
    	Trees[] treeBlocks = nexml.getTreesArray();
    	if ( treeBlocks != null && treeBlocks.length > 0 ) {
    		TreesManager treeTask = (TreesManager)findElementManager(TreeVector.class);  
    		for ( int i = 0; i < treeBlocks.length; i++ ) {
    			Trees currentBlock = treeBlocks[i];
    			mesquite.lib.Taxa referencedTaxa = getTaxaById(currentBlock.getOtus(), true);
    			AbstractTree[] trees = currentBlock.getTreeArray();
    			TreeVector treevector = treeTask.makeNewTreeBlock(referencedTaxa, currentBlock.getLabel(), file);
    			XmlAttributes treesAttr = processStandardAttributes(currentBlock);
    			treevector.attach(treesAttr);
    			if ( trees != null && trees.length > 0 ) {
    				for ( int j = 0; j < trees.length; j++ ) {    					
    					if ( ! ( trees[j] instanceof AbstractNetwork ) ) {
    						MesquiteTree tree = new MesquiteTree(referencedTaxa);
    						tree.setName(trees[j].getId()); 
    						XmlAttributes attr = processStandardAttributes(trees[j]);
    						tree.attach(attr);
    						treevector.addElement(tree, false);
    						TreeNode root = (TreeNode)trees[j].getNodeArray(0);
    						XmlAttributes nodeAttr = processStandardAttributes(root);
    						tree.setRooted(root.getRoot(), false);  
    						Vector children = getChildNodes(root, trees[j]);
    						int rootNode = tree.getRoot();
    						NameReference nRef = tree.makeAssociatedObjects("NexmlAttributes");
    						tree.setAssociatedObject(nRef, rootNode, nodeAttr);
    						for ( int k = 0; k < children.size(); k++ ) {
    							buildTree(tree,(AbstractNode)children.get(k),rootNode,(AbstractTree)trees[j], nRef);
    						}
    					}
    					else { // XXX networks
    					}
    				}
    				treevector.addToFile(file, getProject(), treeTask);
    			}
    		}
    	}
    }    
    
    private void buildTree (MesquiteTree mesquiteTree, AbstractNode currentXmlNode, int parentNode, AbstractTree xmlTree, NameReference nRef) {
    	int currentNode = mesquiteTree.sproutDaughter(parentNode, false);    	    	
    	String otuIdRef = currentXmlNode.getOtu();
    	if ( otuIdRef != null ) {
    		mesquite.lib.Taxa taxa = mesquiteTree.getTaxa();
    		mesquite.lib.Taxon taxon = getTaxonById(taxa, otuIdRef, true);
    		int taxonNumber = taxa.whichTaxonNumber(taxon);
    		mesquiteTree.setTaxonNumber(currentNode, taxonNumber, false);
    	}
    	AbstractEdge theEdge = getEdge(currentXmlNode, xmlTree);
    	mesquiteTree.setNodeLabel(currentXmlNode.getLabel(), currentNode);
    	mesquiteTree.setBranchLength(currentNode, Double.parseDouble(theEdge.getLength().getStringValue()), false);
    	Vector theChildren = getChildNodes(currentXmlNode, xmlTree);
    	for ( int i = 0; i < theChildren.size(); i++ ) {
    		buildTree(mesquiteTree, (AbstractNode)theChildren.get(i), currentNode, xmlTree, nRef);
    	}
    	XmlAttributes attrs = processStandardAttributes(currentXmlNode);
    	XmlAttributes edgeAttrs = processStandardAttributes(theEdge);
    	attrs.put("edgeAttributes", edgeAttrs);
    	mesquiteTree.setAssociatedObject(nRef, currentNode, attrs);    	
    }
    
    private static Vector getChildNodes (AbstractNode parent, AbstractTree tree) {
    	Vector children = new Vector();
    	AbstractNode[] nodes = tree.getNodeArray();
    	AbstractEdge[] edges = tree.getEdgeArray();
    	String nodeId = parent.getId();
    	for ( int i = 0; i < edges.length; i++ ) {
    		if ( edges[i].getSource().equals(nodeId) ) {
    			String childId = edges[i].getTarget();
    			for ( int j = 0; j < nodes.length; j++ ) {
    				if ( nodes[j].getId().equals(childId) ) {
    					children.add(nodes[j]);
    				}
    			}    			
    		}
    	}    	
    	return children;
    }
    
    private static AbstractEdge getEdge (AbstractNode node, AbstractTree tree) {
    	AbstractEdge[] edges = tree.getEdgeArray();
    	AbstractEdge edge = null;
    	String nodeId = node.getId();
    	for ( int i = 0; i < edges.length; i++ ) {
    		if ( edges[i].getTarget().equals(nodeId) ) {
    			edge = edges[i];
    		}
    	}
    	return edge;
    }
    
    private void processOTUBlocks(Nexml n,MesquiteFile file){
        Taxa[] o = n.getOtusArray();
        if ( o != null && o.length > 0 ) {
            TaxaManager taxaTask = (TaxaManager)findElementManager(mesquite.lib.Taxa.class);            
            for ( int taxaCount = 0; taxaCount < o.length; taxaCount++ ) {
                Taxa currentBlock = o[taxaCount];
                int taxaInBlock = currentBlock.getOtuArray().length;
                mesquite.lib.Taxa taxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName(currentBlock.getLabel()), taxaInBlock, false);
                if ( taxa != null ) {
                	XmlAttributes taxaAttrs = processStandardAttributes(currentBlock);
                	taxa.attach(taxaAttrs);
                	taxa.setName(currentBlock.getLabel());
                    taxa.addToFile(file, getProject(), taxaTask);
                    NameReference nref = taxa.makeAssociatedObjects("NexmlAttributes");
                    for ( int taxaCounter = 0; taxaCounter < taxaInBlock; taxaCounter++ ){
                        mesquite.lib.Taxon t = taxa.getTaxon(taxaCounter);
                        if ( t != null ) {
                        	org.nexml.x10.Taxon currentTaxon = currentBlock.getOtuArray(taxaCounter);
                        	XmlAttributes attrs = processStandardAttributes(currentTaxon);
                        	taxa.setAssociatedObject(nref, taxaCounter, attrs);
                        	if ( currentTaxon.getLabel() != null ) {
                        		t.setName(currentTaxon.getLabel());
                        	}
                        	else {
                        		//t.setName(currentTaxon.getId()); //XXX
                        	}
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Parses out the standard xml attributes - xml:base, xlink:href, xml:lang, class
     * and id - on nexml objects and assigns them to an XmlAttributes object.
     * @param obj a nexml object
     * @return an XmlAttributes object, i.e. an empty subclass of HashTable
     */
    private static XmlAttributes processStandardAttributes (org.nexml.x10.Base obj) {
    	XmlAttributes attrs = new XmlAttributes();
    	if ( obj.getBase() != null ) {
    		attrs.put("xml:base", obj.getBase());
    	}
    	if ( obj.getHref() != null ) {
    		attrs.put("xlink:href", obj.getHref());
    	}
    	if ( obj.getLang() != null ) {
    		attrs.put("xml:lang", obj.getLang());
    	}
    	if ( obj.getClass1() != null ) {
    		attrs.put("class", obj.getClass1());
    	}
    	if ( obj instanceof org.nexml.x10.IDTagged ) {
    		attrs.put("id", ((org.nexml.x10.IDTagged)obj).getId());
    	}
    	if ( obj instanceof org.nexml.x10.Annotated ) {
    		Dict dict[] = ((org.nexml.x10.Annotated)obj).getDictArray();
    		if ( dict != null ) {
    			attrs.put("dict", dict);
    		}
    	}
    	return attrs;
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
        if ( c != null && c.length > 0 ){
            CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);

            for( int charBlockCount = 0; charBlockCount < c.length; charBlockCount++ ){
                AbstractBlock currentBlock = c[charBlockCount];
                String linkedTaxaId = currentBlock.getOtus();
                mesquite.lib.characters.CharacterData data = null;
                if (currentBlock instanceof AbstractSeqs){
                    if (currentBlock instanceof ContinuousSeqs){
                    	data = processSeqCharBlock(charTask,linkedTaxaId,currentBlock,ContinuousData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof DnaSeqs){
                        data = processSeqCharBlock(charTask,linkedTaxaId,currentBlock,DNAData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof ProteinSeqs){
                    	data = processSeqCharBlock(charTask,linkedTaxaId,currentBlock,ProteinData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof RestrictionSeqs){
                        MesquiteMessage.warnUser("Restriction data not recognized by mesquite in block " + currentBlock.getId());                       
                    }
                    else if (currentBlock instanceof RnaSeqs){
                    	data = processSeqCharBlock(charTask,linkedTaxaId,currentBlock,DNAData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof StandardSeqs){
                    	data = processSeqCharBlock(charTask,linkedTaxaId,currentBlock,CategoricalData.DATATYPENAME);
                    } 
                    else 
                        MesquiteMessage.warnProgrammer("Unrecognized character matrix type " + currentBlock.getClass() + " in block " + currentBlock.getId());
                }
                else if (currentBlock instanceof AbstractCells){
                    if (currentBlock instanceof ContinuousCells){
                        data = processCellCharBlock(charTask,linkedTaxaId,currentBlock,ContinuousData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof DnaCells){
                        data = processCellCharBlock(charTask,linkedTaxaId,currentBlock,DNAData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof ProteinCells){
                        data = processCellCharBlock(charTask,linkedTaxaId,currentBlock,ProteinData.DATATYPENAME);
                    }
                    else if (currentBlock instanceof RestrictionCells){
                        MesquiteMessage.warnUser("Restriction data not recognized by mesquite in block " + currentBlock.getId());                       
                    }
                    else if (currentBlock instanceof RnaCells){
                        data = processCellCharBlock(charTask,linkedTaxaId,currentBlock,DNAData.DATATYPENAME); // XXX so how does mesquite handle RNA?
                    }
                    else if (currentBlock instanceof StandardCellsImpl){
                        data = processCellCharBlock(charTask,linkedTaxaId,currentBlock,CategoricalData.DATATYPENAME);
                    }
                    else
                        MesquiteMessage.warnProgrammer("Unrecognized character matrix type " + currentBlock.getClass() + " in block " + currentBlock.getId());
                }
                else
                    MesquiteMessage.warnProgrammer("Unrecognized character matrix type " + currentBlock.getClass() + " in block " + currentBlock.getId());
                if ( data != null ) {
                	XmlAttributes attrs = processStandardAttributes(currentBlock);
                	data.attach(attrs);
                    data.setName(currentBlock.getLabel());
                    data.addToFile(file, getProject(), null);
                }
            }
        }
    }
    
    private static void processAbstractState(AbstractState[] stateSet,HashMap symbolForId) {
    	for ( int i = 0; i < stateSet.length; i++ ) {
    		String stateId = stateSet[i].getId();
    		String stateSymbol = stateSet[i].getSymbol().getStringValue();
    		symbolForId.put(stateId, stateSymbol);
    	}    	
    }

    private HashMap processFormatElement (AbstractFormat format,mesquite.lib.characters.CharacterData data) {
    	HashMap stateSetForId = new HashMap();
    	HashMap stateSetForChar = new HashMap();
    	AbstractStates states[] = format.getStatesArray();
    	AbstractChar characters[] = format.getCharArray();
    	for ( int i = 0; i < states.length; i++ ) {
    		String stateSetId = states[i].getId();
    		HashMap symbolForId = new HashMap();
    		AbstractState stateSet[] = states[i].getStateArray();
    		AbstractPolymorphicStateSet apss[] = states[i].getPolymorphicStateSetArray();
    		AbstractUncertainStateSet uss[] = states[i].getUncertainStateSetArray();    		
    		processAbstractState(stateSet,symbolForId);
    		processAbstractState(apss,symbolForId);
    		processAbstractState(uss,symbolForId);
    		stateSetForId.put(stateSetId, symbolForId);
    	}
    	data.addParts(-1,characters.length);
    	for ( int i = 0; i < characters.length; i++ ) {
    		data.setCharacterName(i, characters[i].getLabel());
    		String charId = characters[i].getId();
    		String statesId = characters[i].getStates();
    		stateSetForChar.put(charId, stateSetForId.get(statesId));
    		XmlAttributes charAttrs = processStandardAttributes(characters[i]);
    		NameReference nRef = data.makeAssociatedObjects("NexmlAttributes");
    		data.setAssociatedObject(nRef, i, charAttrs);
    	}    	
    	return stateSetForChar;
    }
    
    private void populateMatrix(HashMap stateSetForChar,AbstractObsMatrix currentBlock,mesquite.lib.characters.CharacterData data) {
    	AbstractObsRow rows[] = currentBlock.getRowArray();
    	NameReference nRef = data.makeAssociatedObjects("NexmlAttributes");
    	mesquite.lib.Associable ti = data.getTaxaInfo(true);    
    	for ( int i = 0; i < rows.length; i++ ) {
    		XmlAttributes rowAttrs = processStandardAttributes(rows[i]);    		
    		ti.setAssociatedObject(nRef, i, rowAttrs);    		
    		AbstractObs cells[] = rows[i].getCellArray();
    		for ( int j = 0; j < cells.length; j++ ) {
    			XmlAttributes cellAttrs = processStandardAttributes(cells[j]);
    			data.setCellObject(nRef, j, i, cellAttrs);
    			String charId = cells[j].getChar().getStringValue();
    			String stateId = cells[j].getState().getStringValue();
    			String symbol = null;
    			int charIndex = -1;
    			if ( stateSetForChar.containsKey(charId) ) {
    				HashMap states = (HashMap)stateSetForChar.get(charId);
    				if ( states != null ) {
    					symbol = (String)states.get(stateId);
    				}
    			}
    			if ( symbol == null ) {
    				symbol = stateId;
    			}
    			for ( int k = 0; k < data.getNumChars(); k++ ) {
    				XmlAttributes charAttrs = (XmlAttributes)data.getAssociatedObject(nRef, k);
    				if ( charAttrs != null ) {
    					String id = (String)charAttrs.get("id");
    					if ( id != null ) {
    						if ( id.equals(charId) ) {
    							charIndex = k;
    							break;
    						}
    					}
    				}
    			}
    			CharacterState cs = null;
    			if ( currentBlock instanceof ContinuousObsMatrix ) {
    				cs = new ContinuousState();
    			}
    			else {
    				cs = new CategoricalState();
    			}
				cs.setValue(symbol, data);
				data.setState(charIndex, i, cs);    			
    		}
    	}
    }    

    private CharacterData processCellCharBlock(CharactersManager charTask, String linkedTaxaId, AbstractBlock currentBlock, String dataType){
        mesquite.lib.Taxa linkedTaxa = getTaxaById(linkedTaxaId,true);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, dataType);
        AbstractObsMatrix obsMatrix = (AbstractObsMatrix)((AbstractCells)currentBlock).getMatrix();
        HashMap stateSetForChar = null;
        AbstractFormat format = null;             
        if ( currentBlock.isSetFormat() ) {
        	format = currentBlock.getFormat();
            stateSetForChar = processFormatElement(format,data);
        }     
        populateMatrix(stateSetForChar,obsMatrix,data);        
        return data;
    }

    private CharacterData processSeqCharBlock(CharactersManager charTask,String linkedTaxaId,AbstractBlock currentBlock,String dataType){
    	mesquite.lib.Taxa linkedTaxa = getTaxaById(linkedTaxaId,true);
        mesquite.lib.characters.CharacterData data = charTask.newCharacterData(linkedTaxa, 0, dataType);
        AbstractSeqMatrix matrix = ((AbstractSeqs)currentBlock).getMatrix();
        AbstractSeqRow [] rows = matrix.getRowArray();
    	NameReference nRef = data.makeAssociatedObjects("NexmlAttributes");
    	mesquite.lib.Associable ti = data.getTaxaInfo(true);        
        for ( int i = 0; i < rows.length; i++ ) {
    		XmlAttributes rowAttrs = processStandardAttributes(rows[i]);    		
    		ti.setAssociatedObject(nRef, i, rowAttrs);         	
            AbstractSeqRow curRow = rows[i];
            String curOtu = curRow.getOtu();
            mesquite.lib.Taxon t = getTaxonById(linkedTaxa,curOtu,true);
            int it = linkedTaxa.whichTaxonNumber(t);  //TODO how to handle -1 returns?
            XmlAnySimpleType x = curRow.getSeq();             
            if ( x instanceof AbstractTokenList ) {
            	AbstractTokenList curSeq = (AbstractTokenList)x;
	        	List seqList = curSeq.getListValue();
	        	int ic = 0;
	        	Iterator elementItr = seqList.iterator();
	        	while ( elementItr.hasNext() ) {
	        		Double element = (Double)elementItr.next();
	        		CharacterState cs = null;
	        		if ( data instanceof ContinuousData ) {
	        			cs = new ContinuousState(element.doubleValue());
	        			((ContinuousState)cs).setNumItems(1);
	        		}
	        		else {
	        			cs = new CategoricalState();
	        			cs.setValue(element.toString(), data);
	        		}
	        		if (data.getNumChars() <= ic) {
	        			data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed
	        		}
	        		data.setState(ic, it, cs);	
	        		ic++;
	        	}
	        }
	        else {
	        	AbstractSeq curSeq = (AbstractSeq)x; 
	        	String curString = curSeq.getStringValue();
	        	int ic = 0;
	        	for ( int pos = 0; pos < curString.length(); pos++ ) {
	        		char curChar = curString.charAt(pos);
	        		if ( !Character.isSpaceChar(curChar) ) {
	        			if ( data.getNumChars() <= ic ) {
	        				data.addCharacters(data.getNumChars()-1, 1, false);
	        			}
	        			((CategoricalData)data).setState(ic++, it, curChar);
	        		}
	        	}
            }
        }
        return data;
    }
    
    public void exportFile(MesquiteFile file, String arguments) {
    	MesquiteProject project = getProject();
    	NexmlDocument doc = NexmlDocument.Factory.newInstance();    
    	Nexml nexml = doc.addNewNexml();
    	nexml.setVersion(new java.math.BigDecimal(1.0));
    	nexml.setGenerator("mesquite");
    	addOtusElements(project, nexml);
    	addCharactersElements(project, nexml);
    	addTreesElements(project, nexml);
    	System.out.print(doc.toString());
    	StringBuffer outputBuffer = new StringBuffer();
    	outputBuffer.append(doc.toString());
    	saveExportedFileWithExtension(outputBuffer, arguments, "xml");
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
    	XmlAttributes treesAttrs = getAttachedXmlAttributes(treeVector); 
    	if ( treesAttrs == null ) {
    		treesAttrs = new XmlAttributes();
    	}
    	if ( ! treesAttrs.containsKey("id") ) {
    		treesAttrs.put("id", treeVector.getAssignedID());
    	}
    	if ( ! treesAttrs.containsKey("otus") ) {
    		XmlAttributes taxaAttrs = getAttachedXmlAttributes(taxa);
    		String otusId;
    		if ( taxaAttrs != null ) {
    			if ( taxaAttrs.containsKey("id") ) {
    				otusId = (String)taxaAttrs.get("id");
    			}
    			else {
    				otusId = taxa.getAssignedID();
    			}
    		}
    		else {
    			otusId = taxa.getAssignedID();
    		}    		
    		trees.setOtus(otusId);
    	}
    	addCommonAttributes(trees, treeVector.getName(), treesAttrs);    	
    	for ( int i = 0; i < treeVector.getNumberOfTrees(); i++ ) {
    		MesquiteTree tree = (MesquiteTree)treeVector.getTree(i);
    		AbstractTree tr = trees.addNewTree();
    		FloatTree xmltree = FloatTree.Factory.newInstance();
    		int size = trees.sizeOfTreeArray();    		
    		XmlAttributes treeAttrs = new XmlAttributes();
    		treeAttrs.put("id", "" + tree.getID());
    		addCommonAttributes(xmltree, tree.getName(), treeAttrs);    
    		NameReference nRef = tree.makeAssociatedObjects("NexmlAttributes");
    		addTreeElement(xmltree, tree, tree.getRoot(), nRef);
    		trees.setTreeArray(size - 1, xmltree);
    	}
    }
    
    private static String getNodeId(MesquiteTree tree, int N, NameReference nRef) {
    	String nodeId = "node" + N;
    	XmlAttributes nodeAttrs = (XmlAttributes)tree.getAssociatedObject(nRef, N);
    	if ( nodeAttrs != null ) {
    		if ( nodeAttrs.containsKey("id") ) {
    			nodeId = (String)nodeAttrs.get("id");
    		}
    	}    	
    	return nodeId;
    }
    
    private static XmlAnySimpleType makeBranchLength(MesquiteTree tree, int node) {
    	XmlAnySimpleType length = XmlAnySimpleType.Factory.newInstance();
    	length.setStringValue("" + tree.getBranchLength(node));
    	return length;
    }
    
    private static void addTreeElement(FloatTree xmltree, MesquiteTree tree, int N, NameReference nRef) {		
    	TreeNode node = (TreeNode)xmltree.addNewNode();		
    	XmlAttributes nodeAttrs = (XmlAttributes)tree.getAssociatedObject(nRef, N);
    	XmlAttributes edgeAttrs = null;
    	if ( nodeAttrs != null ) {
    		edgeAttrs = (XmlAttributes)nodeAttrs.get("edgeAttributes");
    	}
    	String nodeId = getNodeId(tree,N,nRef);
		addCommonAttributes(node, tree.getNodeLabel(N), nodeAttrs);   
		int mother = tree.motherOfNode(N);
		int taxon  = tree.taxonNumberOfNode(N);		
		if ( N != tree.getRoot() ) {
			TreeFloatEdge edge = (TreeFloatEdge)xmltree.addNewEdge();
			edge.setSource(getNodeId(tree,mother,nRef));
			edge.setTarget(nodeId);
			edge.setLength(makeBranchLength(tree,N));			
			addCommonAttributes(edge,null,edgeAttrs);
		}	
		else {
			node.setRoot(tree.rootIsReal());			
			if ( mesquite.lib.MesquiteDouble.equals( 0.00, tree.getBranchLength(N), 0.0001 ) ) {
				TreeFloatRootEdge edge = (TreeFloatRootEdge)xmltree.addNewRootedge();
				edge.setTarget(nodeId);
				edge.setLength(makeBranchLength(tree,N));
				addCommonAttributes(edge,null,edgeAttrs);
			}
		}
		if ( taxon != -1 ) {
			XmlAttributes taxonAttrs = (XmlAttributes)tree.getTaxa().getAssociatedObject(nRef, taxon);
			if ( taxonAttrs != null ) {
				String otuId = (String)taxonAttrs.get("id");
				if ( otuId != null ) {
					node.setOtu(otuId);
				}
				else {
					node.setOtu(tree.getTaxa().getTaxon(taxon).getUniqueID());
				}
			}
			else {
				node.setOtu(tree.getTaxa().getTaxon(taxon).getUniqueID());
			}
		}
    	for ( int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d) ) {
    		addTreeElement(xmltree, tree, d, nRef);
    	}
    }
  
    private static void addCommonAttributes(IDTagged obj, String label, XmlAttributes attrs) {
    	if ( label != null ) {
    		obj.setLabel(label);
    	}
    	if ( attrs != null ) {
	    	if ( attrs.containsKey("xml:base") ) {
	    		obj.setBase((String)attrs.get("xml:base"));
	    	}
	    	if ( attrs.containsKey("xlink:href") ) {
	    		obj.setHref((String)attrs.get("xlink:href"));
	    	}
	    	if ( attrs.containsKey("xml:lang") ) {
	    		obj.setLang((String)attrs.get("xml:lang"));
	    	}
	    	if ( attrs.containsKey("class") ) {
	    		//obj.setClass1((String)attrs.get("class"));
	    	}
	    	if ( attrs.containsKey("id") ) {
	    		obj.setId((String)attrs.get("id"));
	    	}
	    	if ( attrs.containsKey("dict") ) {
	    		Dict[] dictArray = (Dict[])attrs.get("dict");
	    		((org.nexml.x10.Annotated)obj).setDictArray(dictArray);
	    	}
    	}
    }
    
    public static XmlAttributes getAttachedXmlAttributes (mesquite.lib.Attachable obj) {
    	Vector attachments = obj.getAttachments();
    	XmlAttributes theAttachment = new XmlAttributes();
    	if ( attachments != null ) {
	    	for ( int i = 0; i < attachments.size(); i++ ) {
	    		Object att = attachments.get(i);
	    		if ( att instanceof XmlAttributes ) {
	    			theAttachment = (XmlAttributes)att;
	    		}
	    	}
    	}
    	return theAttachment;
    }
    
    public static XmlAttributes getAssociatedXmlAttributes (mesquite.lib.Associable obj, int i) {
    	XmlAttributes theAttachment = null;
    	NameReference nRef = obj.makeAssociatedObjects("NexmlAttributes");
    	theAttachment = (XmlAttributes)obj.getAssociatedObject(nRef, i);
    	if ( theAttachment == null ) {
    		theAttachment = new XmlAttributes();
    		obj.setAssociatedObject(nRef, i, theAttachment);
    	}
    	return theAttachment;
    }
    
    private static void addOtusElements (MesquiteProject project, Nexml nexml) {
    	for ( int i = 0; i < project.getNumberTaxas(); i++ ) {
    		mesquite.lib.Taxa taxa = project.getTaxa(i);
    		NameReference nRef = taxa.makeAssociatedObjects("NexmlAttributes");
    		Taxa xmltaxa = nexml.addNewOtus();
    		XmlAttributes taxaAttrs = getAttachedXmlAttributes(taxa);
    		addCommonAttributes(xmltaxa, taxa.getName(), taxaAttrs);
    		for ( int j = 0; j < taxa.getNumTaxa(); j++ ) {
    			XmlAttributes attrs = (XmlAttributes)taxa.getAssociatedObject(nRef, j);
    			mesquite.lib.Taxon taxon = taxa.getTaxon(j);
    			Taxon xmltaxon = xmltaxa.addNewOtu();
    			addCommonAttributes(xmltaxon, taxon.getName(), attrs);
    		} 
    	}    	
    }
    
    private static void addCharactersElements (MesquiteProject project, Nexml nexml) {
    	for ( int i = 0; i < project.getNumberCharMatrices(); i++ ) {
    		mesquite.lib.characters.CharacterData data = project.getCharacterMatrix(i);
    		String dataType = data.getDataTypeName();
    		AbstractBlock chr = nexml.addNewCharacters();
    		AbstractBlock xmlcharacters = null;
    		HashMap idForState = new HashMap();    		
    		if ( dataType.equalsIgnoreCase(DNAData.DATATYPENAME) ) {
    			xmlcharacters = DnaCells.Factory.newInstance();
    			idForState = addFormatElement((DnaCells)xmlcharacters, data);      			
    		}
    		else if ( dataType.equalsIgnoreCase(RNAData.DATATYPENAME) ) {
    			xmlcharacters = RnaCells.Factory.newInstance();
    			idForState = addFormatElement((RnaCells)xmlcharacters, data);      			
    		}    		
    		else if ( dataType.equalsIgnoreCase(ProteinData.DATATYPENAME) ) {
    			xmlcharacters = ProteinCells.Factory.newInstance();
    			idForState = addFormatElement((ProteinCells)xmlcharacters, data);      			
    		}
    		else if ( dataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
    			xmlcharacters = ContinuousCells.Factory.newInstance();
    			idForState = addFormatElement((ContinuousCells)xmlcharacters, data);      			
    		}
    		else if ( dataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {
    			xmlcharacters = StandardCells.Factory.newInstance();
    			idForState = addFormatElement((StandardCells)xmlcharacters, data);      			
    		}
    		addMatrix(xmlcharacters, data, idForState);
    		XmlAttributes attrs = getAttachedXmlAttributes(data);
    		attrs.put("id", getCharactersId(data));
    		addCommonAttributes(xmlcharacters, data.getName(), attrs);
    		xmlcharacters.setOtus(getTaxaId(data.getTaxa()));
    		int size = nexml.sizeOfCharactersArray();
    		nexml.setCharactersArray(size - 1, xmlcharacters);    		
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
    		XmlAttributes attr = getAssociatedXmlAttributes(data,i);
    		if ( ! attr.containsKey("id") ) {
    			attr.put("id", "c" + i);
    		}
    		AbstractChar c = format.addNewChar();
    		addCommonAttributes((IDTagged)c,data.getCharacterName(i),attr);    		
	    	if ( format instanceof StandardFormat ) {
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
    	addCharacterLabels(format, data);
    	return new HashMap();
    }
    
    private static HashMap addFormatElement(RnaCells characters, CharacterData data) {
    	RNAFormat format = (RNAFormat)characters.addNewFormat();
    	addCharacterLabels(format, data);
    	return new HashMap();
    }    
    
    private static HashMap addFormatElement(ProteinCells characters, CharacterData data) {
    	AAFormat format = (AAFormat)characters.addNewFormat();
    	addCharacterLabels(format, data);
    	return new HashMap();
    } 
    
    private static HashMap addFormatElement(ContinuousCells characters, CharacterData data) {
    	ContinuousFormat format = (ContinuousFormat)characters.addNewFormat();
    	addCharacterLabels(format, data);
    	return new HashMap();
    }    
    
    private static void addMatrix(AbstractBlock characters, CharacterData data, HashMap idForState) {
    	AbstractObsMatrix matrix = ((AbstractCells)characters).addNewMatrix();    
    	mesquite.lib.Associable taxaInfo = data.getTaxaInfo(true);
    	NameReference nRef = data.makeAssociatedObjects("NexmlAttributes");
    	for ( int i = 0; i < data.getNumTaxa(); i++ ) {
    		AbstractObsRow row = matrix.addNewRow();
    		for ( int j = 0; j < data.getNumChars(); j++ ) {
    			StringBuffer sb = new StringBuffer(10);
    			data.statesIntoStringBuffer(j, i, sb, false);
    			AbstractObs cell = row.addNewCell();
    			XmlAnySimpleType state = XmlAnySimpleType.Factory.newInstance();
    			XmlAnySimpleType character = XmlAnySimpleType.Factory.newInstance();
    			character.setStringValue( getCharacterId(data,j) );
    			String symbol = (String)idForState.get(sb.toString());
    			if ( symbol != null ) {
    				state.setStringValue((String)idForState.get(sb.toString()));
    			}
    			else {
    				state.setStringValue(sb.toString());
    			}
    			cell.setChar(character);
    			cell.setState(state);
    			XmlAttributes cellAttrs = (XmlAttributes)data.getCellObject(nRef, j, i);
    			if ( cellAttrs != null ) {
    				if ( cellAttrs.containsKey("dict") ) {
    					Dict[] dict = (Dict[])cellAttrs.get("dict");
    					cell.setDictArray(dict);
    				}
    			}    			
    		}
    		XmlAttributes rowAttrs = getAssociatedXmlAttributes(taxaInfo,i);
    		addCommonAttributes(row,null,rowAttrs);
    		String otu = getTaxonId(data.getTaxa(),data.getTaxa().getTaxon(i));
    		row.setOtu(otu);
    	}
    }
    
    public String getName() {
        return "Nexml import and export";
    }
}

class XmlAttributes extends HashMap {
	
}