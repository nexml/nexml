package org.nexml.model;

import java.util.Set;

public interface CompoundCharacterState extends CharacterState {
	Set<CharacterState> getStates();

	void setStates(Set<CharacterState> characterStates);
}
