package org.nexml.model;

import java.util.List;
import java.util.Set;

public interface OTUs extends Iterable<OTU>, NexmlWritable {
	OTU createOTU();
	
	// unmodifiable
	List<OTU> getOTUsFromSet(String setName);

	Set<String> getSetNames();
	
	void createOTUSet(String setName);

	void addOTUToSet(String setName, OTU otu);

	void removeOTUFromSet(String setName, OTU otu);

	void addDictionaryToSet(String setName, Dictionary dictionary);
	
	void removeDictionaryFromSet(String setName);
	
	void removeOTU(OTU otu);

	List<OTU> getAllOTUs();
}
