package org.nexml.model;

import java.util.List;

public interface Matrix extends OTUsLinkable {
	List<MatrixCell> getRow(OTU otu);
	List<MatrixCell> getColumn(Character character);
	MatrixCell getCell(OTU otu, Character character);
	Character createCharacter();
	void removeCharacter(Character character);
}
