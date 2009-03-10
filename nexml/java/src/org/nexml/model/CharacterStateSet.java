package org.nexml.model;

import java.util.Set;

public interface CharacterStateSet extends NexmlWritable {
	Set<CharacterState> getCharacterStates();
	
	void setCharacterStates(Set<CharacterState> characterStates);
}
