/**
 * 
 */
package mesquite.nexml.InterpretNEXML;

import java.io.FileInputStream;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.nexml.InterpretNEXML.NexmlReader.NexmlReader;
import mesquite.nexml.InterpretNEXML.NexmlWriter.NexmlWriter;

import org.nexml.model.Document;
import org.nexml.model.DocumentFactory;

public class InterpretNEXML extends FileInterpreterI {

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
	@SuppressWarnings("rawtypes")
	public boolean canExportData(Class dataClass) {  
		return true;
	}
/*.................................................................................................................*/
	public boolean canImport() {  
		 return true;
	}

/*.................................................................................................................*/
	public void readFile(MesquiteProject project, MesquiteFile file, String arguments) {
		FileInputStream fs = null;
		Document xmlDocument = null;
		try {
			fs = new FileInputStream(file.getPath());
			xmlDocument = DocumentFactory.parse(fs);
		} catch ( Exception e ) {
			e.printStackTrace();
		}	
		NexmlReader nr = new NexmlReader(this);		
	    nr.fillProjectFromNexml(xmlDocument,project);			
	}		

/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	// XXX make compact/verbose switch
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		return true;
	}	

	
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) {
		MesquiteProject mesProject = getProject();
		NexmlWriter nw = new NexmlWriter(this);			    		
		Document xmlProject = nw.createDocumentFromProject(mesProject);
		StringBuffer outputBuffer = new StringBuffer();		
		String xmlString = null;
		try {
			xmlString = xmlProject.getXmlString();			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		outputBuffer.append(xmlString);
		saveExportedFileWithExtension(outputBuffer, arguments, "xml");
		return true;
	}

	/*.................................................................................................................*/
    public String getName() {
		return "NeXML (taxa, matrices, trees and annotations)";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Imports and exports NeXML2009 files (see http://www.nexml.org)" ;
   	 }
	/*.................................................................................................................*/

}
