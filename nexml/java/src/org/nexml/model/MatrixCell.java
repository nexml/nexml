package org.nexml.model;

public interface MatrixCell<T> extends NexmlWritable {
	T getValue();

	void setValue(T value);
}
