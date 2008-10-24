package org.biophylo;
import org.biophylo.Listable;
import org.biophylo.Util.CONSTANT;
import org.biophylo.Util.Logger;
import org.biophylo.Util.Exceptions.*;
import java.util.*;
import org.biophylo.Taxa.*;
import org.biophylo.Matrices.*;
import org.biophylo.Forest.*;
import org.w3c.dom.*;

public class Project extends Listable {
	private static Logger logger = Logger.getInstance();
	
	/**
	 * 
	 */
	public Project () {
		super();
		this.container = CONSTANT.NONE;
		this.type = CONSTANT.PROJECT;
		this.tag = "nex:nexml";
	}
	
	/**
	 * @param constant
	 * @return
	 */
	private Vector getObject(int constant) {
		Vector result = new Vector();
		Containable[] ents = this.getEntities();
		for ( int i = 0; i < ents.length; i++ ) {
			if ( ents[i].type == constant ) {
				result.add(ents[i]);
			}
		}
		return result;
	}
	
	/**
	 * @return
	 */
	public Taxa[] getTaxa() {
		Vector objects = getObject(CONSTANT.TAXA);
		Taxa[] taxa = new Taxa[objects.size()];
		objects.copyInto(taxa);
		return taxa;
	}
	
	/**
	 * @return
	 */
	public Forest[] getForests() {
		Vector objects = getObject(CONSTANT.FOREST);
		Forest[] forest = new Forest[objects.size()];
		objects.copyInto(forest);
		return forest;
	}
	
	/**
	 * @return
	 */
	public Matrix[] getMatrices() {
		Vector objects = getObject(CONSTANT.MATRIX);
		Matrix[] matrices = new Matrix[objects.size()];
		objects.copyInto(matrices);
		return matrices;
	}
	
	/* (non-Javadoc)
	 * @see org.biophylo.Util.XMLWritable#toXmlElement()
	 */
	public Element toXmlElement () throws ObjectMismatch {
		HashMap attrs = new HashMap();
		String className = this.getClass().getName();
		double version = this.VERSION;
		attrs.put("version", "1.0");
		attrs.put("generator", className + " v." + version);
		attrs.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		attrs.put("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
		attrs.put("xmlns:nex", "http://www.nexml.org/1.0");	
		attrs.put("xsi:schemaLocation", "http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd");
		setDocument(createDocument());
		Element projElt = createElement("nex:nexml",attrs,getDocument());
		if ( getGeneric("dict") != null ) {
			HashMap dict = (HashMap)getGeneric("dict");
			projElt.appendChild(dictToXmlElement(dict));
		}
		Taxa[] theTaxa = getTaxa();
		for ( int i = 0; i < theTaxa.length; i++ ) {
			theTaxa[i].setDocument(getDocument());
			projElt.appendChild(theTaxa[i].toXmlElement());
		}
		Matrix[] theMatrix = getMatrices();
		for ( int i = 0; i < theMatrix.length; i++ ) {
			theMatrix[i].setDocument(getDocument());
			projElt.appendChild(theMatrix[i].toXmlElement());
		}
		Forest[] theForest = getForests();
		for ( int i = 0; i < theForest.length; i++ ) {
			theForest[i].setDocument(getDocument());
			projElt.appendChild(theForest[i].toXmlElement());
		}		
		return projElt;
	}
}
