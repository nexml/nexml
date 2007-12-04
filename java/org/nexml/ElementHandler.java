package org.nexml;

// $Id$

import org.xml.sax.*;
public interface ElementHandler extends ContentHandler {
	
	/**
	 * Returns the last accumulated sequence of characters saved from the
	 * stream.
	 * @return an array of characters
	 */	
	public char[] getLastCharacterSequence ();
	
	/**
	 * Sets a factory, i.e. an object that turns xml elements into 
	 * phylogenetic objects, for a given element name.
	 * @param eltName an xml element name
	 * @param fac     a factory that turns elements of the specified name into objects
	 */	
	public void setFactoryForElementName (String eltName, ObjectFactory fac);
	
	/**
	 * Sets a factory, i.e. an object that turns xml elements into 
	 * phylogenetic objects, for a given list of element names.
	 * @param eltNames a list of xml element names
	 * @param fac      a factory that turns elements of the specified names into objects
	 */	
	public void setFactoryForElementNames(String[] eltNames, ObjectFactory fac);
		
	/**
	 * Returns a factory that creates objects appropriate for a given
	 * element name.
	 * @param eltName an xml element name
	 * @return        the factory that creates objects from the specified element
	 */	
	public ObjectFactory getFactoryForElementName(String eltName);
	
	/**
	 * Sets the listener which will be notified of any new objects 
	 * (including "null", potentially) created by factories.
	 * @param myOl an object listener 
	 */	
	public void setObjectListener(ObjectListener ol);
	
	/**
	 * Gets the listener which will be notified of any new objects 
	 * (including "null", potentially) created by factories.
	 * @return an object listener 
	 */		
	public ObjectListener getObjectListener();
}
