package org.biophylo.Util;

import org.biophylo.Base;
import java.util.*;
import org.biophylo.Util.Exceptions.*;
import org.biophylo.Taxa.*;
import org.biophylo.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class XMLWritable extends Base {
	private static Logger logger = Logger.getInstance();
	protected String tag;
	protected Map attributes;
	protected String xmlId;
	protected Document doc;
	
	/**
	 * @param el
	 * @return
	 */
	public static String elementToString(Element el) {	
		Source source = new DOMSource(el);
		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		TransformerFactory factory = TransformerFactory.newInstance();
		try {
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
		} catch ( Exception e ) {
			logger.fatal(e.getMessage());
		}
		return stringWriter.getBuffer().toString();
	}
	
	/**
	 * @return
	 */
	public Document getDocument () {
		return this.doc;
	}
	
	/**
	 * @param doc
	 */
	public void setDocument (Document doc) {
		this.doc = doc;
	}
	
	/**
	 * @return
	 */
	public Document createDocument () {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		DOMImplementation di = db.getDOMImplementation();		
        Document doc = di.createDocument(null, "nexml", null);
        return doc;
	}
	
	/**
	 * @param tag
	 * @param doc
	 * @return
	 */
	public static Element createElement(String tag,Document doc) {
		if ( doc == null ) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = null;
			try {
				db = dbf.newDocumentBuilder();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			DOMImplementation di = db.getDOMImplementation();		
	        doc = di.createDocument(null, "nexml", null);
		}
        Element el = doc.createElement(tag);
        return el;		
	}
	
	/**
	 * @param tag
	 * @param attributes
	 * @param doc
	 * @return
	 */
	public static Element createElement(String tag,Map attributes,Document doc) {
		Element el = createElement(tag,doc);
        if ( attributes != null ) {
	        Object[] keys = attributes.keySet().toArray();
	        for ( int i = 0; i < keys.length; i++ ) {
	        	el.setAttribute((String)keys[i], (String)attributes.get(keys[i]));
	        }
        }
        return el;
	}
	
	/**
	 * @param tag
	 * @param attributes
	 * @param text
	 * @param doc
	 * @return
	 */
	public static Element createElement(String tag, Map attributes, String text,Document doc) {
		Element el = createElement(tag,attributes,doc);
		el.setTextContent(text);
		return el;
	}
	
	/**
	 * @param tag
	 * @param text
	 * @param doc
	 * @return
	 */
	public static Element createElement(String tag,String text,Document doc) {
		Element el = createElement(tag,null,text,doc);
		return el;
	}
	
	/**
	 * @param tag
	 */
	public void setTag(String tag) {
		this.tag = tag;
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
		if ( this.attributes == null ) {
			this.attributes = new HashMap();
		}
		this.attributes.put(key, value);
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
		return this.tag;
	}
	
	/**
	 * @param closeMe
	 * @return
	 * @throws ObjectMismatch
	 */
	public String getXmlTag(boolean closeMe) throws ObjectMismatch {
		Map attrs = this.getAttributes();
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
	
	/**
	 * @return
	 */
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
	
	/**
	 * @return
	 */
	public String getRootCloseTag() {
		return "</nex:nexml>";
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
	
	/**
	 * @return
	 * @throws ObjectMismatch
	 */
	public Element toXmlElement () throws ObjectMismatch {
		Element theElt = createElement(getTag(),getAttributes(),getDocument());
		Map dict = (Map)getGeneric("dict");
		if ( dict != null ) {
			theElt.appendChild(dictToXmlElement(dict));
		}
		if ( this instanceof Listable ) {
			Containable[] ents = ((Listable)this).getEntities();
			Document doc = getDocument();
			if ( doc == null ) {
				doc = createDocument();
				setDocument(doc);
			}
			for ( int i = 0; i < ents.length; i++ ) {
				ents[i].setDocument(doc);
				theElt.appendChild(ents[i].toXmlElement());
			}
		}
		return theElt;
	}
	
	/**
	 * @param dict
	 * @return
	 */
	public Element dictToXmlElement(Map dict) {
		Element currentDict = createElement("dict",getDocument());
		Object[] keys = dict.keySet().toArray();
		for ( int i = 0; i < keys.length; i++ ) {
			String key = (String)keys[i];
			Vector value = (Vector)dict.get(keys[i]);
			currentDict.appendChild(createElement("key",key,getDocument()));
			String valueType = (String)value.get(0);
			if ( valueType.equals("dict") ) {
				Map childDict = (Map)value.get(1);
				currentDict.appendChild(dictToXmlElement(childDict));
			}
			else if ( valueType.indexOf("vector") > 0 ){
				StringBuffer sb = new StringBuffer();
				for ( int j = 1; j < value.size(); j++ ) {
					sb.append(value.get(j));
					if ( j < ( value.size() - 1 ) ) {
						sb.append(' ');
					}
				}
				currentDict.appendChild(createElement(valueType,sb.toString(),getDocument()));
			}
			else if ( valueType.equals("any") ) {
				currentDict.appendChild((Element)value.get(1));				
			}
			else {
				currentDict.appendChild(createElement(valueType,(String)value.get(1),getDocument()));
			}
		}		
		return currentDict;
	}
	
	
	/**
	 * @return
	 * @throws ObjectMismatch
	 */
	public final String toXml () throws ObjectMismatch {
		Element theElt = toXmlElement();
		return elementToString(theElt);	
	}
}
