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
package mesquite.io.InterpretNEXML;
/*~~  */

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

import org.nexml.*;

/** A file interpreter for a NEXML file format.  */
public class InterpretNEXML extends FileInterpreterI {	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandrec, boolean hiredByName) {
		return true;
	}
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
			CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			TreesManager treesTask = (TreesManager)findElementManager(TreeVector.class);
			// XXX
			try {
				NexmlParser parser = new NexmlParser();
				ElementHandler handler = parser.getHandler();
				FactoryManager manager = new FactoryManager(project,file);
				ObjectFactory taxaFactory = manager.getTaxaFactory(taxaTask);
				ObjectFactory charFactory = manager.getCharactersFactory(charTask);
				ObjectFactory treesFactory = manager.getTreesFactory(treesTask);
				handler.setFactoryForElementNames(taxaFactory.getElementsToHandle(), taxaFactory);
				handler.setFactoryForElementNames(charFactory.getElementsToHandle(), charFactory);
				handler.setFactoryForElementNames(treesFactory.getElementsToHandle(), treesFactory);
				handler.setObjectListener(manager.getMesquiteObjectListener());			
				parser.parseFile(file.getPath());
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
			
			finishImport(progIndicator, file, abort);
		}
		decrementMenuResetSuppression();
	}
	/* ============================  exporting ============================*/
	boolean compact = false;
	/*.................................................................................................................*/
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export NEXML Options", buttonPressed);

		Checkbox compactCheckBox = exportDialog.addCheckBox("compact representation", compact);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		compact = compactCheckBox.getState();
		exportDialog.dispose();
		return ok;
	}
	/*.................................................................................................................*/
	public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here
		return (ExporterDialog.query(this,containerOfModule(), "Export NEXML Options")==0);
	}
	/*.................................................................................................................*/	
	protected String getSupplementForTaxon(Taxa taxa, int it){
		return "";
	}
	/*.................................................................................................................*/
	public void exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		// TODO xml writing goes here?
	}
	/*.................................................................................................................*/
	public String getName() {
		return "NEXML file (nexus xml)";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Imports and exports NEXML files." ;
	}
	/*.................................................................................................................*/

}

class FactoryManager {
	MesquiteProject project;
	MesquiteFile file;
	FactoryManager(MesquiteProject myProject, MesquiteFile myFile) {
		this.project = myProject;
		this.file = myFile;
	}
	
	public CharactersFactory getCharactersFactory (CharactersManager charTask) {
		return new CharactersFactory(this.project, this.file, charTask);
	}
	
	public TaxaFactory getTaxaFactory (TaxaManager taxaTask) {
		return new TaxaFactory(this.project, this.file, taxaTask);
	}
	
	public TreesFactory getTreesFactory(TreesManager treesTask) {
		return new TreesFactory(this.project, this.file, treesTask);
	}
	
	public MesquiteObjectListener getMesquiteObjectListener () {
		return new MesquiteObjectListener(this.project);
	}

	class MesquiteObjectListener implements ObjectListener {
		MesquiteProject mp;
		MesquiteObjectListener(MesquiteProject mymp) {
			this.mp = mymp;
		}
		public void newObjectNotification (Object obj) {
			if ( obj instanceof DefaultObject ) {
				DefaultObject dobj = (DefaultObject) obj;
				System.out.println("received object: " + dobj + " (from element: " + dobj.getLocalName() + ")" );
			}
			else {
				System.out.println("received object: " + obj);
			}
		}		
	}

}