package org.nexml.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.nexml.model.Character;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.ContinuousMatrix;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class ContinuousMatrixImpl extends MatrixImpl<Double> implements ContinuousMatrix {

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
	protected ContinuousMatrixImpl(Document document, Element item, OTUsImpl otus) {
		super(document,item);
		NodeList children = item.getChildNodes();
		Map<String,Character> charactersById = null;
		for ( int i = 0; i < children.getLength(); i++ ) {
			String localName = children.item(i).getNodeName();
			if ( null != localName && localName.equals("format") ) {
				charactersById = processCharacters((Element)children.item(i));
			}
			if ( null != localName && localName.equals("matrix") ) {
				processMatrix((Element)children.item(i),otus, charactersById);
			}			
		}
		setOTUs(otus);
	}
	
	private Map<String,Character> processCharacters(Element format) {
		setFormatElement(format);
		Map<String,Character> result = new HashMap<String,Character>();
		for ( Element charElement : getChildrenByTagName(format, "char") ) {
			String charId = charElement.getAttribute("id");
			Character character = new CharacterImpl(getDocument(),charElement);
			result.put(charId, character);
			addThing(character);
		}	
		return result;
	}
	
	private void processMatrix(Element matrix,OTUsImpl otus,Map<String,Character> charactersById) {
		setMatrixElement(matrix);
		for ( Element rowElement : getChildrenByTagName(matrix, "row") ) {
			OTU otu = otus.getThingById(rowElement.getAttribute("otu"));
			rowElement.setAttribute("otu", otu.getId());
			for ( Element cellElement : getChildrenByTagName(rowElement, "cell") ) {
				MatrixCellImpl<Double> matrixCell = new MatrixCellImpl<Double>(getDocument(),cellElement);
				setCell(otu, charactersById.get(cellElement.getAttribute("char")), matrixCell);
				cellElement.setAttribute("char", charactersById.get(cellElement.getAttribute("char")).getId());
				matrixCell.setValue(Double.parseDouble(cellElement.getAttribute("state")));
			}
			for ( Element seq : getChildrenByTagName(rowElement, "seq")) {
				String sequence = seq.getTextContent();
				String[] states = sequence.split("\\s+");
				int k = 0;
				STATE: for ( int j = 0; j < states.length; j++ ) {
					if ( states[j].length() == 0 ) {
						continue STATE;
					}
					Character character = null;
					character = getCharacterByIndex(k);
					getCell(otu, character).setValue(Double.parseDouble(states[j]));							
					k++;
				}	
				rowElement.removeChild(seq);
			}
		}		
	}

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
	protected ContinuousMatrixImpl(Document document) {
		super(document);
	}

	/**
	 * This method creates a char element, i.e. a column definition.
	 * @author rvosa
	 */
	public Character createCharacter() {
		CharacterImpl character = new CharacterImpl(getDocument());
		addThing(character);
		getFormatElement().appendChild(character.getElement());
		return character;
	}

	public Character createCharacter(CharacterStateSet stateSet) {
		return createCharacter();
	}

	public Double parseSymbol(String symbol) {
		return Double.parseDouble(symbol);
	}

}
