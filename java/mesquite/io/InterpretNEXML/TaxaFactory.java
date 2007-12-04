package mesquite.io.InterpretNEXML;

// $Id$

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.duties.TaxaManager;

import org.nexml.ObjectFactory;
import org.xml.sax.Attributes;

class TaxaFactory extends GenericFactory implements ObjectFactory {
	private Taxa taxa;
	
	/*
	 * @param myProject the current MesquiteProject to add parsed objects to
	 * @param myFile    the MesquiteFile to serialize parsed objects to
	 * @param myManager the TaxaManager of the current project
	 */
	TaxaFactory(MesquiteProject myProject, MesquiteFile myFile, TaxaManager myManager) {
		super(myProject,myFile,myManager);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#setCharacterData(char[])
	 */
	public void setCharacterData (char[] myCharacters) {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#getElementsToHandle()
	 */
	public String[] getElementsToHandle () {
		String[] elements = {
			"otus",
			"otu"
		};
		return elements;
	}	
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#getCurrentObject()
	 */
	public Object getCurrentObject () {
		return this.taxa;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#createObject(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public Object createObject (String namespaceURI, String localName, String qName, Attributes atts) {		
		String label = atts.getValue("label");
		String id = atts.getValue("id");
		if ( localName.equalsIgnoreCase("otus") ) {
			taxa = this.getProject().createTaxaBlock(0);
			taxa.setUniqueID(id);
			if ( label != null ) {
				taxa.setName(label);
			}
			return taxa;
		}
		else {
			Taxon taxon = taxa.addTaxon(true);
			taxon.setUniqueID(id);
			if ( label != null ) {
				taxon.setName(label);
			}			
			return taxon;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.ObjectFactory#objectIsComplete(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void objectIsComplete(String namespaceURI, String localName, String qName, Attributes atts) {
		
	}	
}
