/**
 * 
 */
package mesquite.nexml.InterpretNEXML.NexmlReader;

import java.util.Set;

import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Taxa;
import mesquite.lib.TreeVector;
import mesquite.lib.duties.TreesManager;

import org.nexml.model.Annotatable;
import org.nexml.model.Edge;
import org.nexml.model.Network;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;

/**
 * @author rvosa
 *
 */
public class NexmlTreeBlockReader extends NexmlBlockReader {

	/**
	 * @param employerEmployee
	 */
	public NexmlTreeBlockReader(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockReader#readBlock(mesquite.lib.MesquiteProject, mesquite.lib.MesquiteFile, org.nexml.model.Annotatable, org.nexml.model.OTUs)
	 */
	@Override
	protected FileElement readBlock(MesquiteProject mesProject,
			MesquiteFile mesFile, Annotatable xmlAnnotatable, OTUs xmlOTUs) {
		Taxa mesTaxa = findEquivalentTaxa(xmlOTUs, mesProject);
		TreeBlock xmlTreeBlock = (TreeBlock)xmlAnnotatable;
		TreesManager mesTreeTask = (TreesManager)getEmployerEmployee().findElementManager(TreeVector.class);
		TreeVector mesTreeVector = mesTreeTask.makeNewTreeBlock(mesTaxa, xmlTreeBlock.getLabel(), mesFile);
		for (Network<?> xmlNetwork : xmlTreeBlock) {
			MesquiteTree mesTree = new MesquiteTree(mesTaxa);
			mesTreeVector.addElement(mesTree, false);
			mesTree.setName(xmlNetwork.getLabel());	
			Set<Node> xmlNodeSet = xmlNetwork.getNodes();
			Node xmlRoot = null;
			if ( xmlNetwork instanceof org.nexml.model.Tree ) {
				xmlRoot = ((org.nexml.model.Tree<?>)xmlNetwork).getRoot();
			}
			FINDROOT: for ( Node xmlNode : xmlNodeSet ) {
				Set<Node> xmlInNodes = xmlNetwork.getInNodes(xmlNode);
				if ( xmlInNodes.size() == 0 ) {
					xmlRoot = xmlNode;
					break FINDROOT;
				}
			}
			readTree(xmlNetwork, xmlRoot, xmlNetwork.getOutNodes(xmlRoot), mesTree.getRoot(), mesTree);
		}
		return mesTreeVector;
	}
	
	/**
	 * 
	 * @param xmlNetwork
	 * @param xmlRoot
	 * @param xmlChildren
	 * @param mesRoot
	 * @param mesTree
	 */
	private void readTree(Network<?> xmlNetwork, Node xmlRoot, Set<Node> xmlChildren, int mesRoot, MesquiteTree mesTree) {
		OTU xmlOTU = xmlRoot.getOTU();
		if ( xmlOTU != null ) {
			String xmlOTUId = xmlOTU.getId();
			Taxa mesTaxa = mesTree.getTaxa();
			mesTree.setTaxonNumber(mesRoot, mesTaxa.findByUniqueID(xmlOTUId), false);
		}
		mesTree.setNodeLabel(xmlRoot.getLabel(), mesRoot);
		readAnnotations(mesTree,xmlRoot,mesRoot,mesTree);
		for ( Node xmlChild : xmlChildren ) {
			int mesChild = mesTree.sproutDaughter(mesRoot, false);
			Edge edge = xmlNetwork.getEdge(xmlRoot,xmlChild);
			mesTree.setBranchLength(mesChild,edge.getLength().doubleValue(),false);
			readAnnotations(mesTree,edge,mesChild,mesTree);
			readTree(xmlNetwork,xmlChild,xmlNetwork.getOutNodes(xmlChild),mesChild,mesTree);
		}
	}	
	
	
	/* (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockReader#getThingInMesquiteBlock(mesquite.lib.FileElement, int)
	 */
	@Override
	protected Listable getThingInMesquiteBlock(FileElement mesBlock, int index) {
		TreeVector mesTrees = (TreeVector)mesBlock;
		return mesTrees.getTree(index);
	}

}
