package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.CharacterState;
import org.nexml.model.CompoundCharacterState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract class CompoundCharacterStateImpl extends CharacterStateImpl
		implements CompoundCharacterState {
	public CompoundCharacterStateImpl(Document document) {
		super(document);
	}

	public CompoundCharacterStateImpl(Document document,Element element) {
		super(document,element);
	}	
	
	private Set<CharacterState> mCharacterStates;

	public Set<CharacterState> getStates() {
		return mCharacterStates;
	}

	/**
	 * XXX As the behaviour of our objects implies that the
	 * calling this method replaces the current set, the 
	 * equivalent for the element tree mirrors that, i.e.
	 * the current member elements are removed, and new ones
	 * are created.
	 * @author rvosa
	 */
	public void setStates(Set<CharacterState> characterStates) {
		mCharacterStates = characterStates;
		NodeList currentMembers = getElement().getChildNodes();
		for ( int i = 0; i < currentMembers.getLength(); i++ ) {
			getElement().removeChild(currentMembers.item(i));
		}
		for ( CharacterState newMember : characterStates ) {
			Element member = getDocument().createElement("member");
			member.setAttribute("state", newMember.getId());
			getElement().appendChild(member);
		}
	}

}
