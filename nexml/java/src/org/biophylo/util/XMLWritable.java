package org.biophylo.util;

import org.biophylo.Base;
import java.util.*;
import org.biophylo.util.exceptions.*;
import org.biophylo.taxa.*;
import org.biophylo.*;

public class XMLWritable extends Base {
	protected String mTag;
	private Map mAttributes;
	private String mXmlId;
	protected boolean mHasXmlId = true;

	/**
	 * 
	 * @param s
	 * @return
	 */
	private String XMLEntityEncode(String s) {
		StringBuffer buf = new StringBuffer();
		int len = (s == null ? -1 : s.length());
		for ( int i = 0; i < len; i++ ) {
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.') {
				buf.append(c);
			} 
			else {
				buf.append("&#" + (int) c + ";");
			}
		}
		return buf.toString();
	}
	
	
	/**
	 * @param tag
	 */
	public void setTag(String tag) {
		mTag = tag;
	}
	
	/**
	 * Sets attributes as a Map
	 * @param attributes
	 */
	public void setAttributes(Map pAttributes) {
		if ( mAttributes != null ) {
			Object[] keys = pAttributes.keySet().toArray();
			for ( int i = 0; i < keys.length; i++ ) {
				mAttributes.put(keys[i], pAttributes.get(keys[i]));
			}
		}
		else {
			mAttributes = pAttributes;
		}
	}
	
	/**
	 * Sets a key/value attribute pair.
	 * @param key
	 * @param value
	 */
	public void setAttributes(String key,String value) {
		if ( mAttributes == null ) {
			mAttributes = new HashMap();
		}
		mAttributes.put(key, value);
	}
	
	/**
	 * Sets the value of the id attribute of generated xml.
	 * Under normal circumstances you would never use this
	 * method, as xml ids are generated internally.
	 * @param xmlId
	 */
	public void setXmlId(String pXmlId) {
		mXmlId = pXmlId;
	}
	
	/**
	 * @return the element name that the invocant serializes into
	 */
	public String getTag () {
		return mTag;
	}
	
	/**
	 * 
	 * @param sb
	 * @param closeMe
	 * @throws ObjectMismatch
	 */
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
		if ( mXmlId != null ) {
			return mXmlId;
		}
		else {
			return getTag() + getId();
		}
	}
	
	/**
	 * @return
	 * @throws ObjectMismatch
	 */
	public Map getAttributes () throws ObjectMismatch{
		Map attrs = mAttributes;
		if ( attrs == null ) {
			attrs = new HashMap();
		}
		if ( !attrs.containsKey("label") && getName() != null ) {
			attrs.put("label", XMLEntityEncode(getName()));
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
	
	/**
	 * 
	 * @param sb
	 * @param compact
	 * @throws ObjectMismatch
	 */
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
	
	/**
	 * 
	 * @return
	 * @throws ObjectMismatch
	 */
	public final String toXml () throws ObjectMismatch {
		StringBuffer sb = new StringBuffer();
		generateXml(sb,true);
		return sb.toString();		
	}
}
