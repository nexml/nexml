package mesquite.nexml.InterpretNEXML;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import mesquite.categ.lib.CategoricalData; //CategoricalData.DATATYPENAME
import mesquite.categ.lib.CategoricalState; //CharacterState mesCS = new CategoricalState();
import mesquite.categ.lib.DNAData; //DNAData.DATATYPENAME 
import mesquite.categ.lib.ProteinData; //ProteinData.DATATYPENAME
import mesquite.categ.lib.RNAData; //RNAData.DATATYPENAME
import mesquite.cont.lib.ContinuousData; //ContinuousData.DATATYPENAME // CharacterData mesMatrix instanceof ContinuousData
import mesquite.cont.lib.ContinuousState; //mesCS = new ContinuousState(xmlDouble) //(ContinuousState)mesCS
import mesquite.lib.Associable;
import mesquite.lib.Bits;
import mesquite.lib.DoubleArray;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.LongArray;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTree;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.ObjectArray;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.Tree;
import mesquite.lib.TreeVector;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.duties.TreesManager;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;
import org.nexml.model.CategoricalMatrix;
import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.ContinuousMatrix;
import org.nexml.model.Document;
import org.nexml.model.DocumentFactory;
import org.nexml.model.Edge;
import org.nexml.model.FloatEdge;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.MolecularMatrix;
import org.nexml.model.Network;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;

public class ObjectConverter extends MesquiteModule {
	private static URI msqURI = URI.create("http://mesquiteproject.org#");
	private String msqPrefix = "msq";
	private String msqTaxaUID  = msqPrefix + ":taxaUID";
	private String msqTaxonUID = msqPrefix + ":taxonUID";
	private String msqTreeUID  = msqPrefix + ":treeUID";
	private String msqTreePolytomyAssumption = msqPrefix + ":treePolytomyAssumption";
	private EmployerEmployee mEmployerEmployee;
	private static Properties mPredicateHandlerMapping = new Properties();
	private static boolean debugging = true;
	
	private static void debug(String s) {
		if (debugging)
			mesquite.lib.MesquiteMessage.notifyProgrammer(s);
	}
	
