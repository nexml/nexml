package org.biophylo.Forest;
import java.util.*;
import org.biophylo.Listable;
import org.biophylo.Util.*;
import org.biophylo.*;
import org.biophylo.Util.Exceptions.*;
import java.math.*;

public class Tree extends Listable {
	private static Logger logger = Logger.getInstance();
	public Tree () {
		super();
		this.container = CONSTANT.FOREST;
		this.type = CONSTANT.TREE;
		this.tag = "tree";
	}
	
	public Node[] getTerminals() {
		Node root = this.getRoot();
		if ( root != null ) {
			return root.getTerminals();
		}
		return null;
	}

	public Node[] getInternals() {
		Node root = this.getRoot();
		if ( root != null ) {
			return root.getInternals();
		}
		return null;
	}	
	
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
	
	public void visitDepthFirst (HashMap args) {
		Node root = this.getRoot();
		if ( root != null ) {
			root.visitDepthFirst(args);
		}
	}
	
	public String toNewick () {
		Node root = this.getRoot();
		if ( root != null ) {
			return this.getRoot().toNewick();
		}
		return "";
	}
	
	public String toXml () throws ObjectMismatch {
		logger.debug("writing tree to xml");
		String xsi_type = "nex:IntTree";
		Containable[] contents = this.getEntities();
		logger.debug("tree contains " + contents.length + " nodes");
		for ( int i = 0; i < contents.length; i++ ) {
			double bl = ((Node)contents[i]).getBranchLength();
			if ( Math.round(bl) != bl ) {
				xsi_type = "nex:FloatTree";
				break;
			}
		}
		this.setAttributes("xsi:type", xsi_type);
		StringBuffer sb = new StringBuffer();
		sb.append(this.getXmlTag(false));
		Node root = this.getRoot();
		if ( root != null ) {
			if ( xsi_type.equals("nex:IntTree") ) {
				sb.append(root.toXml(true));
			}
			else {
				sb.append(root.toXml());
			}
		}
		sb.append("</");
		sb.append(this.getTag());
		sb.append('>');
		return sb.toString();
	}
	
	
}
