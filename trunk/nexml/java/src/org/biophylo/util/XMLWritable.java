package org.biophylo.util;

import org.biophylo.Base;
import java.util.*;
import org.biophylo.util.exceptions.*;
import org.biophylo.taxa.*;
import org.biophylo.*;

public class XMLWritable extends Base {
	private static Logger logger = Logger.getInstance();
	protected String mTag;
	protected Map attributes;
	protected String xmlId;
	protected boolean mHasXmlId = true;

	
	/**
	 * @param tag
	 */
	public void setTag(String tag) {
		mTag = tag;
	}
	
	/**
	 * @param attributes
	 */
	public void setAttributes(Map attributes) {
		if ( this.attributes != null ) {
			Object[] keys = attributes.keySet().toArray();
			for ( int i = 0; i < keys.length; i++ ) {
				this.attributes.put(keys[i], attributes.get(keys[i]));
			}
		}
		else {
			this.attributes = attributes;
		}
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void setAttributes(String key,String value) {
		if ( attributes == null ) {
			attributes = new HashMap();
		}
		attributes.put(key, value);
	}
	
	/**
	 * @param xmlId
	 */
	public void setXmlId(String xmlId) {
		this.xmlId = xmlId;
	}
	
	/**
	 * @return
	 */
	public String getTag () {
		return mTag;
	}
	
	public void getXmlTag(StringBuffer sb,boolean closeMe) throws ObjectMismatch {
		Map attrs = getAttributes();
		String tag = getTag();
		sb.append('<').append(tag);
		Object[] keys = attrs.keySet().toArray();
		for ( int i = 0; i < keys.length; i++ ) {
			sb.append(' ').append((String)keys[i]).append("=\"");
			sb.append((String)attrs.get(keys[i])).append('"');
		}
		HashMap dict = (HashMap)this.getGeneric("dict");
		if ( dict != null ) {
			sb.append("><dict>");
			keys = dict.keySet().toArray();
			for ( int i = 0; i < keys.length; i++ ) {				
				sb.append("<key>").append((String)keys[i]).append("</key>");
				Vector val = (Vector)dict.get(keys[i]);
				sb.append('<').append(val.get(0)).append('>');
				for ( int j = 1; j < val.size(); j++ ) {
					if ( j > 1 ) {
						sb.append(' ');
					}
					sb.append(val.get(j));
				}
				sb.append("</").append(val.get(0)).append('>');				
			}
			sb.append("</dict>");
			if ( closeMe ) {
				sb.append("</").append(tag).append('>');
			}
		}
		else {
			if (closeMe) { 
				sb.append("/>");
			}
			else {
				sb.append('>');
			}
		}		
	}

	/**
	 * @return
	 */
	public String getXmlId() {
		if ( this.xmlId != null ) {
			return this.xmlId;
		}
		else {
			return this.getTag() + this.getId();
		}
	}
	
	/**
	 * @return
	 * @throws ObjectMismatch
	 */
	public Map getAttributes () throws ObjectMismatch{
		Map attrs = this.attributes;
		if ( attrs == null ) {
			attrs = new HashMap();
		}
		if ( !attrs.containsKey("label") && getName() != null ) {
			attrs.put("label", getName());
		}
		if ( this.mHasXmlId && !attrs.containsKey("id") ) {
			attrs.put("id", getXmlId());
		}
		if ( this instanceof TaxaLinker ) {
			Taxa taxa = ((TaxaLinker)this).getTaxa();
			if ( taxa == null ) {
				throw new ObjectMismatch();
			}
			else {
				attrs.put("otus", taxa.getXmlId());
			}
		}
		else if ( this instanceof TaxonLinker ) {
			Taxon taxon = ((TaxonLinker)this).getTaxon();
			if ( taxon != null ) {
				attrs.put("otu", taxon.getXmlId());
			}
		}
		return attrs;
	}
	
	public void generateXml(StringBuffer sb,boolean compact) throws ObjectMismatch {
		if ( this instanceof Listable ) {
			getXmlTag(sb,false);
			XMLWritable[] entities = ((Listable)this).getEntities();
			for ( int i = 0; i < entities.length; i++ ) {
				entities[i].generateXml(sb,compact);
			}
			sb.append("</" + getTag() + ">");			
		}
		else {
			getXmlTag(sb,true);
		}		
	}
	
	
	/**
	 * @return
	 * @throws ObjectMismatch
	 */
	public final String toXml (boolean compact) throws ObjectMismatch {
		StringBuffer sb = new StringBuffer();
		generateXml(sb,compact);
		return sb.toString();
	}
	
	public final String toXml () throws ObjectMismatch {
		StringBuffer sb = new StringBuffer();
		generateXml(sb,true);
		return sb.toString();		
	}
}