	/**
	 * 
	 * @param mesProject
	 * @return
	 */
	public Document createDocumentFromProject(MesquiteProject mesProject) {
		ListableVector mesTaxas = mesProject.getTaxas();
		Document xmlProject = writeProject(mesProject);
		try {
			writeTaxaBlocks(xmlProject,mesTaxas);
			writeCharacterBlocks(xmlProject,mesProject.getCharacterMatrices());			
			for ( int i = 0; i < mesTaxas.size(); i++ ) {
				Listable[] treeVectors = mesProject.getCompatibleFileElements(TreeVector.class, mesTaxas.elementAt(i));
				writeTreeBlocks(xmlProject,treeVectors);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return xmlProject;
	}
	
	/**
	 * 
	 * @param mesProject
	 * @param predicateHandlerMapping
	 * @return
	 */
	public Document createDocumentFromProject(MesquiteProject mesProject,Properties predicateHandlerMapping) {
		mPredicateHandlerMapping = predicateHandlerMapping;
		return createDocumentFromProject(mesProject);
	}
	
	/**
	 * 
	 * @param mesTaxa
	 * @param xmlProject
	 * @return
	 */
	private OTUs findEquivalentTaxa(Taxa mesTaxa,Document xmlProject) {
		for ( OTUs xmlTaxa : xmlProject.getOTUsList() ) {
			Set<Object> msqUIDs = xmlTaxa.getAnnotationValues(msqTaxaUID);
			if ( msqUIDs.contains(mesTaxa.getUniqueID()) ) {
				return xmlTaxa;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param mesTaxon
	 * @param xmlTaxa
	 * @return
	 */
	private OTU findEquivalentTaxon(Taxon mesTaxon,OTUs xmlTaxa) {
		Integer mesTaxonIndex = mesTaxon.getNumber();
		for ( OTU xmlTaxon : xmlTaxa.getAllOTUs() ) {
			Set<Object> msqUIDs = xmlTaxon.getAnnotationValues(msqTaxonUID);
			if ( msqUIDs.contains(mesTaxonIndex) ) {
				return xmlTaxon;
			}
		}		
		return null;
	}
	
	/**
	 * 
	 * @param mesTree
	 * @param xmlTree
	 * @param mesNode
	 * @param xmlRoot
	 * @param xmlParentNode
	 * @param xmlTaxa
	 */
	private void writeTree(Tree mesTree,org.nexml.model.Tree<FloatEdge> xmlTree,int mesNode,Node xmlRoot,Node xmlParentNode,OTUs xmlTaxa) {
		xmlRoot.setLabel(mesTree.getNodeLabel(mesNode));
		readAnnotations((MesquiteTree)mesTree,xmlRoot,mesNode,mesTree);
		if ( mesTree.nodeIsTerminal(mesNode) ) {
			int[] mesTaxonNumber = mesTree.getTerminalTaxa(mesNode);
			Taxa mesTaxa = mesTree.getTaxa();
			Taxon mesTaxon = mesTaxa.getTaxon(mesTaxonNumber[0]);
			OTU xmlTaxon = findEquivalentTaxon(mesTaxon,xmlTaxa);
			xmlRoot.setOTU(xmlTaxon);		
		}
		if ( xmlParentNode != null ) {
			FloatEdge edge = xmlTree.createEdge(xmlParentNode,xmlRoot);
			edge.setLength(mesTree.getBranchLength(mesNode));
		}		
		for (int d = mesTree.firstDaughterOfNode(mesNode); mesTree.nodeExists(d); d = mesTree.nextSisterOfNode(d)) {
			Node xmlChild = xmlTree.createNode();
			writeTree(mesTree,xmlTree,d,xmlChild,xmlRoot,xmlTaxa);			
		}
	}	

	/**
	 * 
	 * @param xmlProject
	 * @param treeVectors
	 */
	private void writeTreeBlocks(Document xmlProject,Listable[] treeVectors) {
		for ( int i = 0; i < treeVectors.length; i++ ) {	
			TreeVector mesTrees = (TreeVector)treeVectors[i];
			OTUs xmlTaxa = findEquivalentTaxa(mesTrees.getTaxa(),xmlProject);
			TreeBlock xmlForest = xmlProject.createTreeBlock(xmlTaxa);
			xmlForest.setLabel(mesTrees.getName());
			int ntrees = mesTrees.getNumberOfTrees();
			for ( int j = 0; j < ntrees; j++ ) {
				Tree mesTree = mesTrees.getTree(j);
				org.nexml.model.Tree<FloatEdge> xmlTree = xmlForest.createFloatTree();		
				xmlTree.addAnnotationValue(msqTreeUID,msqURI, new Long(mesTree.getID()));
				xmlTree.addAnnotationValue(msqTreePolytomyAssumption,msqURI, new Integer(mesTree.getPolytomiesAssumption()));
				Node xmlRoot = xmlTree.createNode();
				xmlTree.setLabel(mesTree.getName());
				xmlRoot.setRoot(mesTree.getRooted());
				int mesRoot = mesTree.getRoot();
				try {
					writeTree(mesTree,xmlTree,mesRoot,xmlRoot,null,xmlTaxa);
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				writeAnnotations(mesTrees,xmlTree,j,mesTree);
			}
		}		
	}	
	
	/**
	 * 
	 * @param nr
	 * @param annotatable
	 * @param value
	 * @param subject
	 */
	private void writeAnnotation(NameReference nr,Annotatable annotatable,Object value,Object subject) {
		String handlerClassName = getPredicateHandlerMapping().getProperty(nr.getName());
		PredicateHandler ph = null;
		if ( handlerClassName != null ) {
			try {
				Class<?> handlerClass = Class.forName(handlerClassName);
				Constructor<?> declaredConstructor = handlerClass.getDeclaredConstructor(Object.class,String.class,Object.class);
				ph = (PredicateHandler) declaredConstructor.newInstance(null,nr.getName(),value);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			ph = new PredicateHandlerImpl(null,nr.getName(),value);
		}	
		annotatable.addAnnotationValue(ph.getProperty(),ph.getURI(),ph.getValue());
	}
	
	/**
	 * 
	 * @param associable
	 * @param annotatable
	 * @param segmentCount
	 * @param subject
	 */
	private void writeAnnotations(Associable associable, Annotatable annotatable, int segmentCount,Object subject) {		
		int numDoubs = associable.getNumberAssociatedDoubles();
		for ( int i = 0; i < numDoubs; i++ ){  
			DoubleArray array = associable.getAssociatedDoubles(i);
			writeAnnotation(array.getNameReference(),annotatable,array.getValue(segmentCount),subject);			
		}	
		
		int numLongs = associable.getNumberAssociatedLongs();
		for ( int i = 0; i < numLongs; i++ ){  
			LongArray array = associable.getAssociatedLongs(i);
			writeAnnotation(array.getNameReference(),annotatable,array.getValue(segmentCount),subject);
		}
		
		int numBits = associable.getNumberAssociatedBits();
		for ( int i = 0; i < numBits; i++ ){  
			Bits array = associable.getAssociatedBits(i);
			writeAnnotation(array.getNameReference(),annotatable,array,subject);
		}	
		
		int numObjs = associable.getNumberAssociatedObjects();
		for ( int i = 0; i < numObjs; i++ ){  
			ObjectArray array = associable.getAssociatedObjects(i);
			writeAnnotation(array.getNameReference(),annotatable,array.getValue(segmentCount),subject);
		}		
		
	}
	
	/**
	 * 
	 * @param xmlProject
	 * @param mesCharacters
	 */
	@SuppressWarnings("unchecked")
	private void writeCharacterBlocks(Document xmlProject,ListableVector mesCharacters) {
		debug("Going to write character blocks");
		for ( int i = 0; i < mesCharacters.size(); i++ ) {
			debug("Writing character block "+i);
			CharacterData mesData = (CharacterData)mesCharacters.elementAt(i);
    		Taxa mesTaxa = mesData.getTaxa();
    		OTUs xmlTaxa = findEquivalentTaxa(mesTaxa,xmlProject);			
    		org.nexml.model.Matrix<?> xmlMatrix = null;		
    		CharacterStateSet xmlCharacterStateSet = null;
			String mesDataType = mesData.getDataTypeName();
			debug("Data type is "+mesDataType);
			boolean xmlIsMolecularMatrix = false;
			String xmlMolecularMatrixSubtype = null;
			if ( mesDataType.equalsIgnoreCase(DNAData.DATATYPENAME) ) {
				xmlMatrix = xmlProject.createMolecularMatrix(xmlTaxa,MolecularMatrix.DNA);
				xmlCharacterStateSet = ((MolecularMatrix)xmlMatrix).getDNACharacterStateSet();
				xmlIsMolecularMatrix = true;
				xmlMolecularMatrixSubtype = MolecularMatrix.DNA;
			}
			else if ( mesDataType.equalsIgnoreCase(RNAData.DATATYPENAME) ) {
				xmlMatrix = xmlProject.createMolecularMatrix(xmlTaxa,MolecularMatrix.RNA);	
				xmlCharacterStateSet = ((MolecularMatrix)xmlMatrix).getRNACharacterStateSet();	
				xmlIsMolecularMatrix = true;
				xmlMolecularMatrixSubtype = MolecularMatrix.RNA;
			}    		
			else if ( mesDataType.equalsIgnoreCase(ProteinData.DATATYPENAME) ) {
				xmlMatrix = xmlProject.createMolecularMatrix(xmlTaxa,MolecularMatrix.Protein); 		
				xmlCharacterStateSet = ((MolecularMatrix)xmlMatrix).getProteinCharacterStateSet();	
				xmlIsMolecularMatrix = true;
				xmlMolecularMatrixSubtype = MolecularMatrix.Protein;
			}
			else if ( mesDataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
				xmlMatrix = xmlProject.createContinuousMatrix(xmlTaxa);      			
			}
			else if ( mesDataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {
				xmlMatrix = xmlProject.createCategoricalMatrix(xmlTaxa);   
				xmlCharacterStateSet = ((CategoricalMatrix)xmlMatrix).createCharacterStateSet();
			}
			else {
				debug("Can't write data type "+mesDataType);
			}
    		int mesNchar = mesData.getNumChars();
    		List<Character> xmlCharacters = new ArrayList<Character>(mesNchar);
    		for ( int j = 0; j < mesNchar; j++ ) {
    			Character xmlChar = xmlMatrix.createCharacter(xmlCharacterStateSet);
    			xmlCharacters.add(xmlChar);
    		}
    		
    		for ( int j = 0; j < mesData.getNumTaxa(); j++ ) {
    			debug("Writing states for taxon "+j);
    			CharacterState[] mesChars = mesData.getCharacterStateArray(j, 0, mesNchar);
    			Taxon mesTaxon = mesData.getTaxa().getTaxon(j);
    			OTU xmlTaxon = findEquivalentTaxon(mesTaxon,xmlTaxa);    			
    			for ( int k = 0; k < mesNchar; k++ ) {
    				debug("Writing state "+k+" for taxon "+j);
    				Character xmlChar = xmlCharacters.get(k);
    				String mesCharString = mesChars[k].toDisplayString();
    				if ( mesCharString != null && !mesCharString.equals("-") ) {    					
    					if ( mesDataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
    						MatrixCell<Double> xmlCell = (MatrixCell<Double>) xmlMatrix.getCell(xmlTaxon,xmlChar);
    						xmlCell.setValue((Double)xmlMatrix.parseSymbol(mesCharString));
    					}
    					else if ( mesDataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {    						
    						MatrixCell<org.nexml.model.CharacterState> xmlCell = (MatrixCell<org.nexml.model.CharacterState>) xmlMatrix.getCell(xmlTaxon,xmlChar);
    						xmlCell.setValue((org.nexml.model.CharacterState)xmlMatrix.parseSymbol(mesCharString));
    					}
    					else if ( xmlIsMolecularMatrix ) {
    						MatrixCell<org.nexml.model.CharacterState> xmlCell = (MatrixCell<org.nexml.model.CharacterState>) xmlMatrix.getCell(xmlTaxon,xmlChar);
    						xmlCell.setValue((org.nexml.model.CharacterState)((MolecularMatrix)xmlMatrix).parseSymbol(mesCharString,xmlMolecularMatrixSubtype));
    					}    					
    				}
    			}    			
    		}
		}
	}	
	
	/**
	 * 
	 * @param xmlProject
	 * @param mesTaxas
	 */
	private void writeTaxaBlocks(Document xmlProject,ListableVector mesTaxas) {
		for ( int i = 0; i < mesTaxas.size(); i++ ) {
			OTUs otus = xmlProject.createOTUs();
			Taxa mesTaxa = (Taxa)mesTaxas.elementAt(i);
			otus.setLabel(mesTaxa.getName());
			String mesTaxaUID = mesTaxa.getUniqueID();
			if ( mesTaxaUID == null ) {
				mesTaxaUID = MesquiteTrunk.getUniqueIDBase() + Taxa.totalCreated;
				mesTaxa.setUniqueID(mesTaxaUID);
			}
			otus.addAnnotationValue(msqTaxaUID,msqURI, mesTaxaUID);
			for ( int j = 0; j < mesTaxa.getNumTaxa(); j++ ) {			
				OTU otu = otus.createOTU();
				writeAnnotations(mesTaxa,otu,j,mesTaxa.getTaxon(j));
				otu.setLabel(mesTaxa.getTaxonName(j));				
				otu.addAnnotationValue(msqTaxonUID,msqURI,new Integer(j));
				writeAnnotations(mesTaxas,otu,j,mesTaxa.getTaxon(j));				
			}
		}		
	}	
	
	/**
	 * 
	 * @param mesProject
	 * @return
	 */
	private Document writeProject(MesquiteProject mesProject) {
		Document xmlProject = DocumentFactory.safeCreateDocument();		
		return xmlProject;
	}
	
	/**
	 * 
	 * @param employerEmployee
	 */
	public ObjectConverter (EmployerEmployee employerEmployee) { 
		mEmployerEmployee = employerEmployee;
	}
	
	/**
	 * 
	 * @param xmlDocument
	 * @param project
	 * @return
	 */
	public MesquiteProject fillProjectFromNexml(Document xmlDocument,MesquiteProject project) {
		//MesquiteProject pr = mEmployerEmployee.getProject();
		List<OTUs> xmlOTUsList = xmlDocument.getOTUsList();		
		List<TreeBlock> xmlTreeBlockList = xmlDocument.getTreeBlockList();
		List<Matrix<?>> xmlMatrices = xmlDocument.getMatrices();
		for ( OTUs xmlOTUs : xmlOTUsList ) {
			Taxa mesTaxa = readTaxaBlocks(project,project.getFile(0),xmlOTUs);
			for ( TreeBlock xmlTreeBlock : xmlTreeBlockList ) {
				readTreeBlocks(project,project.getFile(0),xmlTreeBlock,mesTaxa);
			}	
			for ( Matrix<?> xmlMatrix : xmlMatrices ) {
				readCharacterBlocks(project,project.getFile(0),xmlMatrix,mesTaxa);
			}			
		}
		return project;
	}
	
	/**
	 * 
	 * @param xmlDocument
	 * @param mesProject
	 * @param predicateHandlerMapping
	 * @return
	 */
	public MesquiteProject fillProjectFromNexml(Document xmlDocument,MesquiteProject mesProject, Properties predicateHandlerMapping) {
		setPredicateHandlerMapping(predicateHandlerMapping);
		return fillProjectFromNexml(xmlDocument,mesProject);		
	}
	
	/**
	 * 
	 * @param mesProject
	 * @param mesFile
	 * @param xmlMatrix
	 * @param mesTaxa
	 */
	private void readCharacterBlocks (MesquiteProject mesProject, MesquiteFile mesFile,Matrix<?> xmlMatrix,Taxa mesTaxa) {		
		String mesDataType = null;
		debug("Going to read characters element "+xmlMatrix.getId());
		if ( xmlMatrix instanceof ContinuousMatrix ) {
			mesDataType = ContinuousData.DATATYPENAME;
		}
		else if ( xmlMatrix instanceof CategoricalMatrix ) {
			mesDataType = CategoricalData.DATATYPENAME;
		}
		else if ( xmlMatrix instanceof MolecularMatrix ) {
			mesDataType = DNAData.DATATYPENAME;			
		}		
		if ( mesDataType != null ) {
			debug("Going to read matrix of type "+mesDataType);
			readMatrix(mesDataType,xmlMatrix,mesFile,mesTaxa);
		}
		else {
			debug("Can't process datatype "+xmlMatrix.getClass().getSimpleName());
		}
	}
	
	/**
	 * 
	 * @param mesDataType
	 * @param xmlMatrix
	 * @param mesFile
	 * @param mesTaxa
	 */
	private void readMatrix(String mesDataType,Matrix<?> xmlMatrix,MesquiteFile mesFile,Taxa mesTaxa) {
		CharactersManager charTask = (CharactersManager)mEmployerEmployee.findElementManager(CharacterData.class);
		mesquite.lib.characters.CharacterData mesMatrix = charTask.newCharacterData(mesTaxa, 0, mesDataType);
		OTUs xmlOTUs = xmlMatrix.getOTUs();
		List<OTU> xmlOTUList = xmlOTUs.getAllOTUs();
		List<Character> xmlCharacterList = xmlMatrix.getCharacters();		
		for ( OTU xmlOTU : xmlOTUList ) {
			String xmlOTUId = xmlOTU.getId();
			debug("Reading characters for OTU "+xmlOTUId);
			int mesTaxon = mesTaxa.findByUniqueID(xmlOTUId);			
			int mesCharacter = 0 ;
			for ( Character xmlCharacter : xmlCharacterList ) {
				CharacterState mesCS = null;
				MatrixCell<?> xmlCell = xmlMatrix.getCell(xmlOTU, xmlCharacter);
        		if ( mesMatrix instanceof ContinuousData ) {       
        			Double xmlDouble = (Double)xmlCell.getValue();
        			if ( xmlDouble != null ) {
        				mesCS = new ContinuousState(xmlDouble);
        				((ContinuousState)mesCS).setNumItems(1); // XXX for multidimensional matrices
        			}
        		}
        		else {        			
        			org.nexml.model.CharacterState xmlState = (org.nexml.model.CharacterState)xmlCell.getValue(); 
        			if ( xmlState != null ) {
        				mesCS = new CategoricalState();
        				String xmlSymbol = xmlState.getSymbol().toString();
        				mesCS.setValue(xmlSymbol, mesMatrix);
        			}
        		}
				if (mesMatrix.getNumChars() <= mesCharacter) {
					mesMatrix.addCharacters(mesMatrix.getNumChars()-1, 1, false);   // add a character if needed
        		}
				if ( mesCS != null ) {
					mesMatrix.setState(mesCharacter, mesTaxon, mesCS);
					//can add in character state stuff here
				}

				mesCharacter++;
			}
		}
		
		debug("Going to read character names");
		int ic = 0;
		for ( Character xmlCharacter : xmlCharacterList ) {
			String name = xmlCharacter.getLabel();
			if ( null != name && ! name.equals("")  ) {
				mesMatrix.setCharacterName(ic, name);
			}
			ic++;
		}

		mesMatrix.setUniqueID(xmlMatrix.getId());
		mesMatrix.setName(xmlMatrix.getLabel());
		mesMatrix.addToFile(mesFile, mesFile.getProject(), charTask);		
	}	
	
	/**
	 * 
	 * @param project
	 * @param file
	 * @param xmlOTUs
	 * @return
	 */
	private Taxa readTaxaBlocks (MesquiteProject project, MesquiteFile file, OTUs xmlOTUs) {
		TaxaManager taxaTask = (TaxaManager)mEmployerEmployee.findElementManager(mesquite.lib.Taxa.class);
		int xmlOTUListSize = xmlOTUs.getAllOTUs().size();
        mesquite.lib.Taxa mesTaxa = taxaTask.makeNewTaxa(project.getTaxas().getUniqueName(xmlOTUs.getId()), xmlOTUListSize, false);
        mesTaxa.setName(xmlOTUs.getLabel());
        mesTaxa.addToFile(file, project, taxaTask);
        int mesTaxonIndex = 0;
        for ( OTU xmlOTU : xmlOTUs.getAllOTUs() ) {
        	Taxon mesTaxon = mesTaxa.getTaxon(mesTaxonIndex);
        	mesTaxon.setUniqueID(xmlOTU.getId());
        	mesTaxon.setName(xmlOTU.getLabel());
        	readAnnotations(mesTaxa,xmlOTU,mesTaxonIndex,mesTaxon);
        	mesTaxonIndex++;
        }	
        
        logln("From ObjectConverter's readTaxaBlocks, this.getProject().getHomeDirectoryName(): " + file.getProject().getHomeDirectoryName());
        return mesTaxa;
	}	
	
	/**
	 * 
	 * @param associable
	 * @param annotatable
	 * @param segmentCount
	 * @param subject
	 */
	private void readAnnotations(Associable associable,Annotatable annotatable,int segmentCount,Object subject) {
		Set<Annotation> allAnnotations = annotatable.getAllAnnotations();
		for ( Annotation annotation : allAnnotations ) {
			String property = annotation.getProperty();
			if ( property.equals("") ) {
				property = annotation.getRel();
			}
			String[] curie = property.split(":");
			String localProperty = curie[1]; // NameReference;	lookup in properties		
			Object value = annotation.getValue();
			PredicateHandler ph = getPredicateHandler(subject,localProperty,value);
			NameReference mesNr = null;
			Object convertedValue = ph.getValue();
			if ( convertedValue instanceof Boolean ) {
				mesNr = associable.makeAssociatedBits(ph.getPredicate());
				associable.setAssociatedBit(mesNr,segmentCount,(Boolean)convertedValue);
			}
			else if ( value instanceof Double ) {
				mesNr = associable.makeAssociatedDoubles(ph.getPredicate());
				associable.setAssociatedDouble(mesNr,segmentCount,(Double)convertedValue);
			}
			else if ( value instanceof Long ) {
				mesNr = associable.makeAssociatedLongs(ph.getPredicate());
				associable.setAssociatedLong(mesNr,segmentCount,(Long)convertedValue);					
			}	
			else if ( value instanceof Object ) {
				mesNr = associable.makeAssociatedObjects(ph.getPredicate());
				associable.setAssociatedObject(mesNr,segmentCount,convertedValue);
			}			
		}
		
	}	
	
	/**
	 * 
	 * @param mesProject
	 * @param mesFile
	 * @param xmlTreeBlock
	 * @param referencedTaxa
	 */
	private void readTreeBlocks (MesquiteProject mesProject, MesquiteFile mesFile, TreeBlock xmlTreeBlock, Taxa referencedTaxa) {
		TreesManager mesTreeTask = (TreesManager)mEmployerEmployee.findElementManager(TreeVector.class);
		TreeVector mesTreeVector = mesTreeTask.makeNewTreeBlock(referencedTaxa, xmlTreeBlock.getLabel(), mesFile);
		int mesTreeCount = 0;
		for (Network<?> xmlNetwork : xmlTreeBlock) {
			MesquiteTree mesTree = new MesquiteTree(referencedTaxa);
			mesTreeVector.addElement(mesTree, false);
			mesTree.setName(xmlNetwork.getLabel());	
			Set<Object> xmlAnnotationValues = xmlNetwork.getAnnotationValues(msqTreePolytomyAssumption);
			if ( ! xmlAnnotationValues.isEmpty() ) {
				Object mesPolytomyAssumption = xmlAnnotationValues.iterator().next();
				if ( mesPolytomyAssumption instanceof BigInteger ) {
					mesTree.setPolytomiesAssumption(((BigInteger)mesPolytomyAssumption).intValue(),false);
				}
			}
			readAnnotations(mesTreeVector,xmlNetwork,mesTreeCount,mesTree);
			Set<Node> xmlNodeSet = xmlNetwork.getNodes();
			Node xmlRoot = null;
			if ( xmlNetwork instanceof org.nexml.model.Tree ) {
				xmlRoot = ((org.nexml.model.Tree<?>)xmlNetwork).getRoot();
			}
			FINDROOT: for ( Node xmlNode : xmlNodeSet ) {
				Set<Node> xmlInNodes = xmlNetwork.getInNodes(xmlNode);
				if ( xmlInNodes.size() == 0 ) {
					xmlRoot = xmlNode;
					break FINDROOT;
				}
			}
			readTree(xmlNetwork, xmlRoot, xmlNetwork.getOutNodes(xmlRoot), mesTree.getRoot(), mesTree);
		}
		mesTreeVector.addToFile(mesFile, mesProject, mesTreeTask);
	}
	
	/**
	 * 
	 * @param xmlNetwork
	 * @param xmlRoot
	 * @param xmlChildren
	 * @param mesRoot
	 * @param mesTree
	 */
	private void readTree(Network<?> xmlNetwork, Node xmlRoot, Set<Node> xmlChildren, int mesRoot, MesquiteTree mesTree) {
		OTU xmlOTU = xmlRoot.getOTU();
		if ( xmlOTU != null ) {
			String xmlOTUId = xmlOTU.getId();
			Taxa mesTaxa = mesTree.getTaxa();
			mesTree.setTaxonNumber(mesRoot, mesTaxa.findByUniqueID(xmlOTUId), false);
		}
		mesTree.setNodeLabel(xmlRoot.getLabel(), mesRoot);
		readAnnotations(mesTree,xmlRoot,mesRoot,mesTree);
		for ( Node xmlChild : xmlChildren ) {
			int mesChild = mesTree.sproutDaughter(mesRoot, false);
			Edge edge = xmlNetwork.getEdge(xmlRoot,xmlChild);
			mesTree.setBranchLength(mesChild,edge.getLength().doubleValue(),false);
			readAnnotations(mesTree,edge,mesChild,mesTree);
			readTree(xmlNetwork,xmlChild,xmlNetwork.getOutNodes(xmlChild),mesChild,mesTree);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see mesquite.lib.MesquiteModule#getDutyClass()
	 */
	@Override
	public Class<?> getDutyClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.lib.MesquiteModule#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "NeXML object converter";
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.lib.MesquiteModule#startJob(java.lang.String, java.lang.Object, boolean)
	 */
	@Override
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public static Properties getPredicateHandlerMapping() {
		return mPredicateHandlerMapping;
	}

	/**
	 * 
	 * @param predicateHandlerMapping
	 */
	public void setPredicateHandlerMapping(Properties predicateHandlerMapping) {
		mPredicateHandlerMapping = predicateHandlerMapping;
	}	
	
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param value
	 * @return
	 */
	private PredicateHandler getPredicateHandler(Object subject,String predicate,Object value) {
		String handlerClassName = getPredicateHandlerMapping().getProperty(predicate);
		PredicateHandler ph = null;
		if ( handlerClassName != null ) {
			try {
				Class<?> handlerClass = Class.forName(handlerClassName);
				Constructor<?> declaredConstructor = handlerClass.getDeclaredConstructor(Object.class,String.class,Object.class);
				ph = (PredicateHandler) declaredConstructor.newInstance(subject,predicate,value);	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if ( null == ph ) {
			ph = new PredicateHandlerImpl(subject,predicate,value);
		}
		logln("Using predicateHandler " + ph.toString());
		return ph;
	}

}
