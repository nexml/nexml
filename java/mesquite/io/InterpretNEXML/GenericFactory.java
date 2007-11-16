package mesquite.io.InterpretNEXML;

import mesquite.lib.LogWindow;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.FileElement;
import mesquite.lib.duties.FileElementManager;
import mesquite.lib.Taxa;

//import org.nexml.ObjectFactory;
//import org.xml.sax.Attributes;

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
	
	public MesquiteProject getProject() {
		return this.project;
	}
	
	public MesquiteFile getFile() {
		return this.file;
	}
	
	public Taxa getTaxaByID (String id) {
		return this.project.getTaxa(id, 0);
	}
	
	public FileElementManager getManager() {
		return this.manager;
	}
	
	public void addToFile(FileElement fe) {
		fe.addToFile(this.file, this.project, this.manager);
	}

	public static void log (String line) {
		if ( true ) {			
			System.out.println(line);
			log.append(line + "\n");
		}
	}	

}
