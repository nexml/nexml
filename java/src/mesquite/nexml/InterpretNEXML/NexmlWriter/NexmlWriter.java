package mesquite.nexml.InterpretNEXML.NexmlWriter;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;
import org.nexml.model.Document;
import org.nexml.model.DocumentFactory;
import mesquite.lib.Associable;
import mesquite.lib.Attachable;
import mesquite.lib.Bits;
import mesquite.lib.DoubleArray;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.LongArray;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteProject;
import mesquite.lib.NameReference;
import mesquite.lib.ObjectArray;
import mesquite.lib.TreeVector;
import mesquite.nexml.InterpretNEXML.AnnotationHandlers.AnnotationWrapper;
import mesquite.nexml.InterpretNEXML.Constants;
import mesquite.nexml.InterpretNEXML.NexmlMesquiteManager;
import mesquite.nexml.InterpretNEXML.AnnotationHandlers.PredicateHandler;

public class NexmlWriter extends NexmlMesquiteManager {
	
	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlWriter (EmployerEmployee employerEmployee) { 
		super(employerEmployee);
	}

	/**
	 * 
	 * @param nr
	 * @param annotatable
	 * @param value
	 */
	private void writeAnnotation(NameReference nr,Annotatable annotatable,Object value) {
		URI namespace = nr.getNamespace();
		String predicate = nr.getName();
		if ( null == namespace ) {
			namespace = URI.create(Constants.NRURIString);
			if ( predicate.indexOf(":") < 0 ) {
				predicate = Constants.NRPrefix + ":" + nr.getName();
			}
		}
		Annotation annotation = annotatable.addAnnotationValue(predicate,namespace,value);
		PredicateHandler handler = getNamespaceHandler(annotatable,annotation);
		if ( null == handler ) {
			handler = getPredicateHandler(annotatable,annotation);
		}
		if ( null != handler ) {
			handler.write();
		}
	}
	
	/**
	 * 
	 * @param associable
	 * @param annotatable
	 * @param segmentCount
	 */
	protected void writeAnnotations(Associable associable, Annotatable annotatable, int segmentCount) {		
		int numDoubs = associable.getNumberAssociatedDoubles();
		for ( int i = 0; i < numDoubs; i++ ){  
			DoubleArray array = associable.getAssociatedDoubles(i);
			double value = array.getValue(segmentCount);
			if ( MesquiteDouble.unassigned != value ) {
				writeAnnotation(array.getNameReference(),annotatable,value);
			}
		}	
		
		int numLongs = associable.getNumberAssociatedLongs();
		for ( int i = 0; i < numLongs; i++ ){  
			LongArray array = associable.getAssociatedLongs(i);
			long value = array.getValue(segmentCount);
			if ( MesquiteLong.unassigned != value  ) {
				writeAnnotation(array.getNameReference(),annotatable,value);
			}
		}
		
		int numBits = associable.getNumberAssociatedBits();
		for ( int i = 0; i < numBits; i++ ){  
			Bits array = associable.getAssociatedBits(i);			
			writeAnnotation(array.getNameReference(),annotatable,array.isBitOn(segmentCount));
		}	
		
		int numObjs = associable.getNumberAssociatedObjects();
		for ( int i = 0; i < numObjs; i++ ){  
			ObjectArray array = associable.getAssociatedObjects(i);
			Object value = array.getValue(segmentCount);
			if ( null != value ) {
				writeAnnotation(array.getNameReference(),annotatable,value);
			}
		}		
		
	}
	
	/**
	 * 
	 * @param xmlProject
	 * @param mesTaxas
	 */
	private void writeTaxaBlocks(Document xmlProject,ListableVector mesTaxas) {
		NexmlOTUsBlockWriter nobw = new NexmlOTUsBlockWriter(getEmployerEmployee());
		List<FileElement> taxaBlockList = new ArrayList<FileElement>();
		for ( int i = 0; i < mesTaxas.size(); i++ ) {
			taxaBlockList.add((FileElement)mesTaxas.elementAt(i));
		}
		nobw.writeBlocks(xmlProject, taxaBlockList);
	}	
	
