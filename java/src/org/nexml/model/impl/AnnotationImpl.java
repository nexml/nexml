package org.nexml.model.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.nexml.model.Annotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnnotationImpl extends NexmlWritableImpl implements Annotation {
	private String[] javaToXmlTypeArray = {
		"java.math.BigDecimal", "xsd:decimal",
		"java.math.BigInteger", "xsd:integer",
		"java.lang.Boolean",    "xsd:boolean",
		"java.lang.Byte",       "xsd:byte",
		"java.util.Calendar",   "xsd:dateTime",
		"java.util.Date",       "xsd:dateTime",
		"java.lang.Double",     "xsd:double",
		"java.lang.Float",      "xsd:float",
		"java.lang.Integer",    "xsd:integer",
		"java.lang.Long",       "xsd:long",
		"java.lang.Short",      "xsd:short",
		"java.lang.String",     "xsd:string",
		"java.util.UUID",       "xsd:string",
		"java.awt.Image",       "xsd:base64Binary",		
		"javax.xml.datatype.Duration", "xsd:duration",
		"javax.xml.namespace.QName",   "xsd:QName",
		"javax.xml.transform.Source",  "xsd:base64Binary",		
		"javax.xml.datatype.XMLGregorianCalendar", "xsd:anySimpleType",	
	};
	private Map<String,String> javaToXmlTypeMap = new HashMap<String,String>();
	private Object mValue;
	
	private void init() {
		for ( int i = 0; i < javaToXmlTypeArray.length; i += 2 ) {
			javaToXmlTypeMap.put(javaToXmlTypeArray[i],javaToXmlTypeArray[i+1]);
		}
		
	}
    
    
    /**
     * Protected constructors that take a DOM document object but not
     * an element object are used for generating new element nodes in
     * a NeXML document. On calling such constructors, a new element
     * is created, which can be retrieved using getElement(). After this
     * step, the Impl class that called this constructor would still 
     * need to attach the element in the proper location (typically
     * as a child element of the class that called the constructor). 
     * @param document a DOM document object
     * @author rvosa
     */    
    protected AnnotationImpl(Document document) {
    	super(document);
    	init();
    }
    
    public AnnotationImpl() {}

    /*
     * (non-Javadoc)
     * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
     */
    @Override
    String getTagName() {
        return "meta";
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#getProperty()
     */
    public String getProperty() {
        return getElement().getAttribute("property");
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#getValue()
     */
    public Object getValue() {
        return mValue;
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setProperty(java.lang.String)
     */
    public void setProperty(String property) {
        getElement().setAttribute("property", property);
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        mValue = value;
        String className = value.getClass().getName();
        if ( javaToXmlTypeMap.containsKey(className) ) {
        	getElement().setAttribute("datatype", javaToXmlTypeMap.get(className));
    		getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type","nex:LiteralMeta");
    		getElement().appendChild(getDocument().createTextNode(value.toString()));
        }
        else if ( value instanceof Byte[] ) {
    		getElement().setAttribute("datatype", "xsd:base64Binary");
    		getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type","nex:LiteralMeta");
    	}
    	else if ( value instanceof Element ) {
    		getElement().setAttribute("datatype", "rdf:Literal");
    		getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type","nex:LiteralMeta");
    		getElement().appendChild((Element)value);
    	}
    	else if ( value instanceof Element[] ) {
    		getElement().setAttribute("datatype", "rdf:Literal");
    		getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type","nex:LiteralMeta");
    		for ( int i = 0; i < ((Element[])value).length; i++ ) {
    			getElement().appendChild(((Element[])value)[i]);
    		}
    	}
    	else if ( value instanceof URI ) {
    		getElement().setAttribute("href", value.toString());
    		getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type","nex:ResourceMeta");
    	}
    	else {
    		getElement().setAttribute("datatype", "xsd:anySimpleType");
    		getElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type","nex:LiteralMeta");
    		getElement().appendChild(getDocument().createTextNode(value.toString()));
    	}
        
    }

}
