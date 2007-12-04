package mesquite.io.InterpretNEXML;

// $Id$

import mesquite.lib.LogWindow;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.FileElement;
import mesquite.lib.duties.FileElementManager;
import mesquite.lib.Taxa;

//import org.nexml.ObjectFactory;
//import org.xml.sax.Attributes;

/**
 * A default factory for mesquite objects.
 */
public class GenericFactory /*implements ObjectFactory*/ {
	private static LogWindow log = MesquiteModule.logWindow;
	private MesquiteProject project;
	private MesquiteFile file;
	private FileElementManager manager;	
	
	GenericFactory(MesquiteProject myProject, MesquiteFile myFile, FileElementManager myManager) {
		this.manager = myManager;
		this.project = myProject;
		this.file = myFile;
	}
	
	/**
	 * Gets the current project, which is the container for all characters, trees and taxa blocks.
	 * @return MesquiteProject
	 * @see MesquiteProject
	 */
	public MesquiteProject getProject() {
		return this.project;
	}
	
	/**
	 * Gets the current file to which processed objects are serialized.
	 * @return MesquiteFile
	 * @see MesquiteFile
	 */
	public MesquiteFile getFile() {
		return this.file;
	}
	
	/**
	 * Gets the current taxa block by the value of its id attribute.
	 * @param id the value of a nexml id attribute
	 * @return Taxa
	 * @see Taxa
	 */
	public Taxa getTaxaByID (String id) {
		return this.project.getTaxa(id, 0);
	}
	
	/**
	 * Gets the current mesquite file manager.
	 * @return FileElementManager
	 */
	public FileElementManager getManager() {
		return this.manager;
	}
	
	/**
	 * Adds a file element (a "block") to the current file in the current project.
	 * @param fe a FileElement object
	 */
	public void addToFile(FileElement fe) {
		fe.addToFile(this.file, this.project, this.manager);
	}

	/**
	 * Helper method for easier debugging, prints a line to standard out and the mesquite log.
	 * @param line a string to log
	 */
	public static void log (String line) {
		if ( true ) {			
			System.out.println(line);
			log.append(line + "\n");
		}
	}	

}
