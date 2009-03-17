package org.nexml.model;

import java.util.Set;

public interface CompoundCharacterState extends CharacterState {
	/**
	 * Gets all the states onto which the invocant maps, either
	 * as uncertainty (this or that) or as polymorphism (this and that)
	 * @return
	 */
	Set<CharacterState> getStates();

	/**
	 * Sets all the states onto which the invocant maps, either
	 * as uncertainty (this or that) or as polymorphism (this and that)
	 * @param characterStates a set of member states
	 */
	void setStates(Set<CharacterState> characterStates);
}
