package org.biophylo.taxa;
import org.biophylo.util.*;
import org.biophylo.*;
import org.biophylo.forest.*;
import org.biophylo.matrices.*;
import org.biophylo.mediators.ObjectMediator;
import org.biophylo.mediators.TaxaMediator;
import java.util.*;
public class Taxon extends Containable {
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	
	/**
	 * 
	 */
	public Taxon () {
		mType = CONSTANT.TAXON;
		mContainer = CONSTANT.TAXA;
		mTag = "otu";
	}
	
	/**
	 * @return
	 */
	public Node[] getNodes () {
		int taxonId = this.getId();
		Vector tl = taxaMediator.getLink(taxonId, CONSTANT.NODE);
		Node[] result = new Node[tl.size()];
		tl.copyInto(result);
		return result;
	}
	
	/**
	 * @param node
	 */
	public void setNodes(Node node) {
		int taxonId = this.getId();
		int linkerId = node.getId();
		taxaMediator.setLink(taxonId, linkerId);
	}
	
	/**
	 * @param node
	 */
	public void unsetNode(Node node) {
		int taxonId = this.getId();
		int linkerId = node.getId();
		taxaMediator.removeLink(taxonId, linkerId);		
	}	
	
	/**
	 * @return
	 */
	public Datum[] getData() {
		int taxonId = this.getId();
		Vector tl = taxaMediator.getLink(taxonId, CONSTANT.DATUM);
		Datum[] result = new Datum[tl.size()];
		tl.copyInto(result);
		return result;		
	}
	
	/**
	 * @param datum
	 */
	public void setData(Datum datum) {
		int taxonId = this.getId();
		int linkerId = datum.getId();
		taxaMediator.setLink(taxonId, linkerId);		
	}
	
	/**
	 * @param datum
	 */
	public void unsetDatum(Datum datum) {
		int taxonId = this.getId();
		int linkerId = datum.getId();
		taxaMediator.removeLink(taxonId, linkerId);			
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Base#finalize()
	 */
	protected void finalize() throws Throwable {
		taxaMediator.removeLink(this.getId(), -1);
	  //do finalization here
	  super.finalize(); //not necessary if extending Object.
	} 

}
