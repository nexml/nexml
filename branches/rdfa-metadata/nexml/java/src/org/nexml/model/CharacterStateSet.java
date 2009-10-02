package org.nexml.model;

import java.util.Set;

public interface CharacterStateSet extends
		NexmlWritable {
	Set<CharacterState> getCharacterStates();

	void setCharacterStates(Set<CharacterState> characterStates);

	/**
	 * This method creates the state element (i.e. a single state definition
	 * within a state set, inside a format element). Because state elements
	 * require a symbol attribute it needs to be passed in here.
	 * @author rvosa
	 */
	CharacterState createCharacterState(Object symbol);
	
	/**
	 * The method creates the polymorphic_state_set element. Because state 
	 * elements require a symbol attribute it needs to be passed in here.
	 * Polymorphic_state_set elements have two or more members, these need
	 * to be passed in here. XXX In discussion with Jeet, we concluded that 
	 * polymorphic state sets biologically are a combination of fundamental
	 * states (whereas uncertain state sets can also contain polymorphic states).
	 * THis, however, is very much open to debate. In any case, the method
	 * at present doesn't distinguish between CharacterState subclasses, but
	 * perhaps that needs to change.
	 * @author rvosa
	 */	
	PolymorphicCharacterState createPolymorphicCharacterState(Object symbol,Set<CharacterState> members);
	
	/**
	 * XXX see discussion for createPolymorphicCharacterState()
	 */	
	UncertainCharacterState createUncertainCharacterState(Object symbol,Set<CharacterState> members);	

	/**
	 * Queries the state set for the CharacterState that has 
	 * the provided label.
	 * @param label
	 * @return a CharacterState object, or null
	 */
    public CharacterState lookupCharacterStateByLabel(String label);
    
	/**
	 * Queries the state set for the CharacterState that has 
	 * the provided symbol.
	 * @param symbol
	 * @return a CharacterState object, or null
	 */    
    public CharacterState lookupCharacterStateBySymbol(String symbol);

	/**
	 * Queries the state set for the CharacterState that has 
	 * the provided id.
	 * @param id
	 * @return a CharacterState object, or null
	 */    
    public CharacterState lookupCharacterStateById(String id);
    
}
