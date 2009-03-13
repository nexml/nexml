package org.nexml.model;

import java.util.List;
import java.util.Set;

public interface OTUs extends Iterable<OTU>, NexmlWritable {
	OTU createOTU();

	/**
	 * An unmodifiable view of the subset named {@code subsetName}.
	 * 
	 * @param setName see description.
	 * @return see description.
	 */
	List<OTU> getOTUsFromSubset(String subsetName);

	Set<String> getSubsetNames();

	void createOTUSubset(String subsetName);

	void addOTUToSubset(String subsetName, OTU otu);

	void addAnnotationToSubset(String subsetName, Annotation annotation);

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
