package org.nexml.model;

import java.net.URI;
import java.util.List;

public interface Document extends Annotatable {
    
	/**
	 * Creates a tree block associated with the
	 * provided OTUs (i.e. any nodes in any trees and
	 * networks inside the block that reference OTU objects,
	 * must refer to members of the provided OTUs object)
	 * @param otus an OTUs block to refer to
	 * @return a newly created tree block
	 */
	TreeBlock createTreeBlock(OTUs otus);

	/**
	 * Creates a new taxa block
	 * @return a newly created taxa block
	 */
	OTUs createOTUs();

	/**
	 * Creates a new categorical character matrix, i.e.
	 * a matrix of type Standard*. All rows in the matrix
	 * must refer to an OTU from the provided taxa block
	 * @param otus a taxa block
	 * @return a character state matrix
	 */
	CategoricalMatrix createCategoricalMatrix(OTUs otus);
	
	/**
	 * Creates a new continuous character matrix, i.e.
	 * a matrix of type Continuous*. All rows in the matrix
	 * must refer to an OTU from the provided taxa block
	 * @param otus a taxa block
	 * @return a character state matrix
	 */	
	ContinuousMatrix createContinuousMatrix(OTUs otus);

	/**
	 * Creates a new molecular character matrix, i.e.
	 * a matrix of type DNA|RNA|Protein*. All rows in the matrix
	 * must refer to an OTU from the provided taxa block
	 * @param otus a taxa block
	 * @return a character state matrix
	 */	
	MolecularMatrix createMolecularMatrix(OTUs otus, String type);

	/**
	 * Returns a list of all taxa blocks inside the document.
	 */
	List<OTUs> getOTUsList();
	
	/**
	 * Returns a list of all matrices inside the document.
	 */
	List<Matrix<?>> getMatrices();

	/**
	 * Returns a list of all tree blocks inside the document.
	 */
	List<TreeBlock> getTreeBlockList();

	/**
	 * Returns a NeXML serialization of the document.
	 */
	String getXmlString();
	
	/**
	 * 
	 * @param baseURI
	 */
	void setBaseURI(URI baseURI);
	URI getBaseURI();

}
