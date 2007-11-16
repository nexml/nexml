package org.nexml;

import org.xml.sax.Attributes;

public interface ObjectFactory {
	public Object createObject (String namespaceURI, String localName, String qName, Attributes atts);
	public void setCharacterData (char[] character);
	public void objectIsComplete (String namespaceURI, String localName, String qName, Attributes atts);
	public Object getCurrentObject ();
	public String[] getElementsToHandle ();
}
