package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexml.model.Character;
import org.nexml.model.MatrixCell;
import org.nexml.model.MatrixRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MatrixRowImpl<T> extends OTULinkableImpl implements MatrixRow<T> {
	private Map<Character,T> mStateForCharacter = new HashMap<Character,T>();
	private Map<Character,MatrixCellImpl<T>> mCellForCharacter = new HashMap<Character,MatrixCellImpl<T>>();
	private List<MatrixCellImpl<T>> mMatrixCell = new ArrayList<MatrixCellImpl<T>>();
	private Element mSeqElement = null;
	private MatrixImpl<T> mMatrix = null;
	
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
	protected MatrixRowImpl(Document document) {
		super(document);
	}
	
	protected MatrixRowImpl(Document document,MatrixImpl<T> matrix) {
		super(document);
		mMatrix = matrix;
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
	protected MatrixRowImpl(Document document, Element element) {
		super(document, element);
		List<Element> seqElements = getChildrenByTagName(element, "seq");
		if ( ! seqElements.isEmpty() ) {
			mSeqElement = seqElements.get(0);
		}
		List<Element> cellElements = getChildrenByTagName(element, "cell");
		for ( Element cellElement : cellElements ) {
			MatrixCellImpl<T> cell = new MatrixCellImpl<T>(getDocument(),cellElement);
			mMatrixCell.add(cell);
		}
	}	

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Segmented#getSegmentCount()
	 */
	@Override
	public int getSegmentCount() {
		return mStateForCharacter.keySet().size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Segmented#getSegment(int)
	 */
	@Override
	public T getSegment(int index) {
		return mStateForCharacter.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MatrixRow#getCell(org.nexml.model.Character)
	 */
	@Override
	public MatrixCell<T> getCell(org.nexml.model.Character character) {
		MatrixCellImpl<T> cell;
		if ( mCellForCharacter.containsKey(character) ) {
			cell = mCellForCharacter.get(character);
		}
		else {
			cell = new MatrixCellImpl<T>(getDocument());
			mCellForCharacter.put(character, cell);
			attachFundamentalDataElement(cell.getElement());
			cell.getElement().setAttribute("char", character.getId());
		}
		if ( mStateForCharacter.containsKey(character) ) {
			T state = mStateForCharacter.get(character);
			cell.setValue(state);
		}
		getMatrix().setCompact(false);
		return cell;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MatrixRow#getCells()
	 */
	@Override
	public List<MatrixCell<T>> getCells(List<org.nexml.model.Character> characterList) {
		List<MatrixCell<T>> cells = new ArrayList<MatrixCell<T>>();
		for ( org.nexml.model.Character character : characterList ) {
			cells.add(getCell(character));
		}
		return cells;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MatrixRow#setSeq(java.util.List)
	 */
	@Override
	public void setSeq(String seqString) {
		getSeqElement().setTextContent(seqString);
	}
	
	protected Element getSeqElement() {
		if ( null == mSeqElement ) {
			List<Element> seqElements = getChildrenByTagName(getElement(), "seq");
			if ( seqElements.isEmpty() ) {
				Element seqElement = getDocument().createElementNS(DEFAULT_NAMESPACE,"seq");
				getElement().appendChild(seqElement);
				setSeqElement(seqElement);
			}
			else if ( seqElements.size() == 1 ) {
				setSeqElement(seqElements.get(0));
			}
			else {
				throw new RuntimeException("Too many seq elements");
			}			
		}
		getMatrix().setCompact(true);
		return mSeqElement;
	}

	private void setSeqElement(Element seqElement) {
		mSeqElement = seqElement;		
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}

	public static String getTagNameClass() {
		return "row";
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MatrixRow#removeCharacter(org.nexml.model.Character)
	 */
	@Override
	public void removeCharacter(Character character) {
		mStateForCharacter.remove(character);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.MatrixRow#setCell(org.nexml.model.MatrixCell, org.nexml.model.Character)
	 */
	@Override
	public void setCell(MatrixCell<T> matrixCell, Character character) {
		mStateForCharacter.put(character,matrixCell.getValue());
		getMatrix().setCompact(false);
	}

	@Override
	public String getSeq() {
		return getSeqElement().getTextContent();
	}
	
	private MatrixImpl<T> getMatrix() {
		return mMatrix;
	}

}
