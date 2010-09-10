package mesquite.nexml.InterpretNEXML.NexmlWriter;

import java.util.List;

import org.nexml.model.Annotatable;
import org.nexml.model.Document;
import org.nexml.model.NexmlWritable;

import mesquite.lib.Associable;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;

public abstract class NexmlBlockWriter extends NexmlWriter {

	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlBlockWriter(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/**
	 * 
	 * @param xmlProject
	 * @param mesBlock
	 * @return
	 */
	protected abstract Annotatable writeBlock(Document xmlProject, FileElement mesBlock);

	
	/**
	 * 
	 * @param xmlBlock
	 * @param index
	 * @return
	 */
	protected abstract Annotatable getThingInXmlBlock(NexmlWritable xmlBlock,int index);
	
	/**
	 * 
	 * @param xmlProject
	 * @param mesBlocks
	 */
	public void writeBlocks(Document xmlProject,List<FileElement> mesBlocks) {
		for ( FileElement mesFE : mesBlocks ) {
			Annotatable xmlBlock = writeBlock(xmlProject,mesFE);
			writeAttributes(mesFE,xmlBlock);
			if ( mesFE instanceof Associable ) {
				Associable mesA = (Associable)mesFE;
				int mesNumParts = mesA.getNumberOfParts();
				for ( int i = 0; i < mesNumParts; i++ ) {
					writeAnnotations(mesA,getThingInXmlBlock(xmlBlock,i),i);
				}
			}
		}		
	}
		
}
