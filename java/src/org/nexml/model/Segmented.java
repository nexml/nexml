package org.nexml.model;

public interface Segmented<T> {
	/**
	 * Objects that implement this method are objects
	 * that consist of segments of the same type. E.g.
	 * for OTUs objects, these would be OTU objects. 
	 * For TreeBlock objects these are Tree and Network
	 * objects, etc. This method returns the number of
	 * such objects.
	 * @return
	 */
	int getSegmentCount();
	
	/**
	 * Objects that implement this method are objects
	 * that consist of segments of the same type. E.g.
	 * for OTUs objects, these would be OTU objects. 
	 * For TreeBlock objects these are Tree and Network
	 * objects, etc. This method returns the i'th object
	 * @return
	 */	
	T getSegment(int index);

}
