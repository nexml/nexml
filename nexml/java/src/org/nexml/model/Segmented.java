package org.nexml.model;

public interface Segmented<T> {
	int getSegmentCount();
	T getSegment(int index);

}
