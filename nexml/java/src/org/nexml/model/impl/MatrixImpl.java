package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexml.model.Character;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.OTU;

class MatrixImpl<T> extends OTUsLinkableImpl<Character> implements
		Matrix<T> {

	private final Map<OTU, Map<Character, MatrixCell<T>>> mMatrixCells = new HashMap<OTU, Map<Character, MatrixCell<T>>>();

	@Override
	String getTagName() {
		return "characters";
	}

	public List<MatrixCell<T>> getColumn(Character character) {
		List<MatrixCell<T>> column = new ArrayList<MatrixCell<T>>();
		for (Map<Character, MatrixCell<T>> characterToMatrixCell : mMatrixCells
				.values()) {
			column.add(characterToMatrixCell.get(character));
		}
		return column;
	}

	public List<MatrixCell<T>> getRow(OTU otu) {
		Map<Character, MatrixCell<T>> charsToCells = mMatrixCells.get(otu);
		List<MatrixCell<T>> row = new ArrayList<MatrixCell<T>>();
		for (Character character : getThings()) {
			row.add(charsToCells.get(character));
		}
		return row;
	}

	public MatrixCell<T> getCell(OTU otu, Character character) {
		if (!mMatrixCells.containsKey(otu)) {
			mMatrixCells.put(otu, new HashMap<Character, MatrixCell<T>>());
		}
		MatrixCell<T> matrixCell = mMatrixCells.get(otu).get(character);
		if (null == matrixCell) {
			matrixCell = new MatrixCellImpl<T>();
			Map<Character, MatrixCell<T>> row = mMatrixCells.get(otu);
			row.put(character, matrixCell);
		}
		return matrixCell;
	}

	public Character createCharacter() {
		Character character = new CharacterImpl();
		addThing(character);
		return character;
	}

	public void removeCharacter(Character character) {
		removeThing(character);
		for (Map<Character, MatrixCell<T>> characterToMatrixCell : mMatrixCells
				.values()) {
			characterToMatrixCell.remove(character);
		}
	}
}
