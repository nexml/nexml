package org.biophylo.Taxa;
import org.biophylo.Util.*;
import org.biophylo.*;
import org.biophylo.Forest.*;
import org.biophylo.Matrices.*;
import org.biophylo.Mediators.ObjectMediator;
import org.biophylo.Mediators.TaxaMediator;
import java.util.*;
public class Taxon extends Containable {
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	public Taxon () {
		this.type = CONSTANT.TAXON;
		this.container = CONSTANT.TAXA;
		this.tag = "otu";
	}
	public Node[] getNodes () {
		int taxonId = this.getId();
		Vector tl = taxaMediator.getLink(taxonId, CONSTANT.NODE);
		Node[] result = new Node[tl.size()];
		tl.copyInto(result);
		return result;
	}
	public void setNodes(Node node) {
		int taxonId = this.getId();
		int linkerId = node.getId();
		taxaMediator.setLink(taxonId, linkerId);
	}
	public void unsetNode(Node node) {
		int taxonId = this.getId();
		int linkerId = node.getId();
		taxaMediator.removeLink(taxonId, linkerId);		
	}	
	public Datum[] getData() {
		int taxonId = this.getId();
		Vector tl = taxaMediator.getLink(taxonId, CONSTANT.DATUM);
		Datum[] result = new Datum[tl.size()];
		tl.copyInto(result);
		return result;		
	}
	public void setData(Datum datum) {
		int taxonId = this.getId();
		int linkerId = datum.getId();
		taxaMediator.setLink(taxonId, linkerId);		
	}
	public void unsetDatum(Datum datum) {
		int taxonId = this.getId();
		int linkerId = datum.getId();
		taxaMediator.removeLink(taxonId, linkerId);			
	}
	protected void finalize() throws Throwable {
		taxaMediator.removeLink(this.getId(), -1);
	  //do finalization here
	  super.finalize(); //not necessary if extending Object.
	} 

}
