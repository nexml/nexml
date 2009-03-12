package org.nexml.model.impl;

import org.w3c.dom.Document;

class PolymorphicCharacterStateImpl extends CompoundCharacterStateImpl {

	public PolymorphicCharacterStateImpl(Document document) {
		super(document);
		// TODO Auto-generated constructor stub
	}

	@Override
	String getTagName() {
		return "polymorphic_state_set";
	}
}