	/**
	 * 
	 * @param mesObject
	 * @param xmlObject
	 */
	@SuppressWarnings("rawtypes")
	protected void writeAttributes(Object mesObject,Annotatable xmlObject) {
		Class mesClass = mesObject.getClass();
		Method[] mesMethods = mesClass.getMethods();
		Map<String,Class<?>[]> signatureOf = new HashMap<String,Class<?>[]>();
		Map<String,Method> methodOf = new HashMap<String,Method>();
		for ( int i = 0; i < mesMethods.length; i++ ) {
			signatureOf.put(mesMethods[i].getName(), mesMethods[i].getParameterTypes());
			methodOf.put(mesMethods[i].getName(), mesMethods[i]);
		}
		for ( String mesGetter : signatureOf.keySet() ) {
			if ( mesGetter.equals("getAttachments") ) {
				continue;
			}
			if ( mesGetter.startsWith("get") && signatureOf.get(mesGetter).length == 0 ) {
				String mesSetter = "s" + mesGetter.substring(1);
				if ( signatureOf.containsKey(mesSetter) && signatureOf.get(mesSetter).length == 1 ) {
					Object value = null;
					try {						
						value = methodOf.get(mesGetter).invoke(mesObject);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if ( null != value ) {
						xmlObject.addAnnotationValue(Constants.BeanPrefix+':'+mesGetter.substring(3), Constants.BeanURI, value);
					}
				}
			}
		}
		if ( mesObject instanceof Attachable ) {
			Vector attachmentVector = ((Attachable)mesObject).getAttachments();
			if ( null != attachmentVector ) {
				for ( Object obj : ((Attachable)mesObject).getAttachments() ) {
					if ( obj instanceof AnnotationWrapper ) {
						AnnotationWrapper aw = (AnnotationWrapper)obj;
						xmlObject.addAnnotationValue(aw.getName(), aw.getPredicateNamespace(), aw.getValue());
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param xmlProject
	 * @param mesCharacters
	 */
	private void writeCharacterBlocks(Document xmlProject,ListableVector mesCharacters) {
		List<FileElement> mesDatas = new ArrayList<FileElement>();
		for ( int i = 0; i < mesCharacters.size(); i++ ) {
			FileElement mesData = (FileElement)mesCharacters.elementAt(i);
			mesDatas.add(mesData); 		
		}
		NexmlCharactersBlockWriter ncbw = new NexmlCharactersBlockWriter(getEmployerEmployee());
		ncbw.writeBlocks(xmlProject, mesDatas);
	}	
	
	/**
	 * 
	 * @param xmlProject
	 * @param treeVectors
	 */
	private void writeTreeBlocks(Document xmlProject,Listable[] treeVectors) {
		List<FileElement> ltv = new ArrayList<FileElement>();
		NexmlTreeBlockWriter ntbw = new NexmlTreeBlockWriter(getEmployerEmployee());
		for ( int i = 0; i < treeVectors.length; i++ ) {	
			ltv.add((FileElement)treeVectors[i]);
		}	
		ntbw.writeBlocks(xmlProject, ltv);
	}
	
	/**
	 * 
	 * @param mesProject
	 * @return
	 */
	private Document writeProject(MesquiteProject mesProject) {
		Document xmlProject = DocumentFactory.safeCreateDocument();		
		return xmlProject;
	}
	
	/**
	 * 
	 * @param mesProject
	 * @return
	 */
	public Document createDocumentFromProject(MesquiteProject mesProject) {
		ListableVector mesTaxas = mesProject.getTaxas();
		Document xmlProject = writeProject(mesProject);
		try {
			writeTaxaBlocks(xmlProject,mesTaxas);
			writeCharacterBlocks(xmlProject,mesProject.getCharacterMatrices());			
			for ( int i = 0; i < mesTaxas.size(); i++ ) {
				Listable[] treeVectors = mesProject.getCompatibleFileElements(TreeVector.class, mesTaxas.elementAt(i));
				writeTreeBlocks(xmlProject,treeVectors);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return xmlProject;
	}	
	
}
