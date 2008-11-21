/**
 * 
 */
package mesquite.nexml.InterpretNEXML;

import java.io.FileInputStream;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.RNAData;
import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;
import mesquite.lib.Arguments;
import mesquite.lib.ExporterDialog;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Tree;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.TreeVector;
import mesquite.lib.ListableVector;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.duties.TreesManager;

/**
 * @author rvosa
 *
 */
public class InterpretNEXML extends FileInterpreterI {
	private static org.biophylo.util.Logger logger = org.biophylo.util.Logger.getInstance();
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
/*.................................................................................................................*/
	public String preferredDataFileExtension() {
 		return "xml";
   	 }
/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;  //
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
	public void readFile(MesquiteProject project, MesquiteFile file, String arguments) {
		logger.VERBOSE(2);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file.getPath());
		} catch ( Exception e ) {
			logger.fatal(e.getMessage());
		}
		Object[] blocks = org.biophylo.IO.parse("Nexml", fs);
		for ( int i = 0; i < blocks.length; i++ ) {
			switch ( ((org.biophylo.Containable)blocks[i]).type() ) {
				case org.biophylo.util.CONSTANT.TAXA   : readTaxaBlocks(project,file,(org.biophylo.taxa.Taxa)blocks[i]);            break;
				case org.biophylo.util.CONSTANT.FOREST : readTreeBlocks(project,file,(org.biophylo.forest.Forest)blocks[i]);        break;
				case org.biophylo.util.CONSTANT.MATRIX : readCharacterBlocks(project,file,(org.biophylo.matrices.Matrix)blocks[i]); break;
			}
		}
	}
	
	private void readCharacterBlocks (MesquiteProject project, MesquiteFile file, org.biophylo.matrices.Matrix xmlMatrix) {		
		String xmlDataType = xmlMatrix.getType();
		String mesDataType = null;
		if ( xmlDataType.equals("Continuous") ) {
			mesDataType = ContinuousData.DATATYPENAME;
		}
		else if ( xmlDataType.equals("Standard") ) {
			mesDataType = CategoricalData.DATATYPENAME;
		}
		else if ( xmlDataType.equals("Dna") ) {
			mesDataType = DNAData.DATATYPENAME;			
		}
		else if ( xmlDataType.equals("Rna") ) {
			mesDataType = DNAData.DATATYPENAME;
		}
		else if ( xmlDataType.equals("Restriction") ) {
			logger.warn("Can't process restriction site data");
		}		
		if ( mesDataType != null ) {
			buildMatrix(mesDataType,xmlMatrix,file);
		}
	}
	
	private void buildMatrix(String dataType,org.biophylo.matrices.Matrix xmlMatrix,MesquiteFile file) {
		CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);
		mesquite.lib.Taxa linkedTaxa = getProject().getTaxa(xmlMatrix.getTaxa().getInternalName());
		mesquite.lib.characters.CharacterData mesData = charTask.newCharacterData(linkedTaxa, 0, dataType);
		org.biophylo.Containable[] xmlDatum = xmlMatrix.getEntities();
		for ( int i = 0; i < xmlDatum.length; i++ ) {
			org.biophylo.taxa.Taxon xmlTaxon = ((org.biophylo.matrices.Datum)xmlDatum[i]).getTaxon();
			String name = xmlTaxon.getName();
			if ( name == null || name.equals("") ) {
				name = xmlTaxon.getInternalName();
			}
			int mesTaxon = mesData.getTaxa().getTaxon(name).getNumber();
			String[] xmlChars = ((org.biophylo.matrices.Datum)xmlDatum[i]).getChar();
			for ( int j = 0; j < xmlChars.length; j++ ) {
				CharacterState cs = null;
        		if ( mesData instanceof ContinuousData ) {
        			cs = new ContinuousState(Double.parseDouble(xmlChars[j]));
        			((ContinuousState)cs).setNumItems(1);
        		}
        		else {
        			cs = new CategoricalState();
        			cs.setValue(xmlChars[j], mesData);
        		}
        		if (mesData.getNumChars() <= j) {
        			mesData.addCharacters(mesData.getNumChars()-1, 1, false);   // add a character if needed
        		}
        		mesData.setState(j, mesTaxon, cs);					
			}
		}
		mesData.setName(xmlMatrix.getName());
		mesData.addToFile(file, getProject(), null);		
	}
	
	private void readTaxaBlocks (MesquiteProject project, MesquiteFile file, org.biophylo.taxa.Taxa xmlTaxa) {
		TaxaManager taxaTask = (TaxaManager)findElementManager(mesquite.lib.Taxa.class);
		int ntax = xmlTaxa.getNtax();
        mesquite.lib.Taxa mesTaxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName(xmlTaxa.getXmlId()), ntax, false);
        mesTaxa.setName(xmlTaxa.getInternalName());
        mesTaxa.addToFile(file, getProject(), taxaTask);
        for ( int i = 0; i < ntax; i++ ) {
        	mesquite.lib.Taxon mesTaxon = mesTaxa.getTaxon(i);
        	org.biophylo.taxa.Taxon xmlTaxon = (org.biophylo.taxa.Taxon)xmlTaxa.getByIndex(i);
        	mesTaxon.setUniqueID(xmlTaxon.getXmlId());
        	String name = xmlTaxon.getName();
        	if ( name==null || name.equals("") ) name = xmlTaxon.getInternalName();
        	mesTaxon.setName(name);
        }		
	}
	
	private void readTreeBlocks (MesquiteProject project, MesquiteFile file, org.biophylo.forest.Forest xmlForest) {
		TreesManager treeTask = (TreesManager)findElementManager(TreeVector.class);
		mesquite.lib.Taxa referencedTaxa = getProject().getTaxa(xmlForest.getTaxa().getInternalName());
		TreeVector mesTreevector = treeTask.makeNewTreeBlock(referencedTaxa, xmlForest.getInternalName(), file);
		org.biophylo.Containable[] xmlTree = xmlForest.getEntities();
		for ( int i = 0; i < xmlTree.length; i++ ) {
			MesquiteTree mesTree = new MesquiteTree(referencedTaxa);
			mesTreevector.addElement(mesTree, false);
			mesTree.setName(xmlTree[i].getInternalName());			
			org.biophylo.forest.Node xmlRoot = ((org.biophylo.forest.Tree)xmlTree[i]).getRoot();
			buildTree(xmlRoot,xmlRoot.getChildren(),mesTree.getRoot(),mesTree);
		}
		mesTreevector.addToFile(file, getProject(), treeTask);
	}
	
	private void buildTree(org.biophylo.forest.Node xmlRoot, org.biophylo.forest.Node[] xmlChildren, int mesRoot, MesquiteTree mesTree) {
		org.biophylo.taxa.Taxon xmlTaxon = xmlRoot.getTaxon();
		if ( xmlTaxon != null ) {
			String name = xmlTaxon.getName();
			if ( name == null || name.equals("") ) {
				name = xmlTaxon.getInternalName();
			}
			Taxon mesTaxon = mesTree.getTaxa().getTaxon(name);
			mesTree.setTaxonNumber(mesRoot, mesTaxon.getNumber(), false);
		}
		mesTree.setNodeLabel(xmlRoot.getInternalName(), mesRoot);		
		mesTree.setBranchLength(mesRoot, xmlRoot.getBranchLength(), false);
		for ( int i = 0; i < xmlChildren.length; i++ ) {
			int mesChild = mesTree.sproutDaughter(mesRoot, false);
			buildTree(xmlChildren[i],xmlChildren[i].getChildren(),mesChild,mesTree);
		}
	}


