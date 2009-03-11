package org.nexml.model.impl;

import org.nexml.model.CategoricalCharacterState;

public class StandardCharacterStateImpl extends NexmlWritableImpl implements
		CategoricalCharacterState {

	@Override
	String getTagName() {
		return "state";
	}
}
