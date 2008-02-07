package mesquite.io.InterpretNEXML;

// $Id$

import java.util.Hashtable;
import mesquite.categ.lib.CategoricalData;
import mesquite.cont.lib.ContinuousData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.CharactersManager;
import org.nexml.ObjectFactory;
import org.xml.sax.Attributes;

/**
 * To parse a nexml characters block, the following (may) need to happen:
 * 
 * - parse out the data type (xsi:type) and translate it to the equivalent
 *   mesquite data type, instantiate the appropriate matrix object and link
 *   it up with the appropriate taxa object
 *   
 * - for each &lt;states&gt; element, create a new slot in the stateMap
 *   hashtable and record the id of that element, the state set
 *   
 * - for each &lt;state&gt; element, create a nested slot in the stateMap
 *   hashtable and populate it with the symbol, keyed on the state ID
 *   
 * - for each &lt;char&gt; element, create a new slot in the stateSetForCharacter
 *   hashtable, and populate it with the appropriate stateMap entry
 * 
 * Nexml character data types, + indicates data types supported by Mesquite:
 * + nex:ContinuousSeqs
 * + nex:ContinuousCells
 * + nex:DnaSeqs
 * + nex:DnaCells
 * + nex:ProteinSeqs
 * + nex:ProteinCells
 * - nex:RestrictionSeqs
 * - nex:RestrictionCells
 * + nex:RnaSeqs
 * + nex:RnaCells
 * + nex:StandardSeqs
 * + nex:StandardCells
 * 
 */
class CharactersFactory extends GenericFactory implements ObjectFactory {
	private CharacterData data;
	private CharactersManager manager;
	private Hashtable discDataForXsiType   = new Hashtable();
	private Hashtable contDataForXsiType   = new Hashtable();
	private Hashtable stateMap             = new Hashtable();
	private Hashtable stateSetForCharacter = new Hashtable();
	private Hashtable colIndexForChar      = new Hashtable();
	private String currentStateSetID;
	private String currentStateID;
	private String currentRowID;
	private int row;
	private int col;
	private int columnCount;
	private char[] characters;
	private boolean hasStateLookup = false;
	//private static boolean verbose = true;

	CharactersFactory(MesquiteProject myProject, MesquiteFile myFile, CharactersManager myManager) {
		super(myProject,myFile,myManager);
		this.manager = myManager;
		this.discDataForXsiType.put( "nex:DnaSeqs",         "DNA Data"                  );
		this.discDataForXsiType.put( "nex:DnaCells",        "DNA Data"                  );
		this.discDataForXsiType.put( "nex:ProteinSeqs",     "Protein Data"              );
		this.discDataForXsiType.put( "nex:ProteinCells",    "Protein Data"              );
		this.discDataForXsiType.put( "nex:RnaSeqs",         "RNA Data"                  );
		this.discDataForXsiType.put( "nex:RnaCells",        "RNA Data"                  );
		this.discDataForXsiType.put( "nex:StandardSeqs",    "Standard Categorical Data" );
		this.discDataForXsiType.put( "nex:StandardCells",   "Standard Categorical Data" );
		this.contDataForXsiType.put( "nex:ContinuousSeqs",  "Continuous Data"           );
		this.contDataForXsiType.put( "nex:ContinuousCells", "Continuous Data"           );
		log("Instantiated CharactersFactory");
	}
	
	/**
	 * Sets raw character data (sequence data) for the current row in the matrix
	 * @param myCharacters an array of characters
	 * @see org.nexml.ObjectFactory#setCharacterData(char[])
	 */
	public void setCharacterData (char[] myCharacters) {
		this.characters = myCharacters;
	}
	
	/**
	 * Returns the current object
	 * @return an Object
	 * @see org.nexml.ObjectFactory#getCurrentObject()
	 */
	public Object getCurrentObject () {
		return null;
	}	
	
