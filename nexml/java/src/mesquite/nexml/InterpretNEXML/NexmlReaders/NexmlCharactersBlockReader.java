/**
 * 
 */
package mesquite.nexml.InterpretNEXML.NexmlReaders;

import java.util.List;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.Taxa;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.CharactersManager;

import org.nexml.model.Annotatable;
import org.nexml.model.CategoricalMatrix;
import org.nexml.model.Character;
import org.nexml.model.ContinuousMatrix;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.MolecularMatrix;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;

/**
 * @author rvosa
 *
 */
public class NexmlCharactersBlockReader extends NexmlBlockReader {

	/**
	 * @param employerEmployee
	 */
	public NexmlCharactersBlockReader(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockReader#readBlock(mesquite.lib.MesquiteProject, mesquite.lib.MesquiteFile, org.nexml.model.Annotatable, org.nexml.model.OTUs)
	 */
	@Override
	protected FileElement readBlock(MesquiteProject mesProject,
			MesquiteFile mesFile, Annotatable xmlAnnotatable, OTUs xmlOTUs) {
		Matrix<?> xmlMatrix = (Matrix<?>)xmlAnnotatable;
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
			return readMatrix(mesDataType,xmlMatrix,mesFile,findEquivalentTaxa(xmlOTUs, mesProject));
		}
		else {
			debug("Can't process datatype "+xmlMatrix.getClass().getSimpleName());
			return null;
		}
	}
	
	/**
	 * 
	 * @param mesDataType
	 * @param xmlMatrix
	 * @param mesFile
	 * @param mesTaxa
	 */
	private FileElement readMatrix(String mesDataType,Matrix<?> xmlMatrix,MesquiteFile mesFile,Taxa mesTaxa) {
		CharactersManager charTask = (CharactersManager)getEmployerEmployee().findElementManager(CharacterData.class);		
		OTUs xmlOTUs = xmlMatrix.getOTUs();
		List<Character> xmlCharacterList = xmlMatrix.getCharacters();
		CharacterData mesMatrix = charTask.newCharacterData(mesTaxa, xmlCharacterList.size(), mesDataType);
		for ( OTU xmlOTU : xmlOTUs.getAllOTUs() ) {
			String xmlOTUId = xmlOTU.getId();
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
				if ( mesCS != null ) {
					mesMatrix.setState(mesCharacter, mesTaxon, mesCS);
					//can add in character state stuff here
				}
				String name = xmlCharacter.getLabel();
				if ( null != name && ! name.equals("")  ) {
					mesMatrix.setCharacterName(mesCharacter, name);
				}
				mesCharacter++;
			}
		}
		mesMatrix.setUniqueID(xmlMatrix.getId());
		mesMatrix.setName(xmlMatrix.getLabel());		
		return mesMatrix;
	}	

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockReader#getThingInMesquiteBlock(mesquite.lib.FileElement, int)
	 */
	@Override
	protected Listable getThingInMesquiteBlock(FileElement mesBlock, int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
