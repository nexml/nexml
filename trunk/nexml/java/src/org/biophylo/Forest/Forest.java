package org.biophylo.Forest;
import org.biophylo.*;
import org.biophylo.Mediators.TaxaMediator;
import org.biophylo.Util.*;
import org.biophylo.Taxa.*;
public class Forest extends Listable implements TaxaLinker {
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	public Forest () {
		super();
		this.type = CONSTANT.FOREST;
		this.container = CONSTANT.NONE;
		this.tag = "trees";
	}
	public String toNewick() {
		StringBuffer sb = new StringBuffer();
		Containable[] trees = this.getEntities();
		for ( int i = 0; i < trees.length; i++ ) {
			sb.append(((Tree)trees[i]).toNewick());
			sb.append("\n");
		}
		return sb.toString();
	}
	public void setTaxa(Taxa taxa) {
		taxaMediator.setLink(taxa.getId(), this.getId());
	}
	public void unsetTaxa() {
		taxaMediator.removeLink(-1, this.getId());
	}
	public Taxa getTaxa () {
		return (Taxa)taxaMediator.getLink(this.getId());
	}
}
