package org.nexml.model.impl;

import org.nexml.model.UncertainCharacterState;

class UncertainCharacterStateImpl extends CompoundCharacterStateImpl
		implements UncertainCharacterState {

	@Override
	String getTagName() {
		return "uncertain_state_set";
	}
}
