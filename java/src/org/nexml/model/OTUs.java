package org.nexml.model;

import java.util.List;
import java.util.Set;

/**
 * An OTU set.
 */
public interface OTUs extends Annotatable, Iterable<OTU> {

	/**
	 * Create a new {@code OTUs}.
	 * 
	 * @return a new {@code OTUs}.
	 */
	OTU createOTU();


	/**
	 * An unmodifiable view of the subset named {@code subsetName}.
	 * 
	 * @param setName see description.
	 * @return see description.
	 */
	List<OTU> getOTUsFromSubset(String subsetName);

	/**
	 * Get all subset names.
	 * 
	 * @return all subset names.
	 */
	Set<String> getSubsetNames();

	/**
	 * Create a subset named {@code subsetName}.
	 * 
	 * @param subsetName see description.
	 */
	void createOTUSubset(String subsetName);

	/**
	 * Add {@code otu} to the subset named {@code subsetName}.
	 * 
	 * @param subsetName see description.
	 * @param otu see description.
	 */
	void addOTUToSubset(String subsetName, OTU otu);

	/**
	 * Add {@code annotation} to the subset named {@code subsetName}.
	 * 
	 * @param subsetName see description.
	 * @param annotation see description.
	 */
	void addAnnotationToSubset(String subsetName, Annotation annotation);
	
	/**
	 * Removes specified annotation from the taxon subset
	 * @param annotation
	 */
	void removeAnnotationFromSubset(Annotation annotation);
	
	/**
	 * Remove {@code otu} from the subset named {@code subsetName}.
	 * 
	 * @param subsetName see description.
	 * @param otu see description.
	 */
	void removeOTUFromSubset(String subsetName, OTU otu);

	/**
	 * Remove {@code otu} from this {@code OTUs}.
	 * 
	 * @param otu the {@code OTU} we're removing.
	 */
	void removeOTU(OTU otu);

	/**
	 * Get an unmodifiable view of the contained {@code OTU}s.
	 * 
	 * @return an unmodifiable view of the contained {@code OTU}s.
	 */

	List<OTU> getAllOTUs();
}
