package mesquite.nexml.InterpretNEXML.NexmlReaders;

import java.util.ArrayList;
import java.util.List;
import mesquite.lib.Associable;
import mesquite.lib.Attachable;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.NameReference;
import mesquite.nexml.InterpretNEXML.NexmlMesquiteManager;
import mesquite.nexml.InterpretNEXML.AnnotationHandlers.AnnotationWrapper;
import mesquite.nexml.InterpretNEXML.AnnotationHandlers.PredicateHandler;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;
import org.nexml.model.Document;
import org.nexml.model.Matrix;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;

public class NexmlReader extends NexmlMesquiteManager {	
		
	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlReader (EmployerEmployee employerEmployee) { 
		super(employerEmployee);
	}
	
	/**
	 * 
	 * @param xmlDocument
	 * @param mesProject
	 * @return
	 */
	public MesquiteProject fillProjectFromNexml(Document xmlDocument,MesquiteProject mesProject) {
		List<OTUs> xmlOTUsList = xmlDocument.getOTUsList();
		MesquiteFile mesFile = mesProject.getFile(0);
		
		// process taxa blocks
		NexmlOTUsBlockReader nobr = new NexmlOTUsBlockReader(getEmployerEmployee());		
		List<Annotatable> xmlAnnoOTUsList = new ArrayList<Annotatable>();
		for ( OTUs xmlOTUs : xmlDocument.getOTUsList() ) {
			xmlAnnoOTUsList.add(xmlOTUs);
		}
		nobr.readBlocks(mesProject, mesFile, xmlAnnoOTUsList);
		
		for ( OTUs xmlOTUs : xmlOTUsList ) {
			
			// process tree blocks
			NexmlTreeBlockReader ntbr = new NexmlTreeBlockReader(getEmployerEmployee());
			List<Annotatable> xmlAnnoTreeBlockList = new ArrayList<Annotatable>();
			for ( TreeBlock xmlTreeBlock : xmlDocument.getTreeBlockList(xmlOTUs) ) {
				xmlAnnoTreeBlockList.add(xmlTreeBlock);
			}
			ntbr.readBlocks(mesProject, mesFile, xmlAnnoTreeBlockList);

			// process characters blocks
			NexmlCharactersBlockReader ncbr = new NexmlCharactersBlockReader(getEmployerEmployee());
			List<Annotatable> xmlCharactersBlockList = new ArrayList<Annotatable>();
			for ( Matrix<?> xmlMatrix : xmlDocument.getMatrices(xmlOTUs) ) {
				xmlCharactersBlockList.add(xmlMatrix);				
			}			
			ncbr.readBlocks(mesProject, mesFile, xmlCharactersBlockList);
		}
		return mesProject;
	}	
	
	/**
	 * 
	 * @param mesAttachable
	 * @param mesAnnotatable
	 */
	protected void readWrappedAnnotations(Attachable mesAttachable,Annotatable mesAnnotatable) {
		for ( Annotation annotation : mesAnnotatable.getAllAnnotations() ) {
			String name = annotation.getProperty();
			if ( null == name || "".equals(name) ) {
				name = annotation.getRel();
			}
			AnnotationWrapper aw = new AnnotationWrapper();
			aw.setValue(annotation.getValue());
			aw.setPredicateNamespace(annotation.getPredicateNamespace());
			aw.setName(name);
			mesAttachable.attach(aw);
		}
	}
	
	/**
	 * 
	 * @param mesAssociable
	 * @param xmlAnnotatable
	 * @param segmentCount
	 * @param mesListable
	 */
	protected void readAnnotations(Associable mesAssociable,Annotatable xmlAnnotatable,int segmentCount,Listable mesListable) {
		for ( Annotation xmlAnnotation : xmlAnnotatable.getAllAnnotations() ) {
			PredicateHandler handler = getNamespaceHandler(xmlAnnotatable,xmlAnnotation);
			if ( null == handler ) {
				handler = getPredicateHandler(xmlAnnotatable,xmlAnnotation);
			}
			Object convertedValue = handler.getValue();			
			debug("using Handler "+handler+" with converted value "+convertedValue);
			if ( convertedValue instanceof Boolean ) {
				NameReference mesNr = mesAssociable.makeAssociatedBits(handler.getPredicate());
				mesNr.setNamespace(xmlAnnotation.getPredicateNamespace());
				mesAssociable.setAssociatedBit(mesNr,segmentCount,(Boolean)convertedValue);
			}
			else if ( convertedValue instanceof Double ) {
				NameReference mesNr = mesAssociable.makeAssociatedDoubles(handler.getPredicate());
				mesNr.setNamespace(xmlAnnotation.getPredicateNamespace());
				mesAssociable.setAssociatedDouble(mesNr,segmentCount,(Double)convertedValue);
			}
			else if ( convertedValue instanceof Long ) {
				NameReference mesNr = mesAssociable.makeAssociatedLongs(handler.getPredicate());
				mesNr.setNamespace(xmlAnnotation.getPredicateNamespace());
				mesAssociable.setAssociatedLong(mesNr,segmentCount,(Long)convertedValue);					
			}	
			else if ( convertedValue instanceof Object ) {
				NameReference mesNr = mesAssociable.makeAssociatedObjects(handler.getPredicate());
				mesNr.setNamespace(xmlAnnotation.getPredicateNamespace());
				mesAssociable.setAssociatedObject(mesNr,segmentCount,convertedValue);
			}
			handler.read(mesAssociable, mesListable, segmentCount);
		}
		
	}			
	
}