	/**
	 * Creates a mesquite characters block
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified (without prefix) name of the element
	 * @param qName        the fully qualified (with prefix) name of the element
	 * @param atts         any attributes associated with the element
	 * @see org.nexml.ObjectFactory#createObject(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 * @see Attributes
	 */
	public Object createObject (String namespaceURI, String localName, String qName, Attributes atts) {
		log("\n--------- ELEMENT --------------");
		log("namespaceURI -> " + namespaceURI);
		log("localName    -> " + localName);
		log("qName        -> " + qName);
		log("atts         -> " + atts);
		if ( localName.equals("characters") ) {
			return this.handleCharactersElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("states") ) {
			this.handleStatesElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("state") ) {
			this.handleStateElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("char") ) {
			this.handleCharElement(namespaceURI, localName, qName, atts);
		}			
		else if ( localName.equals("row") ) {
			this.handleRowElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("seq") ) {
			this.handleSeqElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("cell") ) {
			this.handleCellElement(namespaceURI, localName, qName, atts);
		}
		else if ( localName.equals("matrix") ) {
			this.handleMatrixElement(namespaceURI, localName, qName, atts);
		}		
		return null;
	}	
	
	/**
	 * Executes when the closing tag for the characters element is encountered.
	 * @param namespaceURI the universal resource identifier of the element, typically http://www.nexml.org/1.0
	 * @param localName    the unqualified (without prefix) name of the element
	 * @param qName        the fully qualified (with prefix) name of the element
	 * @param atts         any attributes associated with the element
	 * @see org.nexml.ObjectFactory#objectIsComplete(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void objectIsComplete(String namespaceURI, String localName, String qName, Attributes atts) {
		if ( localName.equals("seq") && this.data != null ) {
			String sequence = new String(this.characters);
			String[] tokens;
			int numChars = this.data.getNumChars();
			if ( this.data instanceof CategoricalData || this.data instanceof ContinuousData ) {
				// split standard and continuous on space
				tokens = sequence.split("\\s+");
			}
			else {
				// split others on characters
				tokens = sequence.split("");
			}
			if ( numChars < tokens.length ) {
				this.data.addCharacters( numChars - 1, tokens.length - numChars, false );
			}
			for ( int i = 0; i < tokens.length; i++ ) {
				CharacterState cs = this.data.makeCharacterState();
				cs.setValue(tokens[i], this.data);
				this.data.setState(i, this.row, cs);	
				log("token " + i + " -> " + tokens[i]);
			}
		}
		this.characters = null;
	}	
	
	/**
	 * Returns an array of local names of elements this factory can process
	 * @return an array of local element names
	 * @see org.nexml.ObjectFactory#getElementsToHandle()
	 */
	public String[] getElementsToHandle () {
		String[] elements = {
			"characters",
			"states",
			"state",
			"char",
			"row",
			"seq",
			"cell",
			"matrix"
		};
		return elements;
	}	
	
	/*
	 * Private method, is called when the object is completed.
	 * @param obj   a mesquite CharacterData object
	 * @param id    value of the nexml id attribute
	 * @param label value (if any) of the nexml label attribute
	 * @return a mesquite CharacterData object
	 * @see CharacterData
	 */
	private CharacterData finalizeObject (CharacterData obj, String id, String label) {
		if ( obj != null ) {
			obj.setUniqueID(id);
			if ( label != null ) {
				obj.setName(label);
			}
			this.addToFile(obj);
		}
		return obj;
	}
	
	/*
	 * Handles the "states" child element of the "characters" element
	 */
	private void handleStatesElement (String namespaceURI, String localName, String qName, Attributes atts) {
		this.currentStateSetID = atts.getValue("id");
		this.stateMap.put(this.currentStateSetID, new Hashtable());
		log("processed states element " + atts.getValue("id"));
	}
	
