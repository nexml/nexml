package org.nexml;
import org.xml.sax.*;
public interface ElementHandler extends ContentHandler {
	
	public char[] getLastCharacterSequence ();
	
	public void setFactoryForElementName (String eltName, ObjectFactory fac);
	
	public void setFactoryForElementNames(String[] eltNames, ObjectFactory fac);
	
	public ObjectFactory getFactoryForElementName(String eltName);
	
	public void setObjectListener(ObjectListener ol);
	
	public ObjectListener getObjectListener();
}
