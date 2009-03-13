package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nexml.model.Character;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class MatrixImpl<T> extends OTUsLinkableImpl<Character> implements
		Matrix<T> {
	private Element mFormatElement;
	private Element mMatrixElement;	
	
	public MatrixImpl(Document document) {
		super(document);
	}
	
	public MatrixImpl(Document document,Element element) {
		super(document,element);
	}	

	private final Map<OTU, Map<Character, MatrixCellImpl<T>>> mMatrixCells = new HashMap<OTU, Map<Character, MatrixCellImpl<T>>>();

	@Override
	String getTagName() {
		return getTagNameClass();
	}
	
	static String getTagNameClass() {
		return "characters";
	}	

	public List<MatrixCell<T>> getColumn(Character character) {
		List<MatrixCell<T>> column = new ArrayList<MatrixCell<T>>();
		for (Map<Character, MatrixCellImpl<T>> characterToMatrixCell : mMatrixCells
				.values()) {
			column.add(characterToMatrixCell.get(character));
		}
		return column;
	}

	public List<MatrixCell<T>> getRow(OTU otu) {
		Map<Character, MatrixCellImpl<T>> charsToCells = mMatrixCells.get(otu);
		List<MatrixCell<T>> row = new ArrayList<MatrixCell<T>>();
		for (Character character : getThings()) {
			row.add(charsToCells.get(character));
		}
		return row;
	}
	
	/**
	 * This method is necessary because cell elements actually
	 * go inside row elements, which are linked to otu elements
	 * by id references. XXX This implementation is inefficient
	 * because it scans the matrix elements for child row elements 
	 * with the otu's id. If none is found, a new one is created.
	 * There are at least two better implementations: i) we
	 * do create Row objects; ii) we do the lookup through a 
	 * HashMap @author rvosa
	 * @param otu
	 * @return
	 */
	protected Element getRowElement(OTU otu) {
		Element rowElement = null;
		NodeList rows = getMatrixElement().getElementsByTagName("row");
		for ( int i = 0; i < rows.getLength(); i++ ) {
			if ( ((Element)rows.item(i)).getAttribute("otu").equals(otu.getId()) ) {
				rowElement = (Element)rows.item(i);
			}
		}
		if ( null == rowElement ) {
			rowElement = getDocument().createElement("row");
			rowElement.setAttribute("otu", otu.getId());
			rowElement.setAttribute("id", "a" + UUID.randomUUID());
			getMatrixElement().appendChild(rowElement);
		}
		return rowElement;
	}

	/**
	 * This method creates a lot out of thin air:
	 * - if no matrix element exists, it is created
	 * - if no row element for otu exists, it is created
	 * - if no cell element for otu and char exists, it is created
	 * @author rvosa
	 */
	public MatrixCell<T> getCell(OTU otu, Character character) {
		if (!mMatrixCells.containsKey(otu)) {
			mMatrixCells.put(otu, new HashMap<Character, MatrixCellImpl<T>>());
		}
		MatrixCellImpl<T> matrixCell = mMatrixCells.get(otu).get(character);
		if (null == matrixCell) {
			matrixCell = new MatrixCellImpl<T>(getDocument());
			if ( null == getMatrixElement() ) {
				setMatrixElement( getDocument().createElement("matrix") );
				getElement().appendChild( getMatrixElement() );
			}
			getRowElement(otu).appendChild(matrixCell.getElement());
			matrixCell.getElement().setAttribute("char", character.getId());
			matrixCell.getElement().removeAttribute("id");
			Map<Character, MatrixCellImpl<T>> row = mMatrixCells.get(otu);
			row.put(character, matrixCell);
		}
		return matrixCell;
	}	
	
	protected void setCell(OTU otu, Character character, MatrixCellImpl<T> matrixCell) {
		if (!mMatrixCells.containsKey(otu)) {
			mMatrixCells.put(otu, new HashMap<Character, MatrixCellImpl<T>>());
		}
		mMatrixCells.get(otu).put(character, matrixCell);
	}
	
	protected Element getFormatElement() {
		if ( null == mFormatElement ) {
			Element format = getDocument().createElement("format");
			getElement().insertBefore(format, getElement().getFirstChild());
			setFormatElement(format);
		}
		return mFormatElement;
	}
	
	protected void setFormatElement(Element formatElement) {
		mFormatElement = formatElement;
	}
	
	protected Element getMatrixElement() {
		return mMatrixElement;
	}
	
	protected void setMatrixElement(Element matrixElement) {
		mMatrixElement = matrixElement;
	}
	
	protected Character getCharacterByIndex(int i) {
		return getThings().get(i);
	}

	public void removeCharacter(Character character) {
		removeThing(character);
		for (Map<Character, MatrixCellImpl<T>> characterToMatrixCell : mMatrixCells
				.values()) {
			characterToMatrixCell.remove(character);
		}
	}
}
