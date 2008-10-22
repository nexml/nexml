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
	private static org.biophylo.Util.Logger logger = org.biophylo.Util.Logger.getInstance();
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
				case org.biophylo.Util.CONSTANT.TAXA   : readTaxaBlocks(project,file,(org.biophylo.Taxa.Taxa)blocks[i]);            break;
				case org.biophylo.Util.CONSTANT.FOREST : readTreeBlocks(project,file,(org.biophylo.Forest.Forest)blocks[i]);        break;
				case org.biophylo.Util.CONSTANT.MATRIX : readCharacterBlocks(project,file,(org.biophylo.Matrices.Matrix)blocks[i]); break;
			}
		}
	}
	
	private void readCharacterBlocks (MesquiteProject project, MesquiteFile file, org.biophylo.Matrices.Matrix xmlMatrix) {		
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
	
	private void buildMatrix(String dataType,org.biophylo.Matrices.Matrix xmlMatrix,MesquiteFile file) {
		CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);
		mesquite.lib.Taxa linkedTaxa = getProject().getTaxa(xmlMatrix.getTaxa().getInternalName());
		mesquite.lib.characters.CharacterData mesData = charTask.newCharacterData(linkedTaxa, 0, dataType);
		org.biophylo.Containable[] xmlDatum = xmlMatrix.getEntities();
		for ( int i = 0; i < xmlDatum.length; i++ ) {
			org.biophylo.Taxa.Taxon xmlTaxon = ((org.biophylo.Matrices.Datum)xmlDatum[i]).getTaxon();
			String name = xmlTaxon.getName();
			if ( name == null || name.equals("") ) {
				name = xmlTaxon.getInternalName();
			}
			int mesTaxon = mesData.getTaxa().getTaxon(name).getNumber();
			String[] xmlChars = ((org.biophylo.Matrices.Datum)xmlDatum[i]).getChar();
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
	
	private void readTaxaBlocks (MesquiteProject project, MesquiteFile file, org.biophylo.Taxa.Taxa xmlTaxa) {
		TaxaManager taxaTask = (TaxaManager)findElementManager(mesquite.lib.Taxa.class);
		int ntax = xmlTaxa.getNtax();
        mesquite.lib.Taxa mesTaxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName(xmlTaxa.getXmlId()), ntax, false);
        mesTaxa.setName(xmlTaxa.getInternalName());
        mesTaxa.addToFile(file, getProject(), taxaTask);
        for ( int i = 0; i < ntax; i++ ) {
        	mesquite.lib.Taxon mesTaxon = mesTaxa.getTaxon(i);
        	org.biophylo.Taxa.Taxon xmlTaxon = (org.biophylo.Taxa.Taxon)xmlTaxa.getByIndex(i);
        	mesTaxon.setUniqueID(xmlTaxon.getXmlId());
        	String name = xmlTaxon.getName();
        	if ( name==null || name.equals("") ) name = xmlTaxon.getInternalName();
        	mesTaxon.setName(name);
        }		
	}
	
	private void readTreeBlocks (MesquiteProject project, MesquiteFile file, org.biophylo.Forest.Forest xmlForest) {
		TreesManager treeTask = (TreesManager)findElementManager(TreeVector.class);
		mesquite.lib.Taxa referencedTaxa = getProject().getTaxa(xmlForest.getTaxa().getInternalName());
		TreeVector mesTreevector = treeTask.makeNewTreeBlock(referencedTaxa, xmlForest.getInternalName(), file);
		org.biophylo.Containable[] xmlTree = xmlForest.getEntities();
		for ( int i = 0; i < xmlTree.length; i++ ) {
			MesquiteTree mesTree = new MesquiteTree(referencedTaxa);
			mesTreevector.addElement(mesTree, false);
			mesTree.setName(xmlTree[i].getInternalName());			
			org.biophylo.Forest.Node xmlRoot = ((org.biophylo.Forest.Tree)xmlTree[i]).getRoot();
			buildTree(xmlRoot,xmlRoot.getChildren(),mesTree.getRoot(),mesTree);
		}
		mesTreevector.addToFile(file, getProject(), treeTask);
	}
	
	private void buildTree(org.biophylo.Forest.Node xmlRoot, org.biophylo.Forest.Node[] xmlChildren, int mesRoot, MesquiteTree mesTree) {
		org.biophylo.Taxa.Taxon xmlTaxon = xmlRoot.getTaxon();
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
		java.util.Vector xmlBlocks = new java.util.Vector();
		ListableVector mesTaxas = getProject().getTaxas();
		writeTaxaBlocks(xmlBlocks,mesTaxas);
		writeCharacterBlocks(xmlBlocks,getProject().getCharacterMatrices());
		for ( int i = 0; i < mesTaxas.size(); i++ ) {
			Listable[] treeVectors = getProject().getCompatibleFileElements(TreeVector.class, mesTaxas.elementAt(i));
			writeTreeBlocks(xmlBlocks,treeVectors);
		}
		StringBuffer outputBuffer = new StringBuffer();	
		outputBuffer.append(((org.biophylo.Util.XMLWritable)xmlBlocks.get(0)).getRootOpenTag());
		for ( int i = 0; i < xmlBlocks.size(); i++ ) {
			try {
				outputBuffer.append(((org.biophylo.Util.XMLWritable)xmlBlocks.get(i)).toXml());
			} catch ( Exception e ) {
				logger.fatal(e.getMessage());
				e.printStackTrace();
			}
		}
		outputBuffer.append(((org.biophylo.Util.XMLWritable)xmlBlocks.get(0)).getRootCloseTag());
		saveExportedFileWithExtension(outputBuffer, arguments, "xml");
		return true;
	}
	
	private org.biophylo.Matrices.Datatype.Datatype makeTypeObject(String dataType) {
		org.biophylo.Matrices.Datatype.Datatype to = null;			
		if ( dataType.equalsIgnoreCase(DNAData.DATATYPENAME) ) {
			to = org.biophylo.Matrices.Datatype.Datatype.getInstance("Dna");		
		}
		else if ( dataType.equalsIgnoreCase(RNAData.DATATYPENAME) ) {
			to = org.biophylo.Matrices.Datatype.Datatype.getInstance("Rna");    			
		}    		
		else if ( dataType.equalsIgnoreCase(ProteinData.DATATYPENAME) ) {
			to = org.biophylo.Matrices.Datatype.Datatype.getInstance("Protein");     			
		}
		else if ( dataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
			to = org.biophylo.Matrices.Datatype.Datatype.getInstance("Continuous");      			
		}
		else if ( dataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {
			to = org.biophylo.Matrices.Datatype.Datatype.getInstance("Standard");     			
		}
		if ( to == null ) {
			logger.fatal("No data type object for " + dataType);
		}
		return to;
	}
	
	private org.biophylo.Taxa.Taxa findEquivalentTaxa(Taxa mesTaxa,java.util.Vector xmlBlocks) {
		org.biophylo.Taxa.Taxa xmlTaxa = null;
		TAXA: for ( int j = 0; j < xmlBlocks.size(); j++ ) {
			if ( mesTaxa.getUniqueID().equals(((org.biophylo.Taxa.Taxa)xmlBlocks.get(j)).getGeneric("MesquiteUniqueID")) ) {
				xmlTaxa = (org.biophylo.Taxa.Taxa)xmlBlocks.get(j);
				break TAXA;
			}
		}
		return xmlTaxa;
	}
	
	private org.biophylo.Taxa.Taxon findEquivalentTaxon(Taxon mesTaxon,org.biophylo.Taxa.Taxa xmlTaxa) {
		org.biophylo.Taxa.Taxon xmlTaxon = null;
		int mesTaxonIndex = mesTaxon.getNumber();
		org.biophylo.Containable[] conts = xmlTaxa.getEntities();
		TAXON: for ( int i = 0; i < conts.length; i++ ) {
			if ( mesTaxonIndex == ((Integer)conts[i].getGeneric("MesquiteUniqueID")).intValue() ) {
				xmlTaxon = (org.biophylo.Taxa.Taxon)conts[i];
				break TAXON;
			}
		}		
		return xmlTaxon;
	}
		
	private void writeCharacterBlocks(java.util.Vector xmlBlocks,ListableVector mesCharacters) {
		logger.debug("Going to write characters");
		for ( int i = 0; i < mesCharacters.size(); i++ ) {
			CharacterData mesData = (CharacterData)mesCharacters.elementAt(i);			
			String dataType = mesData.getDataTypeName();
			org.biophylo.Matrices.Datatype.Datatype to = makeTypeObject(dataType);			
    		org.biophylo.Matrices.Matrix xmlMatrix = new org.biophylo.Matrices.Matrix();
    		xmlMatrix.setTypeObject(to);
    		Taxa mesTaxa = mesData.getTaxa();
    		org.biophylo.Taxa.Taxa xmlTaxa = findEquivalentTaxa(mesTaxa,xmlBlocks);
    		xmlMatrix.setTaxa(xmlTaxa);
    		int nchar = mesData.getNumChars();
    		for ( int j = 0; j < mesData.getNumTaxa(); j++ ) {
    			CharacterState[] mesChars = mesData.getCharacterStateArray(j, 0, nchar);
    			org.biophylo.Matrices.Datum xmlDatum = new org.biophylo.Matrices.Datum();
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
    		xmlBlocks.add(xmlMatrix);    		
		}
	}
	
	private void writeTreeBlocks(java.util.Vector xmlBlocks,Listable[] treeVectors) {
		logger.debug("Going to write trees");
		for ( int i = 0; i < treeVectors.length; i++ ) {	
			logger.debug("Writing tree block " + i);
			TreeVector mesTrees = (TreeVector)treeVectors[i];
			Taxa mesTaxa = mesTrees.getTaxa();
			org.biophylo.Taxa.Taxa xmlTaxa = findEquivalentTaxa(mesTaxa,xmlBlocks);
			org.biophylo.Forest.Forest xmlForest = new org.biophylo.Forest.Forest();
			xmlForest.setTaxa(xmlTaxa);
			xmlForest.setName(mesTrees.getName());
			int ntrees = mesTrees.getNumberOfTrees();
			for ( int j = 0; j < ntrees; j++ ) {
				logger.debug("Writing tree " + j);
				Tree mesTree = mesTrees.getTree(j);
				org.biophylo.Forest.Tree xmlTree = new org.biophylo.Forest.Tree();
				org.biophylo.Forest.Node xmlRoot = new org.biophylo.Forest.Node();
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
			xmlBlocks.add(xmlForest);
		}		
	}
	
	private void buildXmlTree(Tree mesTree,org.biophylo.Forest.Tree xmlTree,int mesNode,org.biophylo.Forest.Node xmlNode,org.biophylo.Forest.Node xmlParentNode,org.biophylo.Taxa.Taxa xmlTaxa) throws org.biophylo.Util.Exceptions.ObjectMismatch {
		xmlNode.setBranchLength(mesTree.getBranchLength(mesNode));
		xmlNode.setName(mesTree.getNodeLabel(mesNode));
		xmlTree.insert(xmlNode);
		if ( mesTree.nodeIsTerminal(mesNode) ) {
			int[] mesTaxonNumber = mesTree.getTerminalTaxa(mesNode);
			Taxa mesTaxa = mesTree.getTaxa();
			Taxon mesTaxon = mesTaxa.getTaxon(mesTaxonNumber[0]);
			org.biophylo.Taxa.Taxon xmlTaxon = findEquivalentTaxon(mesTaxon,xmlTaxa);
			xmlNode.setTaxon(xmlTaxon);		
		}
		xmlNode.setParent(xmlParentNode);
		if ( xmlParentNode != null ) {
			xmlParentNode.setChild(xmlNode);			
		}		
		for (int d = mesTree.firstDaughterOfNode(mesNode); mesTree.nodeExists(d); d = mesTree.nextSisterOfNode(d)) {
			org.biophylo.Forest.Node xmlChild = new org.biophylo.Forest.Node();
			buildXmlTree(mesTree,xmlTree,d,xmlChild,xmlNode,xmlTaxa);
		}
	}
	
	
	private void writeTaxaBlocks(java.util.Vector xmlBlocks,ListableVector mesTaxas) {
		for ( int i = 0; i < mesTaxas.size(); i++ ) {
			org.biophylo.Taxa.Taxa xmlTaxa = new org.biophylo.Taxa.Taxa();
			Taxa mesTaxa = (Taxa)mesTaxas.elementAt(i);
			xmlTaxa.setName(mesTaxa.getName());
			xmlTaxa.setGeneric("MesquiteUniqueID",mesTaxa.getUniqueID());
			for ( int j = 0; j < mesTaxa.getNumTaxa(); j++ ) {
				org.biophylo.Taxa.Taxon xmlTaxon = new org.biophylo.Taxa.Taxon();
				xmlTaxon.setName(mesTaxa.getTaxonName(j));
				xmlTaxon.setGeneric("MesquiteUniqueID",new Integer(j));
				try {
					xmlTaxa.insert(xmlTaxon);
				} catch ( Exception e ) {
					logln(e.getMessage());
				}
			}			
			xmlBlocks.add(xmlTaxa);
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
