package org.nexml.model;

public interface CategoricalMatrix extends Matrix<CategoricalCharacterState> {
	CategoricalCharacterStateSet createCategoricalCharacterStateSet();
}
