package mesquite.io.InterpretNEXML;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.TreeVector;
import mesquite.lib.duties.TreesManager;
import java.util.*;
import org.nexml.ObjectFactory;
import org.xml.sax.Attributes;

public class TreesFactory extends GenericFactory implements ObjectFactory {
	private TreeVector trees;
	private Hashtable lengthOfNode;
	private Hashtable parentOfNode;
	private Hashtable taxonOfNode;
	private Hashtable labelOfNode;
	private boolean lengthsAreFloats;
	private Vector nodes;
	
	TreesFactory(MesquiteProject myProject, MesquiteFile myFile, TreesManager myManager) {
		super( myProject, myFile, myManager );
		log("created TreesFactory for project " + myProject + " with file " + myFile + " and manager " + myManager);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#createObject(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public Object createObject(String namespaceURI, String localName, String qName, Attributes atts) {
		log("creating object from element " + localName);
		if ( localName.equals("trees") ) {
			this.handleTreesElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("tree") ) {
			this.handleTreeElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("node") ) {
			this.handleNodeElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("edge") ) {
			this.handleEdgeElement(namespaceURI, localName, qName, atts);
		}
		return null;
	}
	
	/*-----------------------------------------*/
	private void handleTreesElement(String namespaceURI, String localName, String qName, Attributes atts) {
		log("handling trees element");
		Taxa taxa = this.getTaxaByID(atts.getValue("taxa"));
		log("associated taxa: " + taxa.getName() );
		String label = atts.getValue("id");
		TreesManager tm = (TreesManager)this.getManager();
		log("manager: " + tm + ", id: " + label + ", file: " + getFile());
		this.trees = tm.makeNewTreeBlock(taxa, label, this.getFile());
	}
	
	/*-----------------------------------------*/
	private void handleTreeElement(String namespaceURI, String localName, String qName, Attributes atts) {
		log("handling tree element");
		MesquiteTree tree = new MesquiteTree(this.trees.getTaxa());
		String label = atts.getValue("label");
		if ( label != null ) {
			tree.setName(label);
		}
		this.trees.addElement(tree, false);
		this.lengthOfNode = new Hashtable();
		this.parentOfNode = new Hashtable();
		this.taxonOfNode  = new Hashtable();
		this.labelOfNode  = new Hashtable();
		this.nodes = new Vector();
	}	
	
	/*-----------------------------------------*/
	private void handleNodeElement(String namespaceURI, String localName, String qName, Attributes atts) {
		String taxonID = atts.getValue("taxon");
		String nodeID  = atts.getValue("id");
		String label   = atts.getValue("label");
		log("handling node element " + this.nodes.size() + ", id: " + nodeID + ", label: " + label);
		if ( taxonID != null ) {			
			this.taxonOfNode.put(nodeID, taxonID);
		}		
		if ( label != null ) {
			this.labelOfNode.put(nodeID, label);
		}
		this.nodes.addElement(nodeID);
	}	
	
	/*-----------------------------------------*/
	private void handleEdgeElement(String namespaceURI, String localName, String qName, Attributes atts) {
		log("handling edge element");
		String source = atts.getValue("source");
		String target = atts.getValue("target");
		this.parentOfNode.put(target, source);
		String fl = atts.getValue("float");
		String in = atts.getValue("int");
		if ( fl != null ) {
			this.lengthOfNode.put(target, fl);
			lengthsAreFloats = true;
		}
		else if ( in != null ) {
			this.lengthOfNode.put(target, in);
			lengthsAreFloats = false;
		}
	}	
	
	/*-----------------------------------------*/
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#getCurrentObject()
	 */
	public Object getCurrentObject() {
		return this.trees;
	}

	/*-----------------------------------------*/
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#getElementsToHandle()
	 */
	public String[] getElementsToHandle() {
		String[] elements = {
			"trees",
			"tree",
			"node",
			"edge"
		};
		return elements;
	}
	
	/*-----------------------------------------*/
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#objectIsComplete(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void objectIsComplete(String namespaceURI, String localName, String qName, Attributes atts) {
		
