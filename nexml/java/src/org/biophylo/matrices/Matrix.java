package org.biophylo.matrices;

import org.biophylo.Listable;
import org.biophylo.matrices.datatype.Datatype;
import org.biophylo.mediators.TaxaMediator;
import org.biophylo.taxa.Taxa;
import org.biophylo.taxa.TaxaLinker;
import org.biophylo.*;
import org.biophylo.util.exceptions.*;
import org.biophylo.util.*;
import java.util.*;
import org.w3c.dom.Element;

public class Matrix extends Listable implements TypeSafeData, TaxaLinker {
	private Datatype typeObject;
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	private static Logger logger = Logger.getInstance();
	private Vector charlabels;
	
	/**
	 * 
	 */
	public Matrix() {
		super();
		this.initialize("Standard");
	}	
	
	/**
	 * @param type
	 */
	public Matrix(String type) {
		super();
		this.initialize(type);
	}	
	
	/**
	 * @param type
	 */
	private void initialize (String type) {
		this.typeObject = Datatype.getInstance(type);
		this.container = CONSTANT.PROJECT;
		this.type = CONSTANT.MATRIX;
		this.charlabels = new Vector();	
		this.tag = "characters";
	}
	
	/**
	 * @return
	 */
	public Vector getCharLabels() {
		int nchar = this.getNchar();
		logger.info("nchar: "+nchar);
		if ( this.charlabels.capacity() < nchar ) {
			this.charlabels.ensureCapacity(nchar);
		}
		return this.charlabels;
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#getGap()
	 */
	public char getGap() {
		return this.getTypeObject().getGap();
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#getLookup()
	 */
	public int[][] getLookup() {
		return this.getTypeObject().getLookup();
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#getMissing()
	 */
	public char getMissing() {
		return this.getTypeObject().getMissing();
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#getType()
	 */
	public String getType() {
		return this.getTypeObject().getType();
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#getTypeObject()
	 */
	public Datatype getTypeObject() {
		return this.typeObject;
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#setGap(char)
	 */
	public void setGap(char gap) {
		this.getTypeObject().setGap(gap);
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#setLookup(int[][])
	 */
	public void setLookup(int[][] lookup) {
		this.getTypeObject().setLookup(lookup);
	}
	
	/**
	 * @param lookup
	 */
	public void setLookup(HashMap lookup) {
		this.getTypeObject().setLookup(lookup);
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#setMissing(char)
	 */
	public void setMissing(char missing) {
		this.getTypeObject().setMissing(missing);
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Matrices.TypeSafeData#setTypeObject(org.biophylo.Matrices.Datatype.Datatype)
	 */
	public void setTypeObject(Datatype typeObject) {
		this.typeObject = typeObject;
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxaLinker#getTaxa()
	 */
	public Taxa getTaxa() {
		return (Taxa)taxaMediator.getLink(this.getId());
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxaLinker#setTaxa(org.biophylo.Taxa.Taxa)
	 */
	public void setTaxa(Taxa taxa) {
		taxaMediator.setLink(taxa.getId(), this.getId());
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxaLinker#unsetTaxa()
	 */
	public void unsetTaxa() {
		taxaMediator.removeLink(-1, this.getId());
	}
	
	/**
	 * @return
	 */
	public int getNchar() {
		Containable[] rows = this.getEntities();
		int nchar = 0;
		for ( int i = 0; i < rows.length; i++ ) {
			String[] chars = ((Datum)rows[i]).getChar();
			if ( chars.length > nchar ) {
				nchar = chars.length;
			}
		}
		return nchar;
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Listable#canContain(java.lang.Object)
	 */
	public boolean canContain(Object obj) {
		if ( this.type() != ((Containable)obj).container() ) {
			return false;
		}
		else {
			if ( ! this.getTypeObject().isSame( ((TypeSafeData)obj).getTypeObject() ) ) {
				return false;
			}
			else {
				return true;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Listable#insert(java.lang.Object)
	 */
	public void insert(Object obj) throws ObjectMismatch {
		if ( this.canContain(obj) ) {
			((TypeSafeData)obj).setTypeObject(this.getTypeObject());
			super.insert(obj);
		}
		else {
			throw new ObjectMismatch();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Listable#insert(java.lang.Object[])
	 */
	public void insert(Object[] obj) throws ObjectMismatch {
		for ( int i = 0; i < obj.length; i++ ) {
			this.insert(obj[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Util.XMLWritable#toXmlElement()
	 */
	public Element toXmlElement() throws ObjectMismatch {
		return toXmlElement(false);
	}
	
	/**
	 * @param compact
	 * @return
	 * @throws ObjectMismatch
	 */
	public Element toXmlElement (boolean compact) throws ObjectMismatch {
		if ( getDocument() == null ) {
			setDocument(createDocument());
		}		
		String type = getType();
		String xsi_type = compact ? "nex:"+type+"Seqs" : "nex:"+type+"Cells";
		HashMap idsForStates = null;
		setAttributes("xsi:type", xsi_type);
		Element charsElt = createElement(getTag(),getAttributes(),getDocument());
		if ( ! compact ) {
			Element format = createElement("format",getDocument());
			Datatype to = getTypeObject();
			idsForStates = to.getIdsForStates();
			to.setDocument(getDocument());
			Element toElt = to.toXmlElement();
			if ( toElt != null ) {
				format.appendChild(toElt);
			}
			Vector charElts = null;
			if ( idsForStates != null ) {
				charElts = charLabelsToElement(to.getXmlId());
			}
			else {
				charElts = charLabelsToElement(null);
			}
			for ( int i = 0; i < charElts.size(); i++ ) {
				format.appendChild((Element)charElts.get(i));
			}
			charsElt.appendChild(format);
		}
		Element matrixElt = createElement("matrix",getDocument());
		int nchar = getNchar();
		String[] charIds = new String[nchar];
		for ( int i = 0; i < nchar; i++ ) {
			charIds[i] = "c" + ( i + 1 );
		}
		Containable[] rows = getEntities();
		for ( int i = 0; i < rows.length; i++ ) {
			Datum datum = (Datum)rows[i];
			datum.setDocument(getDocument());
			matrixElt.appendChild(datum.toXmlElement(idsForStates, charIds, compact));
		}
		charsElt.appendChild(matrixElt);
		return charsElt;
	}
	
	/**
	 * @param compact
	 * @return
	 * @throws ObjectMismatch
	 */
	public String toXml(boolean compact) throws ObjectMismatch {
		Element theElt = toXmlElement(compact);
		return elementToString(theElt);
	}
	
	/**
	 * @param statesId
	 * @return
	 */
	private Vector charLabelsToElement(String statesId) {
		Vector labels = getCharLabels();
		Vector elements = new Vector();
		int nchar = getNchar();
		for ( int i = 0; i < nchar; i++ ) {
			String label = null;
			if ( ! labels.isEmpty() ) {
				label = (String)labels.get(i-1);
			}
			HashMap charAttrs = new HashMap();
			charAttrs.put("id", "c"+(i+1));
			if ( statesId != null && ! getTypeObject().isValueConstrained() ) {
				charAttrs.put("states", statesId);
			}
			if ( label != null ) {
				charAttrs.put("label", label);
			}
			Element charElt = createElement("char",charAttrs,getDocument());
			elements.add(charElt);			
		}
		return elements;
	}	
	
	/**
	 * @param statesId
	 * @return
	 */
	private String writeCharLabels(String statesId) {
		StringBuffer sb = new StringBuffer();
		Vector labels = this.getCharLabels();
		int nchar = this.getNchar();
		for ( int i = 1; i <= nchar; i++ ) {
			String label = null;
			if ( ! labels.isEmpty() ) {
				label = (String)labels.get(i-1);
			}
			sb.append("<char id=\"c");
			sb.append(i);
			if ( statesId != null && ! this.getTypeObject().isValueConstrained() ) {
				sb.append("\" states=\"");
				sb.append(statesId);
			}
			if ( label != null ) {
				sb.append("\" label=\"");
				sb.append(label);
			}
			sb.append("\"/>");
		}
		return sb.toString();
	}

}
