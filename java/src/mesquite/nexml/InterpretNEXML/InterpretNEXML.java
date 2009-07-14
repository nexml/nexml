/**
 * 
 */
package mesquite.nexml.InterpretNEXML;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.duties.FileInterpreterI;

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
	@SuppressWarnings("unchecked")
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
			//logln("1From InterpretNEXML, file.getPath produces: " + file.getPath());
			//From InterpretNEXML, file.getPath produces: /home/kasia/Desktop/Vari_new.xml

		ObjectConverter ov = new ObjectConverter(this);
		// XXX pass properties here
		Properties properties = new Properties();
	    try {
	        properties.load(this.getClass().getResourceAsStream ("predicateHandlerMapping.properties"));
	        properties.put("path", file.getPath());
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }		
		ov.fillProjectFromNexml(xmlDocument, project,properties);			
	}		

/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	// XXX make compact/verbose switch
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		return true;
	}	

	
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) {
		// XXX Arguments args = new Arguments(new Parser(arguments), true);
		MesquiteProject mesProject = getProject();
		ObjectConverter objectConverter = new ObjectConverter(this);
		
		// XXX pass properties here
		Properties properties = new Properties();
	    try {
	    	properties.load(this.getClass().getResourceAsStream ("predicateHandlerMapping.properties"));
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    		
		Document xmlProject = objectConverter.createDocumentFromProject(mesProject,properties);
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
 		return "Imports and exports NeXML1.0 files (see http://www.nexml.org)" ;
   	 }
	/*.................................................................................................................*/

}
