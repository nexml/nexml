package org.nexml.model;

import java.util.Set;

public interface CharacterStateSet<T extends CharacterState> extends
		NexmlWritable {
	Set<T> getCharacterStates();

	void setCharacterStates(Set<T> characterStates);

	T createCharacterState();
}
