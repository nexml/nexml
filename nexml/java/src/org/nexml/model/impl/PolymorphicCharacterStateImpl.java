package org.nexml.model.impl;

import org.nexml.model.PolymorphicCharacterState;
import org.w3c.dom.Document;

class PolymorphicCharacterStateImpl extends CompoundCharacterStateImpl implements PolymorphicCharacterState {

	public PolymorphicCharacterStateImpl(Document document) {
		super(document);
	}

	@Override
	String getTagName() {
		return "polymorphic_state_set";
	}
}