	/*
	 * Processes &lt;state&gt; element, adds a new symbol keyed on state ID for the current state set
	 */
	private void handleStateElement (String namespaceURI, String localName, String qName, Attributes atts) {
		this.currentStateID = atts.getValue("id");
		Hashtable h = (Hashtable)this.stateMap.get(this.currentStateSetID);
		h.put(this.currentStateID, atts.getValue("symbol"));
		this.hasStateLookup = true;
		log("processed state element " + atts.getValue("id"));
	}
	
	/*
	 * Allocates space in matrix object
	 */
	private void handleMatrixElement (String namespaceURI, String localName, String qName, Attributes atts) {
		if ( this.data != null ) {
			this.data.addCharacters(0, this.columnCount, false);
		}
	}
	
	/*
	 * Processes &lt;row&gt; element
	 */
	private void handleRowElement (String namespaceURI, String localName, String qName, Attributes atts) {
		this.currentRowID = atts.getValue("id");
		String taxonID = atts.getValue("otu");
		this.row++;
		this.col = 0;
		log("processed row element " + atts.getValue("id"));
	}	
	
	/*
	 * Processes &lt;seq&gt; element
	 */
	private void handleSeqElement (String namespaceURI, String localName, String qName, Attributes atts) {
		
	}	
	
	/*
	 * Processes &lt;cell&gt; element: looks up the symbol identified by character ID 
	 * (which indirectly identifies a state set)
	 * and state ID
	 */
	private void handleCellElement (String namespaceURI, String localName, String qName, Attributes atts) {
		String stateID = atts.getValue("state");
		String charID  = atts.getValue("char");
		String state;
		if ( this.hasStateLookup ) {
			state = (String)((Hashtable)this.stateSetForCharacter.get(charID)).get(stateID);	
		}
		else {
			state = stateID;
		}
		CharacterState cs = this.data.makeCharacterState();
		cs.setValue(state, this.data);
		this.data.setState(this.col, this.row, cs);
		this.col++;	
		log("processed cell element " + atts.getValue("char") + " CharacterState: " + cs + ", symbol: " + state);
	}		
	
	/*
	 * Processes &lt;char&gt; element: populates mapping from characters (columns) to state sets
	 */
	private void handleCharElement (String namespaceURI, String localName, String qName, Attributes atts) {
		String charID = atts.getValue("id");
		String statesID = atts.getValue("states");
		MesquiteInteger mi = new MesquiteInteger();
		mi.setValue(this.columnCount);
		this.colIndexForChar.put(charID, mi);
		this.columnCount++;
		if ( statesID != null ) {
			this.stateSetForCharacter.put(charID, (Hashtable)this.stateMap.get(statesID));
		}
		log("processed char element " + atts.getValue("id"));
	}	
	private CharacterData handleCharactersElement (String namespaceURI, String localName, String qName, Attributes atts) {
		this.row = -1;
		this.hasStateLookup = false;
		this.col = 0;
		this.columnCount = 0;		
		String type = "UNKNOWN";
		Taxa taxa = this.getTaxaByID(atts.getValue("otus"));
		for ( int i = 0; i < atts.getLength(); i++ ) {
			if ( atts.getURI(i).equals("http://www.w3.org/2001/XMLSchema-instance") && atts.getLocalName(i).equals("type") ) {
				type = atts.getValue(i);	
				log("xsi:type=" + type);
			}
		}
		if ( this.discDataForXsiType.containsKey(type) ) {
			String dataType = (String)this.discDataForXsiType.get(type);
			this.data = (CategoricalData)this.manager.newCharacterData(taxa, 0, dataType);
			log("mesquite type=" + dataType);
		}
		else if ( this.contDataForXsiType.containsKey(type) ) {
			String dataType = (String)this.contDataForXsiType.get(type);
			this.data = (ContinuousData)this.manager.newCharacterData(taxa, 0, dataType);	
			log("mesquite type=" + dataType);
		}
		else {
			log("No type!");
			return null;
		}
		return this.finalizeObject(this.data, atts.getValue("id"), atts.getValue("label"));			
	}
	
}
