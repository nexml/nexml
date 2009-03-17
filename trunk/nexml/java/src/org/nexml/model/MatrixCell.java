package org.nexml.model;

public interface MatrixCell<T> extends NexmlWritable {
	/**
	 * Gets the value/state of the matrix cell. Either
	 * a character state object or a Double
	 * XXX perhaps create a ContinuousCharacteState, a 
	 * wrapper around Double?
	 * @return
	 */
	T getValue();

	/**
	 * Sets the value/state of the matrix cell. Either
	 * a character state object or a Double
	 * XXX perhaps create a ContinuousCharacteState, a 
	 * wrapper around Double?
	 * @param either a Double, or a CharacterState
	 */
	void setValue(T value);
}
