package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CategoricalCharacterState;
import org.nexml.model.CategoricalCharacterStateSet;
import org.nexml.model.CategoricalMatrix;

public class CategoricalMatrixImpl extends
		MatrixImpl<CategoricalCharacterState> implements CategoricalMatrix {
	private Set<CategoricalCharacterStateSet> mCharacterStateSets;

	public CategoricalCharacterStateSet createCategoricalCharacterStateSet() {
		CategoricalCharacterStateSet characterStateSet = new CategoricalCharacterStateSetImpl();
		mCharacterStateSets.add(characterStateSet);
		return characterStateSet;
	}
}
