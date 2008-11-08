package org.biophylo.taxa;
import org.biophylo.mediators.TaxaMediator;
import org.biophylo.util.*;
import org.biophylo.Listable;
import org.biophylo.forest.*;
import java.util.*;

public class Taxa extends Listable {
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	private static Logger logger = Logger.getInstance();
	
	/**
	 * 
	 */
	public Taxa () {
		super();
		mType = CONSTANT.TAXA;
		mContainer = CONSTANT.PROJECT;
		mTag = "otus";
	}
	
	/**
	 * @param forest
	 */
	public void setForest(Forest forest) {
		forest.setTaxa(this);
	}
	
	/**
	 * @param forest
	 */
	public void unsetForest(Forest forest) {
		forest.unsetTaxa();
	}
	
	/**
	 * @return
	 */
	public Forest[] getForests () {
		Vector tl = taxaMediator.getLink(this.getId(), CONSTANT.FOREST);
		for ( int i = 0; i < tl.size(); i++ ) {
			logger.debug(""+tl.get(i));
		}
		Forest[] f = new Forest[tl.size()];
		tl.copyInto(f);
		return f;
	}
	
	/**
	 * @return
	 */
	public int getNtax() {
		return this.getEntities().length;
	}
	
}
