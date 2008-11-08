package org.biophylo.Forest;
import java.util.*;
import org.biophylo.Listable;
import org.biophylo.Util.*;
import org.biophylo.*;
import org.biophylo.Util.Exceptions.*;
import java.math.*;
import org.w3c.dom.*;

public class Tree extends Listable {
	private static Logger logger = Logger.getInstance();
	
	/**
	 * 
	 */
	public Tree () {
		super();
		this.container = CONSTANT.FOREST;
		this.type = CONSTANT.TREE;
		this.tag = "tree";
	}
	
	/**
	 * @return
	 */
	public Node[] getTerminals() {
		Node root = this.getRoot();
		if ( root != null ) {
			return root.getTerminals();
		}
		return null;
	}

	/**
	 * @return
	 */
	public Node[] getInternals() {
		Node root = this.getRoot();
		if ( root != null ) {
			return root.getInternals();
		}
		return null;
	}	
	
	/**
	 * @return
	 */
	public Node getRoot() {
		Containable[] nodes = this.getEntities();
		for ( int i = 0; i < nodes.length; i++ ) {
			Node node = (Node)nodes[i];
			if ( node.isRoot() ) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * @param nodes
	 * @return
	 */
	public Node getMrca(Node[] nodes) {
		Node mrca = null;
		for ( int i = 0; i < nodes.length; i++ ) {
			if ( mrca == null ) {
				mrca = nodes[0].getMrca(nodes[i]);
			}
			else {
				mrca = mrca.getMrca(nodes[i]);
			}
		}
		return mrca;
	}
	
	/**
	 * @return
	 */
	public boolean isBinary () {
		Node[] internals = this.getInternals();
		if ( internals != null ) {
			for ( int i = 0; i < internals.length; i++ ) {
				if ( internals[i].getFirstDaughter().getNextSister().getId() != internals[i].getLastDaughter().getId() ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @param args
	 */
	public void visitDepthFirst (HashMap args) {
		Node root = this.getRoot();
		if ( root != null ) {
			root.visitDepthFirst(args);
		}
	}
	
	/**
	 * @return
	 */
	public String toNewick () {
		Node root = this.getRoot();
		if ( root != null ) {
			return this.getRoot().toNewick();
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Util.XMLWritable#toXmlElement()
	 */
	public Element toXmlElement () throws ObjectMismatch {
		logger.debug("writing tree to xml");
		String xsi_type = "nex:IntTree";
		boolean roundAsInt = true;
		Containable[] contents = this.getEntities();
		logger.debug("tree contains " + contents.length + " nodes");
		for ( int i = 0; i < contents.length; i++ ) {
			double bl = ((Node)contents[i]).getBranchLength();
			if ( Math.round(bl) != bl ) {
				xsi_type = "nex:FloatTree";
				roundAsInt = false;
				break;
			}
		}
		this.setAttributes("xsi:type", xsi_type);
		Element treeElt = createElement(getTag(),getAttributes(),getDocument());
		if ( getGeneric("dict") != null ) {
			HashMap dict = (HashMap)getGeneric("dict");
			Element dictElt = dictToXmlElement(dict);
			treeElt.appendChild(dictElt);
		}
		Containable[] nodes = this.getEntities();
		for ( int i = 0; i < nodes.length; i++ ) {
			nodes[i].setDocument(getDocument());
			if ( ((Node)nodes[i]).isRoot() ) {
				((Node)nodes[i]).setAttributes("root", "true");
			}
			treeElt.appendChild(nodes[i].toXmlElement());
		}
		for ( int i = 0; i < nodes.length; i++ ) {
			Element edgeElt = ((Node)nodes[i]).edgeToXmlElement(roundAsInt);
			if ( edgeElt != null ) {
				treeElt.appendChild(edgeElt);
			}
		}
		return treeElt;
	}
	
	
}
