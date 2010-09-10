package mesquite.nexml.InterpretNEXML.NexmlReaders;

import java.util.List;

import org.nexml.model.Annotatable;
import org.nexml.model.OTUs;
import org.nexml.model.OTUsLinkable;
import org.nexml.model.Segmented;

import mesquite.lib.Attachable;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;

public abstract class NexmlBlockReader extends NexmlReader {

	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlBlockReader(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/**
	 * 
	 * @param xmlProject
	 * @param mesBlock
	 * @return
	 */
	protected abstract FileElement readBlock(MesquiteProject mesProject, MesquiteFile mesFile, Annotatable xmlAnnotatable, OTUs xmlOTUs);
	
	/**
	 * 
	 * @param mesBlock
	 * @param index
	 * @return
	 */
	protected abstract Listable getThingInMesquiteBlock(FileElement mesBlock,int index);
	
	/**
	 * 
	 * @param mesProject
	 * @param mesFile
	 * @param xmlBlocks
	 */
	public void readBlocks(MesquiteProject mesProject, MesquiteFile mesFile, List<Annotatable> xmlBlocks) {
		for ( Annotatable xmlAnnotatable : xmlBlocks ) {
			OTUs xmlOTUs = null;
			if ( xmlAnnotatable instanceof OTUsLinkable ) {
				xmlOTUs = ((OTUsLinkable)xmlAnnotatable).getOTUs();
			}
			FileElement mesFE = readBlock(mesProject, mesFile, xmlAnnotatable, xmlOTUs);
			readWrappedAnnotations((Attachable)mesFE,xmlAnnotatable);
			Segmented<?> xmlSegmented = (Segmented<?>)xmlAnnotatable;
			int count = xmlSegmented.getSegmentCount();
			for ( int i = 0; i < count; i++ ) {
				Annotatable xmlSegment = (Annotatable)xmlSegmented.getSegment(i);
				Listable mesL = getThingInMesquiteBlock(mesFE,i);
				if ( null != mesL ) {
					readAnnotations(mesFE,xmlSegment,i,mesL);
				}
			}
			mesFE.addToFile(mesFile, mesProject, getEmployerEmployee().findElementManager(mesFE.getClass()));
		}		
	}
	
	
}
