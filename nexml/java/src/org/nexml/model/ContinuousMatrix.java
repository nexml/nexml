package org.nexml.model;

public interface ContinuousMatrix extends Matrix<Double> {
	/**
	 * This method creates a char element, i.e. a column
	 * definition. Because for continuous matrices the 
	 * column definition doesn't (can't) refer to a state
	 * set, this interface is much simpler than that for
	 * CategoricalMatrix.
	 * @author rvosa
	 * @return
	 */
	Character createCharacter();
}
