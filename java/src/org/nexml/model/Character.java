package org.nexml.model;

public interface Character extends Annotatable {
    
	CharacterStateSet getCharacterStateSet();

	void setCharacterStateSet(CharacterStateSet characterStateSet);
}
