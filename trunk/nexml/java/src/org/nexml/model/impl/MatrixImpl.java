package org.nexml.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nexml.model.Character;
import org.nexml.model.Matrix;
import org.nexml.model.MatrixCell;
import org.nexml.model.MatrixRow;
import org.nexml.model.OTU;
import org.nexml.model.Subset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class MatrixImpl<T> extends OTUsLinkableImpl<Character> implements
		Matrix<T> {
	private Element mFormatElement;
	private Element mMatrixElement;	
	private String mType;
	private boolean mCompact;
	protected final Map<OTU,MatrixRow<T>> mMatrixRows = new HashMap<OTU,MatrixRow<T>>();
	
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
	
	protected MatrixImpl(Document document,String type) {
		super(document);
		setType(type);
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
		for (Element rowElement : getChildrenByTagName(element, "row")) {
			String otuId = rowElement.getAttribute("otu");
			OTU otu = ((OTUsImpl)getOTUs()).getThingById(otuId);
			MatrixRow<T> row = new MatrixRowImpl<T>(document, rowElement);
			row.setOTU(otu);
			mMatrixRows.put(otu, row);
		}		
	}

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
		for ( OTU otu : getOTUs() ) {
			column.add(getRowObject(otu).getCell(character));
		}
		return column;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Matrix#getRow(org.nexml.model.OTU)
	 */
	public List<MatrixCell<T>> getRow(OTU otu) {
		return getRowObject(otu).getCells(getCharacters());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Matrix#getRowObject(org.nexml.model.OTU)
	 */
	public MatrixRow<T> getRowObject(OTU otu) {
		if ( ! mMatrixRows.containsKey(otu) ) {
			MatrixRowImpl<T> matrixRow = new MatrixRowImpl<T>(getDocument(),this);
			matrixRow.setOTU(otu);
			attachFundamentalDataElement(getMatrixElement(), matrixRow.getElement());
			mMatrixRows.put(otu, matrixRow);
		}
		return mMatrixRows.get(otu);
	}	

	/**
	 * This method creates a lot out of thin air:
	 * - if no matrix element exists, it is created
	 * - if no row element for otu exists, it is created
	 * - if no cell element for otu and char exists, it is created
	 * @author rvosa
	 */
	public MatrixCell<T> getCell(OTU otu, Character character) {
		return getRowObject(otu).getCell(character);
	}	
	
	protected void setCell(OTU otu, Character character, MatrixCellImpl<T> matrixCell) {
		getRowObject(otu).setCell(matrixCell,character);
	}
	
	protected MatrixCell<T> createMatrixCell(OTU otu,Character character,Element element) {
		MatrixCellImpl<T> cell = new MatrixCellImpl<T>(getDocument(),element);
		setCell(otu, character, cell);
		return cell;
	}	
	
	protected Element getFormatElement() {
		if ( null == mFormatElement ) {
			List<Element> formatElements = getChildrenByTagName(getElement(), "format");
			if ( formatElements.isEmpty() ) {
				Element format = getDocument().createElementNS(DEFAULT_NAMESPACE,"format");
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
	
	public Subset createSubset(String subsetName) {
		return createSubset(subsetName, getFormatElement());
	}
	
	protected void setFormatElement(Element formatElement) {		
		mFormatElement = formatElement;
	}
	
	protected Element getMatrixElement() {
		if ( null == mMatrixElement ) {
			List<Element> matrixElements = getChildrenByTagName(getElement(), "matrix");
			if ( matrixElements.isEmpty() ) {
				Element matrix = getDocument().createElementNS(DEFAULT_NAMESPACE,"matrix");
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
		for ( OTU row : getOTUs() ) {
			getRowObject(row).removeCharacter(character);
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
    	getRowObject(otu).setSeq(seq);   	
    }
    
    abstract String getSplitString();

	protected String getType() {
		return mType;
	}

	protected void setType(String type, boolean compact) {
		mType = type;
		mCompact = compact;
		String subType = compact ? "Seqs" : "Cells";
		getElement().setAttributeNS(XSI_URI, XSI_TYPE, NEX_PRE+":"+type+subType );	
	}
	
	protected void setType(String type) {
		setType(type,true);
	}
	
	public int getSegmentCount() {
		return getCharacters().size();
	}
	
	public Character getSegment(int index) {
		return getCharacters().get(index);
	}
	
	@Override
	protected Set<String> getPermissibleSetContents() {
		Set<String> permissibleSetContents = new HashSet<String>();
		permissibleSetContents.add("char");
		return permissibleSetContents;
	}
	
	protected boolean getCompact() {
		return mCompact;
	}
	
	protected void setCompact(boolean compact) {
		mCompact = compact;
		setType(mType,mCompact);
	}
	
}
