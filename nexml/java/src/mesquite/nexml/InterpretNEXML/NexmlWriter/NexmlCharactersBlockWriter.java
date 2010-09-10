package mesquite.nexml.InterpretNEXML.NexmlWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.RNAData;
import mesquite.cont.lib.ContinuousData;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;

import org.nexml.model.Annotatable;
import org.nexml.model.CategoricalMatrix;
import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.Document;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.MolecularMatrix;
import org.nexml.model.NexmlWritable;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;

public class NexmlCharactersBlockWriter extends NexmlBlockWriter {
	
	@SuppressWarnings("serial")
	private static final Map<String , String> xmlMolecularDataTypeFor = new HashMap<String, String>() {{
		put(DNAData.DATATYPENAME, MolecularMatrix.DNA);
		put(RNAData.DATATYPENAME, MolecularMatrix.RNA);
		put(ProteinData.DATATYPENAME, MolecularMatrix.Protein);
	}};	
	
	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlCharactersBlockWriter(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockWriter#writeBlock(org.nexml.model.Document, mesquite.lib.FileElement)
	 */
	@Override
	protected Annotatable writeBlock(Document xmlProject, FileElement mesBlock) {
		CharacterData mesData = (CharacterData)mesBlock;
		Taxa mesTaxa = mesData.getTaxa();
		OTUs xmlTaxa = findEquivalentTaxa(mesTaxa,xmlProject);			
		org.nexml.model.Matrix<?> xmlMatrix = null;		
		CharacterStateSet xmlCharacterStateSet = null;
		String mesDataType = mesData.getDataTypeName();
		if ( xmlMolecularDataTypeFor.containsKey(mesDataType) ) {
			xmlMatrix = xmlProject.createMolecularMatrix(xmlTaxa,xmlMolecularDataTypeFor.get(mesDataType));
			xmlCharacterStateSet = ((MolecularMatrix)xmlMatrix).getCharacterStateSet();
		}
		else if ( mesDataType.equalsIgnoreCase(CategoricalData.DATATYPENAME) ) {
			xmlMatrix = xmlProject.createCategoricalMatrix(xmlTaxa);   
			xmlCharacterStateSet = ((CategoricalMatrix)xmlMatrix).createCharacterStateSet();
		}
		else if ( mesDataType.equalsIgnoreCase(ContinuousData.DATATYPENAME) ) {
			xmlMatrix = xmlProject.createContinuousMatrix(xmlTaxa);      			
		}			
		else {
			MesquiteMessage.warnProgrammer("Can't write data type "+mesDataType);
		}   		
		writeCharacterStates(mesData, xmlMatrix, xmlCharacterStateSet);
		return xmlMatrix;
	}
	
	/**
	 * 
	 * @param mesData
	 * @param xmlMatrix
	 * @param xmlCharacterStateSet
	 */
	@SuppressWarnings("unchecked")
	private void writeCharacterStates(CharacterData mesData, org.nexml.model.Matrix<?> xmlMatrix, CharacterStateSet xmlCharacterStateSet) {
		String mesDataType = mesData.getDataTypeName();
		int mesNchar = mesData.getNumChars();
		List<Character> xmlCharacters = new ArrayList<Character>(mesNchar);
		for ( int j = 0; j < mesNchar; j++ ) {
			Character xmlChar = xmlMatrix.createCharacter(xmlCharacterStateSet);
			String mesCharacterName = mesData.getCharacterName(j);
			if ( null != mesCharacterName && ! mesCharacterName.equals("") ) {
				xmlChar.setLabel(mesCharacterName);
			}
			xmlCharacters.add(xmlChar);
		}
		for ( int j = 0; j < mesData.getNumTaxa(); j++ ) {
			CharacterState[] mesChars = mesData.getCharacterStateArray(j, 0, mesNchar);
			Taxon mesTaxon = mesData.getTaxa().getTaxon(j);
			OTU xmlTaxon = findEquivalentTaxon(mesTaxon,xmlMatrix.getOTUs());    			
			for ( int k = 0; k < mesNchar; k++ ) {
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
					else if ( xmlMolecularDataTypeFor.containsKey(mesDataType) ) {
						MatrixCell<org.nexml.model.CharacterState> xmlCell = (MatrixCell<org.nexml.model.CharacterState>) xmlMatrix.getCell(xmlTaxon,xmlChar);
						xmlCell.setValue((org.nexml.model.CharacterState)((MolecularMatrix)xmlMatrix).parseSymbol(mesCharString,xmlMolecularDataTypeFor.get(mesDataType)));
					}    					
				}
			}    			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockWriter#getThingInXmlBlock(org.nexml.model.NexmlWritable, int)
	 */
	@Override
	protected Annotatable getThingInXmlBlock(NexmlWritable xmlBlock, int index) {
		Matrix<?> xmlMatrix = (Matrix<?>)xmlBlock;
		return xmlMatrix.getCharacters().get(index);
	}

}
