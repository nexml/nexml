package org.nexml.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.Character;
import org.nexml.model.CharacterState;
import org.nexml.model.CharacterStateSet;
import org.nexml.model.CompoundCharacterState;
import org.nexml.model.OTU;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class CategoricalMatrixImpl extends
		MatrixImpl<CharacterState> implements CategoricalMatrix {
	
	public CategoricalMatrixImpl(Document document) {
		super(document);
	}
	
	private void createState (Class<?> subClass,Element stateElement,
			Map<CharacterStateSet,Map<String,CharacterState>> stateByStateSet,
			Map<CharacterStateSet,Map<String,CharacterState>> symbolByStateSet,
			CharacterStateSet charStateSet) {
		String stateId = stateElement.getAttribute("id");
		String symbol = stateElement.getAttribute("symbol");
		CharacterState stateObj = null;
		try {
			stateObj = (CharacterState)subClass
				.getConstructor(org.w3c.dom.Document.class,Element.class)
				.newInstance(getDocument(),stateElement);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stateByStateSet.get(charStateSet).put(stateId, stateObj);
		symbolByStateSet.get(charStateSet).put(symbol, stateObj);
		if ( stateObj instanceof CompoundCharacterState ) {
			Set<CharacterState> members = new HashSet<CharacterState>();
			for ( Element member : getChildrenByTagName( stateElement, "member" ) ) {
				String memberId = member.getAttribute("state");
				members.add(stateByStateSet.get(charStateSet).get(memberId));
				member.setAttribute("state", stateByStateSet.get(charStateSet).get(memberId).getId());
			}
			((CompoundCharacterState)stateObj).setStates(members);			
		}
	}
	
	public CategoricalMatrixImpl(Document document, Element item, OTUsImpl otus) {
		super(document, item);
		Map<String, OTU> originalOTUIds = otus.getOriginalOTUIds();
		NodeList children = item.getChildNodes();
		Map<CharacterStateSet, Map<String, CharacterState>> stateByStateSet = new HashMap<CharacterStateSet, Map<String, CharacterState>>();
		Map<CharacterStateSet, Map<String, CharacterState>> symbolByStateSet = new HashMap<CharacterStateSet, Map<String, CharacterState>>();
		Map<String, CharacterStateSet> stateSetById = new HashMap<String, CharacterStateSet>();
		Map<String, Character> characterById = new HashMap<String, Character>();
		Map<Character, CharacterStateSet> stateSetByCharacter = new HashMap<Character, CharacterStateSet>();
		for (int i = 0; i < children.getLength(); i++) {
			String localName = children.item(i).getNodeName();
			if (null != localName && localName.equals("format")) {
				this.setFormatElement((Element) children.item(i));
				for (Element stateSet : getChildrenByTagName((Element) children.item(i), "states")) {
					processStateSet(stateByStateSet, symbolByStateSet, stateSetById, stateSet);
				}
				for (Element character : getChildrenByTagName((Element) children.item(i), "char")) {
					String stateSetId = character.getAttribute("states");
					String characterId = character.getAttribute("id");
					Character characterObj = new CharacterImpl(getDocument(), character);
					characterObj.setCharacterStateSet(stateSetById.get(stateSetId));
					characterById.put(characterId, characterObj);
					stateSetByCharacter.put(characterObj, stateSetById.get(stateSetId));
					this.addThing(characterObj);
				}
			}
			if (null != localName && localName.equals("matrix")) {
				this.setMatrixElement((Element) children.item(i));
				for (Element row : getChildrenByTagName((Element) children.item(i), "row")) {
					OTU otu = originalOTUIds.get(row.getAttribute("otu"));
					row.setAttribute("otu", otu.getId());
					for (Element cell : getChildrenByTagName(row, "cell")) {
						processCell(stateByStateSet, characterById, stateSetByCharacter, otu, cell);
					}
					for ( Element seq : getChildrenByTagName(row, "seq")) {
						String sequence = seq.getTextContent();
						String[] states = null;
						if ( item.getAttribute("xsi:type").indexOf("Standard") > 0 ) {
							states = sequence.split("\\s+");
						}
						else {
							states = sequence.split("\\s*");
						}
						int k = 0;
						STATE: for ( int j = 0; j < states.length; j++ ) {
							if ( states[j].length() == 0 ) {
								continue STATE;
							}
							Character character = null;
							try {
								character = this.getCharacterByIndex(k);
							} catch (RuntimeException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							CharacterState value = symbolByStateSet.get(stateSetByCharacter.get(character)).get(states[j]);
							this.getCell(otu, character).setValue(value);							
							k++;
						}
						row.removeChild(seq);
					}
				}
			}
		}
		setOTUs(otus);
	}

	private void processStateSet(
		Map<CharacterStateSet, Map<String, CharacterState>> stateByStateSet,
		Map<CharacterStateSet, Map<String, CharacterState>> symbolByStateSet,
		Map<String, CharacterStateSet> stateSetById,
		Element stateSet) {
		String stateSetId = stateSet.getAttribute("id");
		CharacterStateSetImpl charStateSet = new CharacterStateSetImpl(getDocument(),stateSet);
		stateSetById.put(stateSetId, charStateSet);
		stateByStateSet.put(charStateSet, new HashMap<String,CharacterState>());
		symbolByStateSet.put(charStateSet, new HashMap<String,CharacterState>());
		for ( Element state : getChildrenByTagName(stateSet,"state") ) {
			createState(CharacterStateImpl.class, state, stateByStateSet, symbolByStateSet, charStateSet);
		}
		for ( Element state : getChildrenByTagName(stateSet,"uncertain_state_set") ) {
			createState(UncertainCharacterStateImpl.class, state, stateByStateSet, symbolByStateSet, charStateSet);
		}	
		for ( Element state : getChildrenByTagName(stateSet,"polymorphic_state_set") ) {
			createState(PolymorphicCharacterStateImpl.class, state, stateByStateSet, symbolByStateSet, charStateSet);						
		}
	}

	private void processCell(
		Map<CharacterStateSet, Map<String, CharacterState>> stateByStateSet,
		Map<String, Character> characterById,
		Map<Character, CharacterStateSet> stateSetByCharacter,
		OTU otu,
		Element cell) {
		String charId = cell.getAttribute("char");
		String stateId = cell.getAttribute("state");
		Character character = characterById.get(charId);
		CharacterState state = null;
		try {
			state = stateByStateSet.get(stateSetByCharacter.get(character)).get(stateId);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		MatrixCellImpl<CharacterState> cellObj = new MatrixCellImpl<CharacterState>(getDocument(),cell);
		cellObj.setValue(state);
		setCell(otu, character, cellObj);
		cellObj.getElement().setAttribute("char", character.getId());
	}	

	private Set<CharacterStateSet> mCharacterStateSets = new HashSet<CharacterStateSet>();
	private MolecularCharacterStateSetImpl mMolecularCharacterStates = null;
	
	/**
	 * This is equivalent to creating a <states> element, i.e.
	 * a container for state elements, polymorphic_state_set elements
	 * and uncertain_state_set elements. The states elements are children
	 * of the format element (to which the matrix holds a reference).
	 * If the format element object doesn't exist yet it's created here
	 * @author rvosa
	 */
	public CharacterStateSet createCharacterStateSet() {
		CharacterStateSetImpl characterStateSet = new CharacterStateSetImpl(getDocument());
		mCharacterStateSets.add(characterStateSet);
		if ( null == getFormatElement() ) {
			setFormatElement( getDocument().createElement("format") );
			getElement().insertBefore( getFormatElement(), getElement().getFirstChild() );
		}
		getFormatElement().insertBefore( characterStateSet.getElement(), getFormatElement().getFirstChild() );
		return characterStateSet;
	}

	public Set<CharacterStateSet> getCharacterStateSets() {
		return Collections.unmodifiableSet(mCharacterStateSets);
	}
	
	/**
	 * This method creates a char element, i.e. a column definition.
	 * Because NeXML requires for categorical matrices that these
	 * column definitions have an attribute to reference the 
	 * applicable state set, the state set object needs to be passed
	 * in here, from which the attribute's value is set. 
	 * @author rvosa
	 */
	public Character createCharacter(CharacterStateSet characterStateSet) {
		CharacterImpl character = new CharacterImpl(getDocument());
		addThing(character);
		character.getElement().setAttribute("states", characterStateSet.getId());
		getFormatElement().appendChild(character.getElement());
		return character;
	}	

	public CharacterStateSet getDNACharacterStateSet() {
	    if (mMolecularCharacterStates == null){
	        mMolecularCharacterStates = new MolecularCharacterStateSetImpl(getDocument());
	    }
	    CharacterStateSet result = mMolecularCharacterStates.getDNAStateSet();
	    CharacterStateSetImpl characterStateSet = (CharacterStateSetImpl)result;
	    if (mCharacterStateSets.add(characterStateSet)){
	        if ( null == getFormatElement() ) {
	            setFormatElement( getDocument().createElement("format") );
	            getElement().insertBefore( getFormatElement(), getElement().getFirstChild() );
	        }
	        getFormatElement().insertBefore( characterStateSet.getElement(), getFormatElement().getFirstChild() );
	    }
	    return result;
	}

	public CharacterStateSet getRNACharacterStateSet() {
        if (mMolecularCharacterStates == null){
            mMolecularCharacterStates = new MolecularCharacterStateSetImpl(getDocument());
        }
        CharacterStateSet result = mMolecularCharacterStates.getRNAStateSet();
        CharacterStateSetImpl characterStateSet = (CharacterStateSetImpl)result;
        if (mCharacterStateSets.add(characterStateSet)){
            if ( null == getFormatElement() ) {
                setFormatElement( getDocument().createElement("format") );
                getElement().insertBefore( getFormatElement(), getElement().getFirstChild() );
            }
            getFormatElement().insertBefore( characterStateSet.getElement(), getFormatElement().getFirstChild() );
        }
        return result;
     }
    
    public CharacterStateSet getProteinCharacterStateSet(){
        if (mMolecularCharacterStates == null){
            mMolecularCharacterStates = new MolecularCharacterStateSetImpl(getDocument());
        }
        CharacterStateSet result = mMolecularCharacterStates.getProteinStateSet();
        CharacterStateSetImpl characterStateSet = (CharacterStateSetImpl)result;
        if (mCharacterStateSets.add(characterStateSet)){
            if ( null == getFormatElement() ) {
                setFormatElement( getDocument().createElement("format") );
                getElement().insertBefore( getFormatElement(), getElement().getFirstChild() );
            }
            getFormatElement().insertBefore( characterStateSet.getElement(), getFormatElement().getFirstChild() );
        }
        return result;
    }

 }
