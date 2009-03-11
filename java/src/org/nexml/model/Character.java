package org.nexml.model;

public interface Character extends NexmlWritable {
	CharacterStateSet getCharacterStateSet();

	void setCharacterStateSet(CharacterStateSet characterStateSet);
}
