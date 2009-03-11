package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.OTU;

public class MatrixImpl<T> extends OTUsLinkableImpl<Character> implements
		Matrix<T> {

	private final Map<OTU, Map<Character, MatrixCell>> mMatrixCells = new HashMap<OTU, Map<Character, MatrixCell>>();

	
	@Override
	String getTagName() {
		return "characters";
	}

	public List<MatrixCell<T>> getColumn(Character character) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<MatrixCell<T>> getRow(OTU otu) {
		Map<Character, MatrixCell> charsToCells = mMatrixCells.get(otu);
		List<MatrixCell<T>> matrixCells = new ArrayList<MatrixCell<T>>();
		for (Character character : getThings()) {
			matrixCells.add(charsToCells.get(character));
		}
		return matrixCells;
	}

	public MatrixCell<T> getCell(OTU otu, Character character) {
		MatrixCell<T> matrixCell = new MatrixCellImpl<T>();
		addThing(character);
		return matrixCell;
	}

	public Character createCharacter() {
		// Character character
		// TODO Auto-generated method stub
		return null;
	}

	public void removeCharacter(Character character) {
		// TODO Auto-generated method stub
	}
}
