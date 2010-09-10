package mesquite.nexml.InterpretNEXML.NexmlWriter;

import java.util.Iterator;

import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.Tree;
import mesquite.lib.TreeVector;

import org.nexml.model.Annotatable;
import org.nexml.model.Document;
import org.nexml.model.FloatEdge;
import org.nexml.model.Network;
import org.nexml.model.NexmlWritable;
import org.nexml.model.Node;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;

public class NexmlTreeBlockWriter extends NexmlBlockWriter {

	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlTreeBlockWriter(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockWriter#writeBlock(org.nexml.model.Document, mesquite.lib.FileElement)
	 */
	@Override
	protected Annotatable writeBlock(Document xmlProject, FileElement mesBlock) {
		TreeVector mesTrees = (TreeVector)mesBlock;
		OTUs xmlTaxa = findEquivalentTaxa(mesTrees.getTaxa(),xmlProject);
		TreeBlock xmlForest = xmlProject.createTreeBlock(xmlTaxa);
		xmlForest.setLabel(mesTrees.getName());
		int ntrees = mesTrees.getNumberOfTrees();
		for ( int j = 0; j < ntrees; j++ ) {
			Tree mesTree = mesTrees.getTree(j);
			org.nexml.model.Tree<FloatEdge> xmlTree = xmlForest.createFloatTree();
			Node xmlRoot = xmlTree.createNode();
			xmlTree.setLabel(mesTree.getName());
			xmlRoot.setRoot(mesTree.getRooted());
			int mesRoot = mesTree.getRoot();
			try {
				writeTree(mesTree,xmlTree,mesRoot,xmlRoot,null,xmlTaxa);
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			writeAnnotations(mesTrees,xmlTree,j);
			writeAttributes(mesTree,xmlTree);
		}
		return xmlForest;
	}
	
	/**
	 * 
	 * @param mesTree
	 * @param xmlTree
	 * @param mesNode
	 * @param xmlRoot
	 * @param xmlParentNode
	 * @param xmlTaxa
	 */
	private void writeTree(Tree mesTree,org.nexml.model.Tree<FloatEdge> xmlTree,int mesNode,Node xmlRoot,Node xmlParentNode,OTUs xmlTaxa) {
		xmlRoot.setLabel(mesTree.getNodeLabel(mesNode));
		if ( mesTree.nodeIsTerminal(mesNode) ) {
			int[] mesTaxonNumber = mesTree.getTerminalTaxa(mesNode);
			Taxa mesTaxa = mesTree.getTaxa();
			Taxon mesTaxon = mesTaxa.getTaxon(mesTaxonNumber[0]);
			OTU xmlTaxon = findEquivalentTaxon(mesTaxon,xmlTaxa);
			xmlRoot.setOTU(xmlTaxon);		
		}
		if ( xmlParentNode != null ) {
			FloatEdge edge = xmlTree.createEdge(xmlParentNode,xmlRoot);
			edge.setLength(mesTree.getBranchLength(mesNode));
		}		
		for (int d = mesTree.firstDaughterOfNode(mesNode); mesTree.nodeExists(d); d = mesTree.nextSisterOfNode(d)) {
			Node xmlChild = xmlTree.createNode();
			writeTree(mesTree,xmlTree,d,xmlChild,xmlRoot,xmlTaxa);			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockWriter#getThingInXmlBlock(org.nexml.model.NexmlWritable, int)
	 */
	@Override
	protected Annotatable getThingInXmlBlock(NexmlWritable xmlBlock, int index) {
		TreeBlock xmlTrees = (TreeBlock)xmlBlock;
		Iterator<Network<?>> xmlTreesIterator = xmlTrees.iterator();
		int i = 0;
		while ( xmlTreesIterator.hasNext() ) {
			if ( i == index ) {
				return xmlTreesIterator.next();
			}
			else {
				i++;
				xmlTreesIterator.next();
			}
		}
		return null;
	}

}
