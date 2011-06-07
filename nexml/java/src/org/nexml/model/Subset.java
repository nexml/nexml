package org.nexml.model;

import java.util.Set;

public interface Subset extends Annotatable {
	
	/**
	 * Add a thing to the Subset. For example, for
	 * a taxon set, adds a taxon to the set
	 * @param annotatable
	 */
	void addThing(Annotatable annotatable);
	
	/**
	 * Removes a thing from the Subset, for example
	 * @param annotatable
	 */
	void removeThing(Annotatable annotatable);
	
	/**
	 * Gets all the items in the Subset
	 * @return
	 */
	Set<Annotatable> getThings();
	
	/**
	 * Checks whether the provided item is in the Subset
	 * @param annotatable
	 * @return
	 */
	boolean containsThing(Annotatable annotatable);
}