/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	// XXX make compact/verbose switch
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		/*
		setLineDelimiter(UNIXDELIMITER);
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NBRF Options", buttonPressed);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		
		exportDialog.dispose();
		return ok;
		*/
		return true;
	}	

	
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		logger.VERBOSE(2);
		Arguments args = new Arguments(new Parser(arguments), true);
		MesquiteProject mesProject = getProject();
		ListableVector mesTaxas = getProject().getTaxas();
		org.biophylo.Project xmlProject = convertMesquiteProject(getProject());
		StringBuffer outputBuffer = new StringBuffer();			
		try {
			writeTaxaBlocks(xmlProject,mesTaxas);
			writeCharacterBlocks(xmlProject,getProject().getCharacterMatrices());
			for ( int i = 0; i < mesTaxas.size(); i++ ) {
				Listable[] treeVectors = getProject().getCompatibleFileElements(TreeVector.class, mesTaxas.elementAt(i));
				writeTreeBlocks(xmlProject,treeVectors);
			}
			xmlProject.generateXml(outputBuffer, false);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		saveExportedFileWithExtension(outputBuffer, arguments, "xml");
		return true;
	}
	
	private org.biophylo.Project convertMesquiteProject(MesquiteProject mesProject) {
		org.biophylo.Project xmlProject = new org.biophylo.Project();
		return xmlProject;
	}
	
	private org.biophylo.matrices.datatype.Datatype makeTypeObject(String dataType) {
		org.biophylo.matrices.datatype.Datatype to = null;			
		if ( dataType.equalsIgnoreCase(DNAData.DATATYPENAME) ) {
			to = org.biophylo.matrices.datatype.Datatype.getInstance("Dna");		
		}
		else if ( dataType.equalsIgnoreCase(RNAData.DATATYPENAME) ) {
			to = org.biophylo.matrices.datatype.Datatype.getInstance("Rna");    			
		}    		
		else if ( dataType.equalsIgnoreCase(ProteinData.DATATYPENAME) ) {
			to = org.biophylo.matrices.datatype.Datatype.getInstance("Protein");     			
		}
		else if ( dataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
			to = org.biophylo.matrices.datatype.Datatype.getInstance("Continuous");      			
		}
		else if ( dataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {
			to = org.biophylo.matrices.datatype.Datatype.getInstance("Standard");     			
		}
		if ( to == null ) {
			logger.fatal("No data type object for " + dataType);
		}
		return to;
	}
	
	private org.biophylo.taxa.Taxa findEquivalentTaxa(Taxa mesTaxa,org.biophylo.Project xmlProject) {
		org.biophylo.taxa.Taxa[] xmlTaxa = xmlProject.getTaxa();
		org.biophylo.taxa.Taxa xmlTaxaBlock = null;
		TAXA: for ( int i = 0; i < xmlTaxa.length; i++ ) {
			if ( mesTaxa.getUniqueID().equals(xmlTaxa[i].getGeneric("MesquiteUniqueID")) ) {
				xmlTaxaBlock = xmlTaxa[i];
				break TAXA;
			}
		}
		return xmlTaxaBlock;
	}
	
	private org.biophylo.taxa.Taxon findEquivalentTaxon(Taxon mesTaxon,org.biophylo.taxa.Taxa xmlTaxa) {
		org.biophylo.taxa.Taxon xmlTaxon = null;
		int mesTaxonIndex = mesTaxon.getNumber();
		org.biophylo.Containable[] conts = xmlTaxa.getEntities();
		TAXON: for ( int i = 0; i < conts.length; i++ ) {
			if ( mesTaxonIndex == ((Integer)conts[i].getGeneric("MesquiteUniqueID")).intValue() ) {
				xmlTaxon = (org.biophylo.taxa.Taxon)conts[i];
				break TAXON;
			}
		}		
		return xmlTaxon;
	}
		
	private void writeCharacterBlocks(org.biophylo.Project xmlProject,ListableVector mesCharacters) throws org.biophylo.util.exceptions.ObjectMismatch {
		logger.debug("Going to write characters");
		for ( int i = 0; i < mesCharacters.size(); i++ ) {
			CharacterData mesData = (CharacterData)mesCharacters.elementAt(i);			
			String dataType = mesData.getDataTypeName();
			org.biophylo.matrices.datatype.Datatype to = makeTypeObject(dataType);			
    		org.biophylo.matrices.Matrix xmlMatrix = new org.biophylo.matrices.Matrix();
    		xmlMatrix.setTypeObject(to);
    		Taxa mesTaxa = mesData.getTaxa();
    		org.biophylo.taxa.Taxa xmlTaxa = findEquivalentTaxa(mesTaxa,xmlProject);
    		xmlMatrix.setTaxa(xmlTaxa);
    		int nchar = mesData.getNumChars();
    		for ( int j = 0; j < mesData.getNumTaxa(); j++ ) {
    			CharacterState[] mesChars = mesData.getCharacterStateArray(j, 0, nchar);
    			org.biophylo.matrices.Datum xmlDatum = new org.biophylo.matrices.Datum();
    			xmlDatum.setTypeObject(to);
    			String[] chars = new String[nchar];
    			boolean hasStates = false;
    			for ( int k = 0; k < nchar; k++ ) {
    				String state = mesChars[k].toDisplayString();
    				chars[k] = state;
    				if ( state != null && !state.equals("-") ) {
    					hasStates = true;
    				}
    			}
    			try {
    				if ( hasStates ) {
    					xmlDatum.insert(chars);
    					xmlMatrix.insert(xmlDatum);
    				}
    				
    			} catch ( Exception e ) {
    				logln(e.getMessage());
    			}
    			Taxon mesTaxon = mesData.getTaxa().getTaxon(j);
    			xmlDatum.setTaxon(findEquivalentTaxon(mesTaxon,xmlTaxa));
    		}
    		xmlProject.insert(xmlMatrix);		
		}
	}
	
	private void writeTreeBlocks(org.biophylo.Project xmlProject,Listable[] treeVectors)  throws org.biophylo.util.exceptions.ObjectMismatch {
		logger.debug("Going to write trees");
		for ( int i = 0; i < treeVectors.length; i++ ) {	
			logger.debug("Writing tree block " + i);
			TreeVector mesTrees = (TreeVector)treeVectors[i];
			Taxa mesTaxa = mesTrees.getTaxa();
			org.biophylo.taxa.Taxa xmlTaxa = findEquivalentTaxa(mesTaxa,xmlProject);
			org.biophylo.forest.Forest xmlForest = new org.biophylo.forest.Forest();
			xmlForest.setTaxa(xmlTaxa);
			xmlForest.setName(mesTrees.getName());
			int ntrees = mesTrees.getNumberOfTrees();
			for ( int j = 0; j < ntrees; j++ ) {
				logger.debug("Writing tree " + j);
				Tree mesTree = mesTrees.getTree(j);
				org.biophylo.forest.Tree xmlTree = new org.biophylo.forest.Tree();
				org.biophylo.forest.Node xmlRoot = new org.biophylo.forest.Node();
				xmlTree.setName(mesTree.getName());
				int mesRoot = mesTree.getRoot();
				try {
					buildXmlTree(mesTree,xmlTree,mesRoot,xmlRoot,null,xmlTaxa);
					xmlForest.insert(xmlTree);
				} catch ( Exception e ) {
					logger.fatal(e.getMessage());
					logln(e.getMessage());
					e.printStackTrace();
				}
			}
			xmlProject.insert(xmlForest);
		}		
	}
	
	private void buildXmlTree(Tree mesTree,org.biophylo.forest.Tree xmlTree,int mesNode,org.biophylo.forest.Node xmlNode,org.biophylo.forest.Node xmlParentNode,org.biophylo.taxa.Taxa xmlTaxa) throws org.biophylo.util.exceptions.ObjectMismatch {
		xmlNode.setBranchLength(mesTree.getBranchLength(mesNode));
		xmlNode.setName(mesTree.getNodeLabel(mesNode));
		xmlTree.insert(xmlNode);
		if ( mesTree.nodeIsTerminal(mesNode) ) {
			int[] mesTaxonNumber = mesTree.getTerminalTaxa(mesNode);
			Taxa mesTaxa = mesTree.getTaxa();
			Taxon mesTaxon = mesTaxa.getTaxon(mesTaxonNumber[0]);
			org.biophylo.taxa.Taxon xmlTaxon = findEquivalentTaxon(mesTaxon,xmlTaxa);
			xmlNode.setTaxon(xmlTaxon);		
		}
		xmlNode.setParent(xmlParentNode);
		if ( xmlParentNode != null ) {
			xmlParentNode.setChild(xmlNode);			
		}		
		for (int d = mesTree.firstDaughterOfNode(mesNode); mesTree.nodeExists(d); d = mesTree.nextSisterOfNode(d)) {
			org.biophylo.forest.Node xmlChild = new org.biophylo.forest.Node();
			buildXmlTree(mesTree,xmlTree,d,xmlChild,xmlNode,xmlTaxa);
		}
	}
	
	
	private void writeTaxaBlocks(org.biophylo.Project xmlProject,ListableVector mesTaxas) throws org.biophylo.util.exceptions.ObjectMismatch {
		for ( int i = 0; i < mesTaxas.size(); i++ ) {
			org.biophylo.taxa.Taxa xmlTaxa = new org.biophylo.taxa.Taxa();
			Taxa mesTaxa = (Taxa)mesTaxas.elementAt(i);
			xmlTaxa.setName(mesTaxa.getName());
			String mesTaxaUID = mesTaxa.getUniqueID();
			if ( mesTaxaUID == null ) {
				mesTaxaUID = MesquiteTrunk.getUniqueIDBase() + Taxa.totalCreated;
				mesTaxa.setUniqueID(mesTaxaUID);
			}
			xmlTaxa.setGeneric("MesquiteUniqueID",mesTaxaUID);
			for ( int j = 0; j < mesTaxa.getNumTaxa(); j++ ) {
				org.biophylo.taxa.Taxon xmlTaxon = new org.biophylo.taxa.Taxon();
				xmlTaxon.setName(mesTaxa.getTaxonName(j));
				xmlTaxon.setGeneric("MesquiteUniqueID",new Integer(j));
				try {
					xmlTaxa.insert(xmlTaxon);
				} catch ( Exception e ) {
					logln(e.getMessage());
				}
			}	
			xmlProject.insert(xmlTaxa);
		}		
	}

	/*.................................................................................................................*/
    public String getName() {
		return "NeXML";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports NeXML1.0 files (see http://www.nexml.org)" ;
   	 }
	/*.................................................................................................................*/

}
