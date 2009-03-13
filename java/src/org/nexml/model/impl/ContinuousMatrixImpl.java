package org.nexml.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.nexml.model.Character;
import org.nexml.model.ContinuousMatrix;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class ContinuousMatrixImpl extends MatrixImpl<Double> implements ContinuousMatrix {

	public ContinuousMatrixImpl(Document document, Element item, OTUsImpl otus) {
		super(document,item);
		Map<String, OTU> originalOTUIds = otus.getOriginalOTUIds();
		NodeList children = item.getChildNodes();
		Map<String,Character> charactersById = null;
		for ( int i = 0; i < children.getLength(); i++ ) {
			String localName = children.item(i).getNodeName();
			if ( null != localName && localName.equals("format") ) {
				charactersById = processCharacters((Element)children.item(i));
			}
			if ( null != localName && localName.equals("matrix") ) {
				processMatrix((Element)children.item(i),originalOTUIds, charactersById);
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
	
	private void processMatrix(Element matrix,Map<String, OTU> originalOTUIds,Map<String,Character> charactersById) {
		setMatrixElement(matrix);
		for ( Element rowElement : getChildrenByTagName(matrix, "row") ) {
			OTU otu = originalOTUIds.get(rowElement.getAttribute("otu"));
			rowElement.setAttribute("otu", otu.getId());
			for ( Element cellElement : getChildrenByTagName(rowElement, "cell") ) {
				MatrixCellImpl<Double> matrixCell = new MatrixCellImpl<Double>(getDocument(),cellElement);
				setCell(otu, charactersById.get(cellElement.getAttribute("char")), matrixCell);
				cellElement.setAttribute("char", charactersById.get(cellElement.getAttribute("char")).getId());
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

	
	public ContinuousMatrixImpl(Document document) {
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

}
