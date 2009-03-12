package org.nexml.model;

import java.util.List;

/**
 * @param <T> should be either a {@code Double} or {@CharacterState}.
 */
public interface Matrix<T> extends OTUsLinkable {
	List<MatrixCell<T>> getRow(OTU otu);

	List<MatrixCell<T>> getColumn(Character character);

	MatrixCell<T> getCell(OTU otu, Character character);
	
	void removeCharacter(Character character);
	
}
