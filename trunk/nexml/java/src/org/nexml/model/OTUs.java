package org.nexml.model;

import java.util.List;
import java.util.Set;

public interface OTUs extends Iterable<OTU>, NexmlWritable {
	OTU createOTU();
	
	// unmodifiable
	List<OTU> getOTUsFromSet(String setName);

	Set<String> getSubsetNames();
	
	void createOTUSubset(String setName);

	void addOTUToSubset(String setName, OTU otu);

	void removeOTUFromSubset(String setName, OTU otu);

	void addAnnotationToSet(String setName, Annotation annotation);
	
	void removeAnnotationFromSet(Annotation annotation);
	
	void removeOTU(OTU otu);

	List<OTU> getAllOTUs();
}
