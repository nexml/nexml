package org.nexml.model;

import java.util.List;
import java.util.Set;

public interface Segmented<T> extends Iterable<T>, NexmlWritable {
	T createSegment();
	
	// unmodifiable
	List<T> getSegmentsFromSet(String setName);

	Set<String> getSetNames();
	
	void createSegmentSet(String setName);

	void addSegmentToSet(String setName, T segment);

	void removeSegmentFromSet(String setName, T segment);

	void addDictionaryToSet(String setName, Dictionary dictionary);
	
	void removeDictionaryFromSet(String setName);
	
	void addSegment(T segment);

	void removeSegment(T segment);

	List<T> getAllSegments();
}
