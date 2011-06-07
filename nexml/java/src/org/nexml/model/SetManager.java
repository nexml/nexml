package org.nexml.model;

import java.util.Collection;
import java.util.Set;

public interface SetManager {
	/**
	 * Returns all the subsets attached the object. The function
	 * of these subsets is best understood as being similar to 
	 * taxon sets and character sets in NEXUS. 
	 * @return
	 */
	Collection<Subset> getSubsets();
	
	/**
	 * Creates a named Subset, or returns the already existing
	 * Subset by that name.
	 * @param subsetName
	 * @return
	 */
	Subset createSubset(String subsetName);
	
	/**
	 * Gets all names of the attached subsets
	 * @return
	 */
	Set<String> getSubsetNames();
	
	/**
	 * Returns the named Subset
	 * @param subsetName
	 * @return
	 */
	Subset getSubset(String subsetName);
}
