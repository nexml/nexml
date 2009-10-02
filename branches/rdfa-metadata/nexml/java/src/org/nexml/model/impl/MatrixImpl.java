package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.nexml.model.Character;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract class MatrixImpl<T> extends OTUsLinkableImpl<Character> implements
		Matrix<T> {
	private Element mFormatElement;
	private Element mMatrixElement;	
	private String mType;
	
    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
     * @param document a DOM document object
     * @author rvosa
     */
	protected MatrixImpl(Document document) {
		super(document);
	}
	
    /**
     * Protected constructors are intended for recursive parsing, i.e.
     * starting from the root element (which maps onto DocumentImpl) we
     * traverse the element tree such that for every child element that maps
     * onto an Impl class the containing class calls that child's protected
     * constructor, passes in the element of the child. From there the 
     * child takes over, populates itself and calls the protected 
     * constructors of its children. These should probably be protected
     * because there is all sorts of opportunity for outsiders to call
     * these in the wrong context, passing in the wrong elements etc.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
	protected MatrixImpl(Document document,Element element) {
		super(document,element);
	}	

	private final Map<OTU, Map<Character, MatrixCellImpl<T>>> mMatrixCells = new HashMap<OTU, Map<Character, MatrixCellImpl<T>>>();
	private boolean mCompact;

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}
	
	static String getTagNameClass() {
		return "characters";
	}	

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Matrix#getColumn(org.nexml.model.Character)
	 */
	public List<MatrixCell<T>> getColumn(Character character) {
		List<MatrixCell<T>> column = new ArrayList<MatrixCell<T>>();
		for (Map<Character, MatrixCellImpl<T>> characterToMatrixCell : mMatrixCells
				.values()) {
			column.add(characterToMatrixCell.get(character));
		}
		return column;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Matrix#getRow(org.nexml.model.OTU)
	 */
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
			identify(rowElement,true);
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
	
	protected MatrixCell<T> createMatrixCell(OTU otu,Character character,Element element) {
		MatrixCellImpl<T> cell = new MatrixCellImpl<T>(getDocument(),element);
		this.setCell(otu, character, cell);
		return cell;
	}	
	
	protected Element getFormatElement() {
		if ( null == mFormatElement ) {
			List<Element> formatElements = getChildrenByTagName(getElement(), "format");
			if ( formatElements.isEmpty() ) {
				Element format = getDocument().createElement("format");
				getElement().insertBefore(format, getMatrixElement());
				setFormatElement(format);
			}
			else if ( formatElements.size() == 1 ) {
				setFormatElement(formatElements.get(0));
			}
			else {
				throw new RuntimeException("Too many format elements");
			}
		}
		return mFormatElement;
	}
	
	protected void setFormatElement(Element formatElement) {		
		mFormatElement = formatElement;
	}
	
	protected Element getMatrixElement() {
		if ( null == mMatrixElement ) {
			List<Element> matrixElements = getChildrenByTagName(getElement(), "matrix");
			if ( matrixElements.isEmpty() ) {
				Element matrix = getDocument().createElement("matrix");
				getElement().appendChild(matrix);
				setMatrixElement(matrix);
			}
			else if ( matrixElements.size() == 1 ) {
				setMatrixElement(matrixElements.get(0));
			}
			else {
				throw new RuntimeException("Too many matrix elements");
			}			
		}
		return mMatrixElement;
	}
	
	protected void setMatrixElement(Element matrixElement) {
		mMatrixElement = matrixElement;
	}
	
	protected Character getCharacterByIndex(int i) {
		return getThings().get(i);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Matrix#removeCharacter(org.nexml.model.Character)
	 */
	public void removeCharacter(Character character) {
		removeThing(character);
		for (Map<Character, MatrixCellImpl<T>> characterToMatrixCell : mMatrixCells
				.values()) {
			characterToMatrixCell.remove(character);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Matrix#getCharacters()
	 */
    public List<Character> getCharacters() {
        return getThings();
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Matrix#setSeq(java.lang.String, org.nexml.model.OTU)
     */
    public void setSeq(String seq,OTU otu) {
    	// check if we're switching types    	
    	if ( ! mCompact ) {
    		setType(getType(),true);
    		System.err.println("Warning: unchecked conversion from Cells to Seqs matrix - do this only on matrices w/o cells");
    	}    	
    	Element row = getRowElement(otu);
    	
    	// remove all old seq and cell elements (if any),
    	// but keep any other old nodes
    	List<Element> oldElements = getChildrenByTagName(row, "cell");
    	oldElements.addAll(getChildrenByTagName(row, "seq"));
    	for ( Element oldElement : oldElements ) {
    		row.removeChild(oldElement);
    	}
    	
    	// then, create a new seq child element
    	Element seqElement = getDocument().createElement("seq");
    	seqElement.setTextContent(seq);
    	row.appendChild(seqElement);    	
    }

	protected String getType() {
		return mType;
	}

	protected void setType(String type, boolean compact) {
		mType = type;
		mCompact = compact;
		String subType = compact ? "Seqs" : "Cells";
		getElement().setAttributeNS(XSI_NS, XSI_TYPE, NEX_PREFIX+":"+type+subType );	
	}
	
	protected void setType(String type) {
		setType(type,false);
	}	
	
}
