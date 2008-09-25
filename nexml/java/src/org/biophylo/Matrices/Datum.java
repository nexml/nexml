package org.biophylo.Matrices;

import org.biophylo.Listable;
import org.biophylo.Matrices.Datatype.Datatype;
import org.biophylo.Mediators.TaxaMediator;
import org.biophylo.Taxa.Taxon;
import org.biophylo.Taxa.TaxonLinker;
import org.biophylo.Util.*;
import org.biophylo.*;
import java.util.*;

import org.biophylo.Util.Exceptions.*;

public class Datum extends Listable implements TaxonLinker, TypeSafeData {
	private Datatype typeObject;
	private static TaxaMediator taxaMediator = TaxaMediator.getInstance();
	private static Logger logger = Logger.getInstance();
	
	public Datum () {
		super();
		this.initialize("Standard");
	}
	
	public Datum (String type) {
		super();
		this.initialize(type);
	}
	
	private void initialize(String type) {
		this.typeObject = Datatype.getInstance(type);
		this.type = CONSTANT.DATUM;
		this.container = CONSTANT.MATRIX;
		this.tag = "row";
	}
	
	public Taxon getTaxon() {
		return (Taxon)taxaMediator.getLink(this.getId());
	}

	public void setTaxon(Taxon taxon) {
		taxaMediator.setLink(taxon.getId(), this.getId());
	}

	public void unsetTaxon() {
		taxaMediator.removeLink(-1, this.getId());
	}

	public char getGap() {
		return this.getTypeObject().getGap();
	}

	public int[][] getLookup() {
		return this.getTypeObject().getLookup();
	}

	public char getMissing() {
		return this.getTypeObject().getMissing();
	}

	public String getType() {
		return this.getTypeObject().getType();
	}

	public Datatype getTypeObject() {
		return this.typeObject;
	}

	public void setGap(char gap) {
		this.getTypeObject().setGap(gap);
	}

	public void setLookup(int[][] lookup) {
		this.getTypeObject().setLookup(lookup);
	}
	
	public void setLookup(HashMap lookup) {
		this.getTypeObject().setLookup(lookup);
	}	

	public void setMissing(char missing) {
		this.getTypeObject().setMissing(missing);
	}

	public void setTypeObject(Datatype typeObject) {
		this.typeObject = typeObject;
	}
	
	public boolean canContain(Object charsString) {
		return this.getTypeObject().isValid((String)charsString);
	}
	
	public String[] getChar() {
		return this.getStringEntities();
	}
	
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
	
	public String toXml() throws ObjectMismatch {
		return this.toXml(null,null,false);
	}
	
	public String toXml(HashMap idsForStates, String[] charIds, boolean compact) throws ObjectMismatch {
		Taxon taxon = this.getTaxon();
		if ( taxon != null ) {
			this.setAttributes("otu", taxon.getXmlId());
		}
		String[] chars = this.getChar();
		String missing = ""+this.getMissing();
		String gap = ""+this.getGap();
		StringBuffer sb = new StringBuffer();
		sb.append(this.getXmlTag(false));
		if ( ! compact ) {
			Datatype to = this.getTypeObject();
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
					sb.append("<cell char=\"");
					sb.append(c);
					sb.append("\" state=\"");
					sb.append(s);
					sb.append("\"/>");
				}
			}
		}
		else {
			String[] ucChars = new String[chars.length];
			for ( int i = 0; i < ucChars.length; i++ ) {
				ucChars[i] = chars[i].toUpperCase();
			}
			String seq = this.getTypeObject().join(ucChars);
			sb.append("<seq>");
			sb.append(seq);
			sb.append("</seq>");
		}
		sb.append("</");
		sb.append(this.getTag());
		sb.append('>');
		return sb.toString();
	}

}
