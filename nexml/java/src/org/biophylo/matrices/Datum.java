package org.biophylo.matrices;

import org.biophylo.Listable;
import org.biophylo.matrices.datatype.Datatype;
import org.biophylo.mediators.TaxaMediator;
import org.biophylo.taxa.Taxon;
import org.biophylo.taxa.TaxonLinker;
import org.biophylo.util.*;
import org.biophylo.*;
import java.util.*;
import org.w3c.dom.Element;

import org.biophylo.util.exceptions.*;

/**
 * @author rvosa
 *
 */
public class Datum extends Listable implements TaxonLinker, TypeSafeData {
	private Datatype typeObject;
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	private static Logger logger = Logger.getInstance();
	
	/**
	 * 
	 */
	public Datum () {
		super();
		this.initialize("Standard");
	}
	
	/**
	 * @param type
	 */
	public Datum (String type) {
		super();
		this.initialize(type);
	}
	
	/**
	 * @param type
	 */
	private void initialize(String type) {
		this.typeObject = Datatype.getInstance(type);
		mType = CONSTANT.DATUM;
		mContainer = CONSTANT.MATRIX;
		mTag = "row";
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxonLinker#getTaxon()
	 */
	public Taxon getTaxon() {
		return (Taxon)taxaMediator.getLink(this.getId());
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxonLinker#setTaxon(org.biophylo.Taxa.Taxon)
	 */
	public void setTaxon(Taxon taxon) {
		taxaMediator.setLink(taxon.getId(), this.getId());
	}

	/* (non-Javadoc)
	 * @see org.biophylo.Taxa.TaxonLinker#unsetTaxon()
	 */
	public void unsetTaxon() {
		taxaMediator.removeLink(-1, this.getId());
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
	 * @see org.biophylo.Listable#canContain(java.lang.Object)
	 */
	public boolean canContain(Object charsString) {
		return this.getTypeObject().isValid((String)charsString);
	}
	
	/**
	 * @return
	 */
	public String[] getChar() {
		return this.getStringEntities();
	}
	
	/**
	 * @param chars
	 * @throws ObjectMismatch
	 */
	public void insert(String chars) throws ObjectMismatch {
		String[] splitChars = this.getTypeObject().split(chars);
		Vector clean = new Vector();
		for ( int i = 0; i < splitChars.length; i++ ) {
			if ( ! splitChars[i].matches(" ") ) {
				clean.add(splitChars[i]);
			}
		}
		String[] noSpaces = new String[clean.size()];
		clean.copyInto(noSpaces);
 		super.insert(noSpaces);
	}
	
	/**
	 * @param idsForStates
	 * @param charIds
	 * @param compact
	 * @return
	 * @throws ObjectMismatch
	 */
	public void generateXml(StringBuffer sb,Map idsForStates, String[] charIds, boolean compact) throws ObjectMismatch {
		Taxon taxon = getTaxon();
		if ( taxon != null ) {
			setAttributes("otu", taxon.getXmlId());
		}
		String[] chars = getChar();
		String missing = ""+getMissing();
		String gap = ""+getGap();
		getXmlTag(sb, false);
		if ( ! compact ) {
			Datatype to = getTypeObject();
			for ( int i = 0; i < chars.length; i++ ) {
				if ( !missing.equals(chars[i]) && !gap.equals(chars[i]) ) {
					String c, s;
					if ( charIds != null && charIds[i] != null && ! to.isSequential() ) {
						c = charIds[i];
					}
					else {
						c = ""+i;
					}
					String ucChar = chars[i].toUpperCase();
					if ( idsForStates != null && idsForStates.containsKey(ucChar) && ! to.isValueConstrained() ) {
						s = "s" + (String)idsForStates.get(ucChar);
					}
					else {
						s = ucChar;
					}
					sb.append("<cell char=\"").append(c).append("\" state=\"").append(s).append("\"/>");
				}
			}
		}
		else {
			String[] ucChars = new String[chars.length];
			for ( int i = 0; i < ucChars.length; i++ ) {
				ucChars[i] = chars[i].toUpperCase();
			}
			String seq = this.getTypeObject().join(ucChars);
			sb.append("<seq>").append(seq).append("</seq>");
		}
		sb.append("</").append(getTag()).append('>');	
	}

}
