package org.nexml.model.impl;

import org.nexml.model.CategoricalCharacterState;

public class CategoricalCharacterStateImpl extends NexmlWritableImpl implements
		CategoricalCharacterState {

	@Override
	String getTagName() {
		return "state";
	}

}
