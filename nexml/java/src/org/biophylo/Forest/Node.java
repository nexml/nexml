package org.biophylo.Forest;
import org.biophylo.Util.*;
import org.biophylo.Containable;
import org.biophylo.Visitor;
import org.biophylo.Taxa.*;
import org.biophylo.Mediators.*;
import org.biophylo.Util.Exceptions.*;
import org.w3c.dom.*;

import java.util.*;
import java.math.*;

public class Node extends Containable implements TaxonLinker {
	private static Logger logger = Logger.getInstance();
	protected Node parent;
	Vector children;
	protected double branch_length; 
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	
	/**
	 * 
	 */
	public Node () {
		super();
		this.type = CONSTANT.NODE;
		this.container = CONSTANT.TREE;
		this.children = new Vector();
		this.tag = "node";
		this.parent = null;
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxonLinker#unsetTaxon()
	 */
	public void unsetTaxon() {
		int linkerId = this.getId();
		taxaMediator.removeLink(-1, linkerId);
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxonLinker#setTaxon(org.biophylo.Taxa.Taxon)
	 */
	public void setTaxon (Taxon taxon) {
		int taxonId = taxon.getId();
		int linkerId = this.getId();
		taxaMediator.setLink(taxonId, linkerId);
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxonLinker#getTaxon()
	 */
	public Taxon getTaxon () {
		int linkerId = this.getId();
		return (Taxon)taxaMediator.getLink(linkerId);
	}
	
	/**
	 * @param parent
	 */
	public void setParent(Node parent) { // XXX node mediator
		this.parent = parent;
	}
	
	/**
	 * @return
	 */
	public Node getParent() { // XXX node mediator
		return this.parent;
	}	
	
	/**
	 * @param child
	 */
	public void setChild(Node child) { // XXX node mediator
		this.children.add(child);
	}	
	
	/**
	 * @return
	 */
	public Node[] getChildren() { // XXX node mediator
		Node[] result = new Node[this.children.size()];
		this.children.copyInto(result);
		return result;
	}
	
	/**
	 * @param index
	 * @return
	 */
	public Node getChild(int index) {
		return this.getChildren()[index];
	}
	
	/**
	 * @return
	 */
	public Node getFirstDaughter() {
		Node[] children = this.getChildren();
		if ( children.length != 0 ) {
			return children[0];
		}
		else {
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Node getLastDaughter() {
		Node[] children = this.getChildren();
		if ( children.length != 0 ) {
			return children[children.length-1];
		}
		else {
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Node getNextSister () {
		if ( ! this.isRoot() ) {
			Node[] sisters = this.getSisters();
			int numSibs = sisters.length;
			for ( int i = 0; i < numSibs; i++ ) {
				if ( sisters[i].getId() == this.getId() && i < numSibs - 1) {
					int nsIndex = i+1;
					return sisters[nsIndex];
				}
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public Node getPreviousSister() {
		if ( ! this.isRoot() ) {
			Node[] siblings = this.getParent().getChildren();
			for ( int i = 0; i < siblings.length; i++ ) {
				if ( siblings[i].getId() == this.getId() && i > 0 ) {
					return siblings[i-1];
				}
			}
		}
		return null;		
	}
	
	/**
	 * @return
	 */
	public Node[] getAncestors () {
		Vector ancestors = new Vector();
		Node n = this.getParent();
		while ( n != null ) {
			ancestors.add(n);
			n = n.getParent();
		}
		Node[] result = new Node[ancestors.size()];
		ancestors.copyInto(result);
		return result;
	}
	
	/**
	 * @return
	 */
	public Node[] getSisters(){
		Node parent = this.getParent();
		if ( parent != null ) {
			return parent.getChildren();
		}
		else {
			return null;
		}		
	}
	
	/**
	 * @return
	 */
	public Node[] getDescendants() {
		class NodeCollector implements Visitor {
			public Vector result = new Vector();
			public void visit (Containable node) {
				result.add(node);
			}
		}
		NodeCollector nc = new NodeCollector();
		HashMap args = new HashMap();
		args.put("pre", nc);
		this.visitDepthFirst(args);
		nc.result.remove(0);
		Node[] result = new Node[nc.result.size()];
		nc.result.copyInto(result);
		return result;
	}
	
	/**
	 * @return
	 */
	public Node[] getTerminals() {		
		Vector terminals = new Vector();
		Node[] desc = this.getDescendants();
		for ( int i = 0; i < desc.length; i++ ) {
			if ( desc[i].isTerminal() ) {
				terminals.add(desc[i]);
			}
		}
		Node[] result = new Node[terminals.size()];
		terminals.copyInto(result);
		return result;
	}
	
	/**
	 * @return
	 */
	public Node[] getInternals() {
		Vector internals = new Vector();
		Node[] desc = this.getDescendants();
		for ( int i = 0; i < desc.length; i++ ) {
			if ( desc[i].isInternal() ) {
				internals.add(desc[i]);
			}
		}
		Node[] result = new Node[internals.size()];
		internals.copyInto(result);
		return result;
	}	
	
	/**
	 * @param node
	 * @return
	 */
	public Node getMrca(Node node) {
		Node[] myAnc = this.getAncestors();
		Node[] otherAnc = this.getAncestors();
		Node mrca = null;
		MRCA: for ( int i = 0; i < myAnc.length; i++) {
			for ( int j = 0; j < otherAnc.length; j++ ) {
				if ( myAnc[i].getId() == otherAnc[j].getId() ) {
					mrca = myAnc[i];
					break MRCA;
				}
			}
		}
		return mrca;
	}
	
	/**
	 * @return
	 */
	public Node getLeftmostTerminal () {
		Node node = this;
		Node lmt = null;
		while (node != null ) {
			lmt = node;
			node = node.getFirstDaughter();
		}
		return lmt;
	}
	
	/**
	 * @return
	 */
	public Node getRightmostTerminal() {
		Node node = this;
		Node rmt = null;
		while (node != null ) {
			rmt = node;
			node = node.getLastDaughter();
		}
		return rmt;		
	}
	
	/**
	 * @param branch_length
	 */
	public void setBranchLength(double branch_length) {
		this.branch_length = branch_length;
	}
	
	/**
	 * @return
	 */
	public double getBranchLength() {
		return this.branch_length;
	}
	
	/**
	 * @return
	 */
	public boolean isTerminal() {
		return this.getChildren().length == 0 ? true : false;
	}
	
	/**
	 * @return
	 */
	public boolean isInternal () {
		return ! this.isTerminal();
	}

	/**
	 * @return
	 */
	public boolean isRoot() {
		if ( this.getParent() == null ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * @return
	 */
	public boolean isFirst() {
		return this.getPreviousSister() == null ? true : false;
	}
	
	/**
	 * @return
	 */
	public boolean isLast() {
		return this.getNextSister() == null ? true : false;
	}
	
	/**
	 * @param ancestor
	 * @return
	 */
	public boolean isDescendantOf(Node ancestor) {
		Node[] anc = this.getAncestors();
		for ( int i = 0; i < anc.length; i++ ) {
			if ( anc[i].getId() == ancestor.getId() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param descendant
	 * @return
	 */
	public boolean isAncestorOf(Node descendant) {
		return descendant.isDescendantOf(this);
	}
	
	/**
	 * @param sister
	 * @return
	 */
	public boolean isSisterOf(Node sister) {
		Node[] sisters = this.getSisters();
		for ( int i = 0; i < sisters.length; i++ ) {
			if ( sisters[i].getId() == sister.getId() && sisters[i].getId() != this.getId() ) {
				return true;
			}
 		}
		return false;
	}
	
	/**
	 * @param parent
	 * @return
	 */
	public boolean isChildOf(Node parent) {
		if ( ! this.isRoot() ) {
			return this.getParent().getId() == parent.getId();
		}
		return false;
	}
	
	/**
	 * @param child
	 * @return
	 */
	public boolean isParentOf(Node child) {
		return child.isChildOf(this);
	}
	
	/**
	 * @param nodes
	 * @return
	 */
	public boolean isOutgroupOf(Node[] nodes) {
		for ( int i = 0; i < nodes.length; i++ ) {
			for ( int j = i+1; j < nodes.length; j++ ) {
				Node mrca = nodes[i].getMrca(nodes[j]);
				if ( mrca.isAncestorOf(this) ) {
					return true;
				}
			}
		}
		return true;	
	}
	
	/**
	 * @return
	 */
	public double calcPathToRoot() {
		double path = 0;
		Node[] anc = this.getAncestors();
		for ( int i = 0; i < anc.length; i++ ) {
			path += anc[i].getBranchLength();
		}
		return path;
	}
	
	/**
	 * @return
	 */
	public int calcNodesToRoot() {
		Node[] anc = this.getAncestors();
		if ( anc != null ) {
			return anc.length;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * @return
	 */
	public int calcMaxNodesToTips() {
		Node[] tips = this.getTerminals();
		int maxnodes = 0;
		for ( int i = 0; i < tips.length; i++ ) {
			Node focal = tips[i];
			int dist = 0;
			while ( focal.getId() != this.getId() ) {
				dist++;
				focal = focal.getParent();
			}
			if ( dist > maxnodes) {
				maxnodes = dist;
			}
		}
		return maxnodes;
	}
	
	/**
	 * @return
	 */
	public int calcMinNodesToTips() {
		Node[] tips = this.getTerminals();
		int minnodes = 0;
		boolean uninit = true;
		for ( int i = 0; i < tips.length; i++ ) {
			Node focal = tips[i];
			int dist = 0;
			while ( focal.getId() != this.getId() ) {
				dist++;
				focal = focal.getParent();
			}
			if ( dist < minnodes || uninit ) {
				minnodes = dist;
				uninit = true;
			}
		}
		return minnodes;
	}
	
	/**
	 * @return
	 */
	public double calcMaxPathToTips() {
		double maxpath = 0;
		Node[] desc = this.getDescendants();
		for ( int i = 0; i < desc.length; i++ ) {
			Node focal = desc[i];
			double dist = 0;
			while ( focal.getId() != this.getId() ) {
				dist += focal.getBranchLength();
				focal = focal.getParent();
			}
			if ( dist > maxpath ) {
				maxpath = dist;
			}
		}
		return maxpath;
	}
	
	/**
	 * @return
	 */
	public double calcMinPathToTips () {
		double minpath = 0;
		boolean uninit = true;
		Node[] desc = this.getDescendants();
		for ( int i = 0; i < desc.length; i++ ) {
			Node focal = desc[i];
			double dist = 0;
			while ( focal.getId() != this.getId() ) {
				dist += focal.getBranchLength();
				focal = focal.getParent();
			}
			if ( dist < minpath || uninit ) {
				minpath = dist;
				uninit = true;
			}
		}
		return minpath;
	}
	
	/**
	 * @param node
	 * @return
	 */
	public double calcPatristicDistance (Node node) {
		double dist = 0;
		Node mrca = this.getMrca(node);
		int mrcaId = mrca.getId();
		Node me = this;
		while ( me.getId() != mrcaId ) {
			dist += me.getBranchLength();
			me = me.getParent();
		}
		while ( node.getId() != mrcaId ) {
			dist += node.getBranchLength();
			node = node.getParent();
		}
		return dist;
	}
	
	/**
	 * @param node
	 * @return
	 */
	public int calcNodalDistance (Node node) {
		int dist = 0;
		Node mrca = this.getMrca(node);
		int mrcaId = mrca.getId();
		Node me = this;
		while ( me.getId() != mrcaId ) {
			dist++;
			me = me.getParent();
		}
		while ( node.getId() != mrcaId ) {
			dist++;
			node = node.getParent();
		}
		return dist;
	}
	
	/**
	 * @param args
	 */
	public void visitDepthFirst(HashMap args) {
		this.process(args,"pre");
		Node fd = this.getFirstDaughter();
		if ( fd != null ) {
			this.process(args,"pre_daughter");
			fd.visitDepthFirst(args);
			this.process(args,"post_daughter");
		}
		else {
			this.process(args,"no_daughter");
		}
		this.process(args, "in");
		Node ns = this.getNextSister();
		if ( ns != null ) {
			this.process(args, "pre_sister");
			ns.visitDepthFirst(args);
			this.process(args, "post_sister");
		}
		else {
			this.process(args, "no_sister");
		}
		this.process(args, "post");
	}	
	
	/**
	 * @return
	 */
	public String toNewick() {
		final StringBuffer newick = new StringBuffer();
		class PreDaughterWriter implements Visitor {
			public void visit (Containable node) {
				newick.append("(");
			}
		}
		class PreSisterWriter implements Visitor {
			public void visit (Containable node) {
				newick.append(",");
			}			
		}
		class PostDaughterWriter implements Visitor {
			public void visit (Containable node) {
				newick.append(")");
			}			
		}
		class InWriter implements Visitor {
			public void visit (Containable node) {
				String name = node.getName();
				double bl = ((Node)node).getBranchLength();
				if ( name != null ) {
					newick.append(name);
				}
				newick.append(":");
				newick.append(bl+"");
			}			
		}
		HashMap args = new HashMap();
		args.put("pre_daughter", new PreDaughterWriter());
		args.put("pre_sister", new PreSisterWriter());
		args.put("post_daughter", new PostDaughterWriter());
		args.put("in", new InWriter());
		this.visitDepthFirst(args);
		newick.append(";");
		return newick.toString();
	}
	
	/**
	 * @param roundAsInt
	 * @return
	 */
	public Element edgeToXmlElement (boolean roundAsInt) {
		HashMap attrs = new HashMap();
		attrs.put("id", "edge" + getId());
		attrs.put("target", getXmlId());
		String tagName = null;
		if ( isRoot() ) {
			if ( getBranchLength() != 0 ) {
				tagName = "rootedge";
			}
			else {
				return null;
			}
		}
		else {
			tagName = "edge";
			attrs.put("source", getParent().getXmlId() );
		}
		if ( roundAsInt ) {
			attrs.put("length", ""+Math.round(getBranchLength()));
		}
		else {
			attrs.put("length", ""+getBranchLength());
		}
		return createElement(tagName,attrs,getDocument());
	}	
	
	/**
	 * @param args
	 * @param key
	 */
	private void process(HashMap args,String key) {
		if ( args.containsKey(key) ) {
			if ( args.get(key) instanceof Visitor ) {
				((Visitor)args.get(key)).visit(this);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Base#finalize()
	 */
	protected void finalize() throws Throwable {
	  //do finalization here
		taxaMediator.removeLink(-1, this.getId());
	  super.finalize(); //not necessary if extending Object.
	} 	

}
