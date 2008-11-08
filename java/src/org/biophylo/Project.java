package org.biophylo;
import org.biophylo.Listable;
import org.biophylo.util.CONSTANT;
import org.biophylo.util.Logger;
import org.biophylo.util.exceptions.*;
import java.util.*;
import org.biophylo.taxa.*;
import org.biophylo.matrices.*;
import org.biophylo.forest.*;
import org.w3c.dom.*;

public class Project extends Listable {
	private static Logger logger = Logger.getInstance();
	
	/**
	 * 
	 */
	public Project () {
		super();
		mContainer = CONSTANT.NONE;
		mType = CONSTANT.PROJECT;
		mTag = "nex:nexml";
		mHasXmlId = false;
	}
	
	/**
	 * @param constant
	 * @return
	 */
	private Vector getObject(int constant) {
		Vector result = new Vector();
		Containable[] ents = this.getEntities();
		for ( int i = 0; i < ents.length; i++ ) {
			if ( ents[i].mType == constant ) {
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
	
	public void generateXml(StringBuffer sb,boolean compact) throws ObjectMismatch {
		HashMap attrs = new HashMap();
		String className = getClass().getName();
		double version = VERSION;
		attrs.put("version", "1.0");
		attrs.put("generator", className + " v." + version);
		attrs.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		attrs.put("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
		attrs.put("xmlns:nex", "http://www.nexml.org/1.0");	
		attrs.put("xsi:schemaLocation", "http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd");
		setAttributes(attrs);
		getXmlTag(sb, false);		
		Taxa[] theTaxa = getTaxa();
		for ( int i = 0; i < theTaxa.length; i++ ) {
			theTaxa[i].generateXml(sb,compact);
		}
		Matrix[] theMatrix = getMatrices();
		for ( int i = 0; i < theMatrix.length; i++ ) {
			theMatrix[i].generateXml(sb, compact);
		}
		Forest[] theForest = getForests();
		for ( int i = 0; i < theForest.length; i++ ) {
			theForest[i].generateXml(sb,compact);
		}	
		sb.append("</").append(getTag()).append('>');
	}
}
