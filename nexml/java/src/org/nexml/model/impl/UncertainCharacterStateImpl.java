package org.nexml.model.impl;

import org.nexml.model.UncertainCharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class UncertainCharacterStateImpl extends CompoundCharacterStateImpl
		implements UncertainCharacterState {

	public UncertainCharacterStateImpl(Document document) {
		super(document);
	}

	public UncertainCharacterStateImpl(Document document,Element element) {
		super(document,element);
	}	
	
	@Override
	String getTagName() {
		return "uncertain_state_set";
	}
}
