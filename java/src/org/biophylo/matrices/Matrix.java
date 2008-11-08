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
		mContainer = CONSTANT.PROJECT;
		mType = CONSTANT.MATRIX;
		this.charlabels = new Vector();	
		mTag = "characters";
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
	
	public void generateXml(StringBuffer sb, boolean compact) throws ObjectMismatch {
		String type = getType();
		String xsi_type = compact ? "nex:"+type+"Seqs" : "nex:"+type+"Cells";
		HashMap idsForStates = null;
		setAttributes("xsi:type", xsi_type);
		getXmlTag(sb, false);
		if ( ! compact ) {
			sb.append("<format>");
			Datatype to = getTypeObject();
			idsForStates = to.getIdsForStates();
			to.generateXml(sb);
			if ( idsForStates != null ) {
				generateXmlCharLabels(sb,to.getXmlId());
			}
			else {
				generateXmlCharLabels(sb,null);
			}
			sb.append("</format>");
		}
		sb.append("<matrix>");
		int nchar = getNchar();
		String[] charIds = new String[nchar];
		for ( int i = 0; i < nchar; i++ ) {
			charIds[i] = "c" + ( i + 1 );
		}
		Containable[] rows = getEntities();
		for ( int i = 0; i < rows.length; i++ ) {
			((Datum)rows[i]).generateXml(sb, idsForStates, charIds, compact);
		}
		sb.append("</matrix>").append("</").append(getTag()).append('>');	
	}
	
	private void generateXmlCharLabels(StringBuffer sb,String statesId) {
		Vector labels = getCharLabels();
		int nchar = getNchar();
		for ( int i = 1; i <= nchar; i++ ) {
			String label = null;
			if ( ! labels.isEmpty() ) {
				label = (String)labels.get(i-1);
			}
			sb.append("<char id=\"c").append(i);
			if ( statesId != null && ! getTypeObject().isValueConstrained() ) {
				sb.append("\" states=\"").append(statesId);
			}
			if ( label != null ) {
				sb.append("\" label=\"").append(label);
			}
			sb.append("\"/>");
		}
	}

}
