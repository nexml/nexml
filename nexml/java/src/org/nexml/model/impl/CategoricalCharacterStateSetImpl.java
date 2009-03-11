package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CategoricalCharacterState;
import org.nexml.model.CategoricalCharacterStateSet;

public class CategoricalCharacterStateSetImpl extends NexmlWritableImpl
		implements CategoricalCharacterStateSet {

	@Override
	String getTagName() {
		return "states";
	}

	public Set<CategoricalCharacterState> getCharacterStates() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCharacterStates(
			Set<CategoricalCharacterState> characterStates) {
		// TODO Auto-generated method stub

	}

	public CategoricalCharacterState createCategoricalCharacterState() {
		return new CategoricalCharacterStateImpl();
	}

	public CategoricalCharacterState createCharacterState() {
		// TODO Auto-generated method stub
		return null;
	}

}
