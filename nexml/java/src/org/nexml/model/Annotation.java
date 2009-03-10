package org.nexml.model;

/**
 * TODO: discuss with group. 
 */
public interface Annotation<T, U> extends NexmlWritable {
	T getKey();

	void setKey(T key);

	U getValue();

	void setValue(U value);
}
