package org.biophylo.taxa;

public interface TaxonLinker {
	
	/**
	 * @return
	 */
	public Taxon getTaxon();
	
	/**
	 * @param taxon
	 */
	public void setTaxon(Taxon taxon);
	
	/**
	 * 
	 */
	public void unsetTaxon();
}
