package org.nexml.model.impl;

import org.nexml.model.PolymorphicCharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class PolymorphicCharacterStateImpl extends CompoundCharacterStateImpl implements PolymorphicCharacterState {

	public PolymorphicCharacterStateImpl(Document document) {
		super(document);
	}

	public PolymorphicCharacterStateImpl(Document document,Element element) {
		super(document,element);
	}	
	
	@Override
	String getTagName() {
		return "polymorphic_state_set";
	}
}
