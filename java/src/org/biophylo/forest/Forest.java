package org.biophylo.forest;
import org.biophylo.*;
import org.biophylo.mediators.TaxaMediator;
import org.biophylo.util.*;
import org.biophylo.util.exceptions.ObjectMismatch;
import org.biophylo.taxa.*;
public class Forest extends Listable implements TaxaLinker {
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	
	/**
	 * 
	 */
	public Forest () {
		super();
		this.type = CONSTANT.FOREST;
		this.container = CONSTANT.PROJECT;
		this.tag = "trees";
	}
		
	/**
	 * @return
	 */
	public String toNewick() {
		StringBuffer sb = new StringBuffer();
		Containable[] trees = this.getEntities();
		for ( int i = 0; i < trees.length; i++ ) {
			sb.append(((Tree)trees[i]).toNewick());
			sb.append("\n");
		}
		return sb.toString();
	}	
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxaLinker#setTaxa(org.biophylo.Taxa.Taxa)
	 */
	public void setTaxa(Taxa taxa) {
		taxaMediator.setLink(taxa.getId(), this.getId());
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxaLinker#unsetTaxa()
	 */
	public void unsetTaxa() {
		taxaMediator.removeLink(-1, this.getId());
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxaLinker#getTaxa()
	 */
	public Taxa getTaxa () {
		return (Taxa)taxaMediator.getLink(this.getId());
	}
}
