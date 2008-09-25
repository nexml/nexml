package org.biophylo.Util;

import org.biophylo.Base;
import java.util.*;
import org.biophylo.Util.Exceptions.*;
import org.biophylo.Taxa.*;
import org.biophylo.*;

public class XMLWritable extends Base {
	protected String tag;
	protected HashMap attributes;
	protected String xmlId;
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public void setAttributes(HashMap attributes) {
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
	
	public void setAttributes(String key,String value) {
		if ( this.attributes == null ) {
			this.attributes = new HashMap();
		}
		this.attributes.put(key, value);
	}
	
	public void setXmlId(String xmlId) {
		this.xmlId = xmlId;
	}
	
	public String getTag () {
		return this.tag;
	}
	
	public String getXmlTag(boolean closeMe) throws ObjectMismatch {
		HashMap attrs = this.getAttributes();
		String tag = this.getTag();
		StringBuffer sb = new StringBuffer();
		sb.append('<');
		sb.append(tag);
		Object[] keys = attrs.keySet().toArray();
		for ( int i = 0; i < keys.length; i++ ) {
			sb.append(' ');
			sb.append((String)keys[i]);
			sb.append("=\"");
			sb.append((String)attrs.get(keys[i]));
			sb.append('"');
		}
		HashMap dict = (HashMap)this.getGeneric("dict");
		if ( dict != null ) {
			sb.append("><dict>");
			keys = dict.keySet().toArray();
			for ( int i = 0; i < keys.length; i++ ) {
				sb.append("<key>");
				sb.append((String)keys[i]);
				sb.append("</key>");
				Vector val = (Vector)dict.get(keys[i]);
				sb.append('<');
				sb.append(val.get(0));
				sb.append('>');
				for ( int j = 1; j < val.size(); j++ ) {
					if ( j > 1 ) {
						sb.append(' ');
					}
					sb.append(val.get(j));
				}
				sb.append("</");
				sb.append(val.get(0));
				sb.append('>');				
			}
			sb.append("</dict>");
			if ( closeMe ) {
				sb.append("</");
				sb.append(tag);
				sb.append('>');
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
		return sb.toString();
	}
	
	public String getRootOpenTag () {
		StringBuffer sb = new StringBuffer();
		sb.append("<nex:nexml version=\"1.0\" generator=\"");
		sb.append(this.getClass().getName());
		sb.append(" v.");
		sb.append(Base.VERSION);
		sb.append("\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		sb.append(" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"");
		sb.append(" xsi:schemaLocation=\"http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd\"");
		sb.append(" xmlns:nex=\"http://www.nexml.org/1.0\">");
		return sb.toString();		
	}
	
	public String getRootCloseTag() {
		return "</nex:nexml>";
	}

	public String getXmlId() {
		if ( this.xmlId != null ) {
			return this.xmlId;
		}
		else {
			return this.getTag() + this.getId();
		}
	}
	
	public HashMap getAttributes () throws ObjectMismatch{
		HashMap attrs = this.attributes;
		if ( attrs == null ) {
			attrs = new HashMap();
		}
		if ( !attrs.containsKey("label") && this.getName() != null ) {
			attrs.put("label", this.getName());
		}
		if ( !attrs.containsKey("id") ) {
			attrs.put("id", this.getXmlId());
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
	
	public String toXml () throws ObjectMismatch {
		String xml = null;
		if ( this instanceof Listable ) {
			StringBuffer sb = new StringBuffer();
			Containable[] ents = ((Listable)this).getEntities();
			for ( int i = 0; i < ents.length; i++ ) {
				sb.append(ents[i].toXml());
			}
			xml = sb.toString();
		}
		if ( xml != null ) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getXmlTag(false));
			sb.append(xml);
			sb.append("</");
			sb.append(this.getTag());
			sb.append('>');
			return sb.toString();
		}
		else {
			return this.getXmlTag(true);
		}
	}
}
