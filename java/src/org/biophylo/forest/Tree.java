package org.biophylo.forest;
import java.util.*;
import org.biophylo.Listable;
import org.biophylo.util.*;
import org.biophylo.*;
import org.biophylo.util.exceptions.*;
import java.math.*;
import org.w3c.dom.*;

public class Tree extends Listable {
	private static Logger logger = Logger.getInstance();
	
	/**
	 * 
	 */
	public Tree () {
		super();
		mContainer = CONSTANT.FOREST;
		mType = CONSTANT.TREE;
		mTag = "tree";
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
	
	public void generateXml(StringBuffer sb,boolean compact) throws ObjectMismatch {
		String xsi_type = "nex:IntTree";
		Containable[] contents = getEntities();
		for ( int i = 0; i < contents.length; i++ ) {
			double bl = ((Node)contents[i]).getBranchLength();
			if ( Math.round(bl) != bl ) {
				xsi_type = "nex:FloatTree";
				break;
			}
		}
		setAttributes("xsi:type", xsi_type);
		getXmlTag(sb, false);
		Node root = getRoot();
		if ( root != null ) {
			root.generateXml(sb, xsi_type.equals("nex:IntTree"));
		}
		sb.append("</").append(getTag()).append('>');
	}
	
	
}
