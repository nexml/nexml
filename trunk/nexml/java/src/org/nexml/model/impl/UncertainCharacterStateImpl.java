package org.nexml.model.impl;

import org.nexml.model.UncertainCharacterState;
import org.w3c.dom.Document;

class UncertainCharacterStateImpl extends CompoundCharacterStateImpl
		implements UncertainCharacterState {

	public UncertainCharacterStateImpl(Document document) {
		super(document);
	}

	@Override
	String getTagName() {
		return "uncertain_state_set";
	}
}
