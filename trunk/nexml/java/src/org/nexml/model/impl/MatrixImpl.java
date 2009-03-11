package org.nexml.model.impl;

import java.util.List;

import org.nexml.model.Character;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.OTU;

public class MatrixImpl extends OTUsLinkableImpl<Character> implements Matrix { 

	@Override
	String getTagName() {
		return "characters";
	}

	public List<MatrixCell> getColumn(Character character) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<MatrixCell> getRow(OTU otu) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public MatrixCell getCell(OTU otu, Character character) { 
		MatrixCell matrixCell = new MatrixCellImpl();
		addThing(character);
		return matrixCell;
	}

	public Character createCharacter() {
		//Character character 
		// TODO Auto-generated method stub
		return null;
	}
	
	public void removeCharacter(Character character) {
		// TODO Auto-generated method stub
	}
}
