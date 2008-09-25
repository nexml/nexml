package org.biophylo.Taxa;
import org.biophylo.Mediators.TaxaMediator;
import org.biophylo.Util.*;
import org.biophylo.Listable;
import org.biophylo.Forest.*;
import java.util.*;

public class Taxa extends Listable {
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	private static Logger logger = Logger.getInstance();
	
	public Taxa () {
		super();
		this.type = CONSTANT.TAXA;
		this.container = CONSTANT.NONE;
		this.tag = "otus";
	}
	
	public void setForest(Forest forest) {
		forest.setTaxa(this);
	}
	
	public void unsetForest(Forest forest) {
		forest.unsetTaxa();
	}
	
	public Forest[] getForests () {
		Vector tl = taxaMediator.getLink(this.getId(), CONSTANT.FOREST);
		for ( int i = 0; i < tl.size(); i++ ) {
			logger.debug(""+tl.get(i));
		}
		Forest[] f = new Forest[tl.size()];
		tl.copyInto(f);
		return f;
	}
	
	public int getNtax() {
		return this.getEntities().length;
	}
	
}