		// only need to clean up once closing </tree> element
		if ( localName.equals("tree") ) {			
			
			// fetch last tree we instantiated earlier
			MesquiteTree tree = this.trees.getTree( this.trees.size() - 1 );
			
			// calculate number of tips
			int numParents = this.parentOfNode.values().size();
			int numTips = this.nodes.size() - numParents;			
			log("building tree, nodes: " + numParents + ", tips: " + numTips);
			
			// fetch node ID
			String rootID = "root";
			for ( int i = 0; i < this.nodes.size(); i++ ) {
				String nodeID = (String)this.nodes.get(i);
				if ( ! this.parentOfNode.containsKey(nodeID) ) {
					rootID = nodeID;
					break;
				}
			}
			
			// set label, branch lengths, taxon association of root
			this.setNodeAttributes(tree, rootID, tree.getRoot() );
			
			// fetch children, start recursion
			Vector children = this.getChildIDs(rootID);
			for ( int i = 0; i < children.size(); i++ ) {
				this.buildTree(tree, (String)children.get(i), tree.getRoot() );
			}
			
			log(tree.writeTreeByNames(true));
		}
		else if ( localName.equals("trees") ) {
			log("*** done parsing nexml trees block");
		}
	}
	
	/*-----------------------------------------*/
	private void setNodeAttributes(MesquiteTree tree, String currentNode, int index) {
		// set name
		String label = (String)this.labelOfNode.get(currentNode);
		if ( label != null ) {
			tree.setNodeLabel(label, index);
			log("setting label " + label + " for node " + currentNode);
		}	
		
		// link node to taxon
		String taxonID = (String)this.taxonOfNode.get(currentNode);
		if ( taxonID != null ) {
			log("node " + currentNode + " is associated with taxon " + taxonID);
			Taxa taxa = this.trees.getTaxa();
			for ( int i = 0; i < taxa.getNumTaxa(); i++ ) {
				Taxon t = taxa.getTaxon(i);
				String thisTaxonID = t.getUniqueID();
				if ( taxonID.equals(thisTaxonID) ) {
					log("setting taxon " + thisTaxonID + " with index " + i + " for node " + currentNode);
					tree.setTaxonNumber(index, i, false);
					break;
				}
			}
		}
		
		// set branch length
		String lengthStr = (String)this.lengthOfNode.get(currentNode);
		if ( lengthStr != null ) {
			log("setting length " + lengthStr + " for node " + currentNode);
			if ( lengthsAreFloats ) {
				float length = Float.parseFloat(lengthStr);
				tree.setBranchLength(index, length, false);
			}
			else {
				int length = Integer.parseInt(lengthStr);
				tree.setBranchLength(index, length, false);
			}
		}		
	}
	
	/*-----------------------------------------*/
	/*
	 * builds up tree recursively from root to tips in pre-order traversal
	 */
	private void buildTree (MesquiteTree tree, String currentNode, int parentIndex) {		
		log("recursing past node " + currentNode + ", parent index: " + parentIndex);
		int myIndex = tree.sproutDaughter(parentIndex, true);		
		this.setNodeAttributes(tree, currentNode, myIndex);
		
		// assemble children and recurse
		Vector children = this.getChildIDs(currentNode);
		for ( int i = 0; i < children.size(); i++ ) {			
			String childID = (String)children.get(i);
			log("node " + childID + " is child of " + currentNode);
			this.buildTree(tree, childID, myIndex);
		}
	}
	
	/*-----------------------------------------*/
	private Vector getChildIDs (String parentID) {
		Vector childIDs = new Vector();
		for ( int i = 0; i < this.nodes.size(); i++ ) {
			String childID = (String)this.nodes.elementAt(i);
			String thisParentID = (String)this.parentOfNode.get(childID);
			if ( parentID.equals(thisParentID) ) {
				childIDs.add(childID);
			}
		}
		return childIDs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#setCharacterData(char[])
	 */
	public void setCharacterData(char[] character) {
		// TODO Auto-generated method stub

	}

}
