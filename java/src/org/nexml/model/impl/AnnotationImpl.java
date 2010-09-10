package org.nexml.model.impl;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.nexml.model.Annotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AnnotationImpl extends AnnotatableImpl implements Annotation {
	private Object mValue;
	
	@SuppressWarnings("serial")
	private Map<String,Class<?>> classForXsdType = new HashMap<String, Class<?>>() {{
		put(XSD_PRE+":decimal", BigDecimal.class);
		put(XSD_PRE+":integer", BigInteger.class);
		put(XSD_PRE+":boolean", Boolean.class);
		put(XSD_PRE+":byte", Byte.class);
		put(XSD_PRE+":QName", QName.class);
		put(XSD_PRE+":double", Double.class);
		put(XSD_PRE+":float", Float.class);
		put(XSD_PRE+":long", Long.class);
		put(XSD_PRE+":short", Short.class);
		put(XSD_PRE+":string", String.class);
		put(XSD_PRE+":char", CharWrapper.class);
		put(XSD_PRE+":dateTime", DateTimeWrapper.class);
		put(XSD_PRE+":base64Binary", Base64BinaryWrapper.class);
		put(XSD_PRE+":duration", DurationWrapper.class);
		
	}};
	
	@SuppressWarnings("serial")
	private Map<Class<?>,String> xsdTypeForClass = new HashMap<Class<?>,String>() {{
		for ( String xsdType : classForXsdType.keySet() ) {
			put(classForXsdType.get(xsdType), xsdType);
		}
		put(Date.class, XSD_PRE+":dateTime");
		put(Calendar.class, XSD_PRE+":dateTime");
		put(UUID.class, XSD_PRE+":string");
		put(java.awt.Image.class, XSD_PRE+":base64Binary");
		put(Duration.class, XSD_PRE+":duration");
		put(java.lang.Character.class, XSD_PRE+":char");
		put(Source.class, XSD_PRE+":base64Binary");
	}};
	
	/**
	 * Class version of {@code getTagName()}.
	 * 
	 * @return the tag name.
	 */
	static String getTagNameClass() {
		return "meta";
	}	
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.impl.NexmlWritableImpl#getTagName()
	 */
	@Override
	String getTagName() {
		return getTagNameClass();
	}	

    /**
     * Protected constructors are intended for recursive parsing, i.e.
     * starting from the root element (which maps onto DocumentImpl) we
     * traverse the element tree such that for every child element that maps
     * onto an Impl class the containing class calls that child's protected
     * constructor, passes in the element of the child. From there the 
     * child takes over, populates itself and calls the protected 
     * constructors of its children. These should probably be protected
     * because there is all sorts of opportunity for outsiders to call
     * these in the wrong context, passing in the wrong elements etc.
     * @param document the containing DOM document object. Every Impl 
     * class needs a reference to this so that it can create DOM element
     * objects
     * @param element the equivalent NeXML element (e.g. for OTUsImpl, it's
     * the <otus/> element)
     * @author rvosa
     */
    protected AnnotationImpl(Document document, Element element) {
        super(document, element);
        if ( element.hasAttribute("content") ) {
            String datatype = element.getAttribute("datatype");
            String content = element.getAttribute("content");  
            if ( classForXsdType.containsKey(datatype) ) {
            	try {
					mValue = classForXsdType.get(datatype).getConstructor(String.class).newInstance(content);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
            }
	        else if ( datatype.equals(XSD_PRE+":anySimpleType") ) {
	        	mValue = new SimpleObject(content);
	        }
        }
        else if ( element.hasAttribute("href") ) {
        	mValue = URI.create(element.getAttribute("href"));
        }
        // there is no content or href
        else {
        	// maybe recursive meta?
        	List<Element> metas = getChildrenByTagName(getElement(),getTagName());
        	if ( metas.size() > 0 ) {
        		Set<Annotation> nestedAnnotations = new HashSet<Annotation>();
        		for ( Element meta : metas ) {
        			Annotation anno = new AnnotationImpl(getDocument(),meta);
        			nestedAnnotations.add(anno);
        		}
        		mValue = nestedAnnotations;
        	}
        	else {
        		NodeList literalXmlNodes = element.getChildNodes();
        		mValue = literalXmlNodes;
        	}        	
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
    }
    
    protected AnnotationImpl() {}

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
     * @see org.nexml.model.Annotation#getRel()
     */
    public String getRel() {
    	return getElement().getAttribute("rel");
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setRel(java.lang.String)
     */
    public void setRel(String rel) {
    	getElement().setAttribute("rel",rel);
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Object)
     */  
    public void setValue(Set<Annotation> value) {
    	mValue = value;
		getElement().setAttribute("xsi:type","nex:ResourceMeta"); 
		for ( Annotation annotation : value ) {
			getElement().appendChild(((AnnotationImpl)annotation).getElement());
		}
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(org.nexml.model.Annotation)
     */
    public void setValue(Annotation value) {
    	mValue = value;
    	getElement().setAttribute("xsi:type","nex:ResourceMeta");
    	getElement().appendChild(((AnnotationImpl)value).getElement());
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        mValue = value;
    	getElement().setAttribute("datatype", "xsd:anySimpleType");
    	getElement().setAttribute("xsi:type","nex:LiteralMeta");
    	if ( null != value ) {
    		getElement().setAttribute("content",value.toString());
    	}
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(org.w3c.dom.NodeList)
     */
    public void setValue(NodeList value) {
    	mValue = value;
		getElement().setAttribute("datatype", "rdf:XMLLiteral");
		getElement().setAttribute("xsi:type","nex:LiteralMeta");
		for ( int i = 0; i < ((NodeList)value).getLength(); i++ ) {
			Node node = ((NodeList)value).item(i);
			if ( node.getOwnerDocument() != getDocument() ) {
				node = getDocument().importNode(node,true);
			}				
			getElement().appendChild(node);
		}		    	    	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(org.w3c.dom.Node)
     */
    public void setValue(Node value) {
        mValue = value;
        getElement().setAttribute("datatype", "rdf:XMLLiteral");
        getElement().setAttribute("xsi:type","nex:LiteralMeta");
        if ( value.getOwnerDocument() != getDocument() ) {
            value = getDocument().importNode(value,true);
        }               
        getElement().appendChild(value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(org.w3c.dom.Element)
     */
    public void setValue(Element value) {
    	mValue = value;
		getElement().setAttribute("datatype", "rdf:XMLLiteral");
		getElement().setAttribute("xsi:type","nex:LiteralMeta");
		if ( value.getOwnerDocument() != getDocument() ) {
			value = (Element) getDocument().importNode(value,true);
		}
		getElement().appendChild(value);    	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.net.URI)
     */
    public void setValue(URI value) {
    	mValue = value;    	
    	String valueString = value.toString();
    	
    	if ( null != getBaseURI() ) {
	    	String baseURIString = getBaseURI().toString();
	    	int baseURILength = baseURIString.length();
	    	
	    	// truncate stringified value to omit base uri, if matches
	    	if ( baseURILength > 0 ) {
		    	if ( valueString.startsWith(baseURIString) ) {
		    		getElement().setAttribute("href", valueString.substring(baseURILength));
		    	}
		    	else {
		    		getElement().setAttribute("href", valueString);
		    	}
	    	}
	    	else {
	    		getElement().setAttribute("href", valueString);			
	    	}	    	
    	}
    	else {
    		getElement().setAttribute("href", valueString);			
    	}
    	getElement().setAttribute("xsi:type","nex:ResourceMeta");
    }
    
    /**
     * Helper method to fill out the boiler plate for atomic literal meta objects
     * @param datatype schema datatype, i.e. a CURIE such as xsd:string
     * @param value a marshalled version of the object
     */
    private void setValueAttributes(String datatype,Object value) {
    	mValue = value;
    	getElement().setAttribute("datatype",datatype);
		getElement().setAttribute("xsi:type","nex:LiteralMeta");
		//getElement().appendChild(getDocument().createTextNode(value.toString()));
		// XXX changing this to conform to:
		// * http://www.xml.com/pub/a/2007/02/14/introducing-rdfa.html
		// * http://dublincore.org/documents/dcq-html/
		// * http://en.wikipedia.org/wiki/RDFa#XHTML.2BRDFa_1.0_example
		getElement().setAttribute("content",value.toString());
    	
    }//base
    
    /** 
     * these setters are the default JAXB mappings, see
     * http://java.sun.com/javaee/5/docs/tutorial/doc/bnazq.html#bnazt 
     * */
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Byte[])
     */
    public void setValue(Byte[] value) {    
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);	
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.math.BigDecimal)
     */
    public void setValue(BigDecimal value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.math.BigInteger)
     */
    public void setValue(BigInteger value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value); 	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Boolean)
     */
    public void setValue(Boolean value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Byte)
     */
    public void setValue(Byte value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.util.Calendar)
     */
    public void setValue(Calendar value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);    	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.util.Date)
     */
    public void setValue(Date value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Double)
     */
    public void setValue(Double value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Float)
     */
    public void setValue(Float value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Integer)
     */
    public void setValue(Integer value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);     	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Long)
     */
    public void setValue(Long value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Short)
     */
    public void setValue(Short value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.util.UUID)
     */
    public void setValue(UUID value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.String)
     */
    public void setValue(String value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.awt.Image)
     */
    public void setValue(java.awt.Image value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);  	
    }   
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.datatype.Duration)
     */
    public void setValue(Duration value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.namespace.QName)
     */
    public void setValue(QName value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Character)
     */
    public void setValue(java.lang.Character value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.transform.Source)
     */
    public void setValue(Source value) {
    	setValueAttributes(xsdTypeForClass.get(value.getClass()),value);    	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.datatype.XMLGregorianCalendar)
     */
    public void setValue(XMLGregorianCalendar value) {
    	//setValueAttributes("xsd:anySimpleType",value);   
       	mValue = value;
    	getElement().setAttribute("datatype","xsd:anySimpleType");
		getElement().setAttribute("xsi:type","nex:LiteralMeta");
		//getElement().appendChild(getDocument().createTextNode(value.toString()));
		// XXX changing this to conform to:
		// * http://www.xml.com/pub/a/2007/02/14/introducing-rdfa.html
		// * http://dublincore.org/documents/dcq-html/
		// * http://en.wikipedia.org/wiki/RDFa#XHTML.2BRDFa_1.0_example
		getElement().setAttribute("content",value.toXMLFormat());    	
    }  
    
    /**
     * A wrapper for xsd:anySimpleType holding an opaque string
     * @author rvosa
     *
     */
    class SimpleObject {
    	private String mValue;
    	public SimpleObject(String value) {
    		mValue = value;
    	}
    	public String toString() {
    		return mValue;
    	}
    }
    
    class CharWrapper {
    	private java.lang.Character mValue;
    	public CharWrapper(String value) {
    		mValue = new java.lang.Character(value.charAt(0));
    	}
    	public String toString() {
    		return mValue.toString();
    	}
    }
    
    class DateTimeWrapper {
    	private Date mValue;
    	public DateTimeWrapper(String value) {
    	    try {
    			mValue = DateFormat.getDateTimeInstance().parse(value);
    		} catch (ParseException e) {
    			e.printStackTrace();
    		}
    	}
    	public String toString() {
    		return mValue.toString();
    	}
    }
    
    class Base64BinaryWrapper {
    	private Byte[] mValue;
    	public Base64BinaryWrapper(String value) {
    		byte[] bytes = value.getBytes();
        	mValue = new Byte[bytes.length];
        	for ( int i = 0; i < bytes.length; i++ ) {
        		((Byte[])mValue)[i] = bytes[i];
        	}    		
    	}
    	public String toString() {
    		StringBuffer sb = new StringBuffer();
    		for ( int i = 0; i < mValue.length; i++ ) {
    			sb.append(mValue[i]);
    		}
    		return sb.toString();
    	}
    }
    
    class DurationWrapper {
    	private Duration mValue;
    	public DurationWrapper(String value) {
        	DatatypeFactory dtf = null;
			try {
				dtf = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mValue = dtf.newDuration(value);
    	}
    	public String toString() {
    		return mValue.toString();
    	}
    }
	

	@Override
	public boolean isValueMapped(Class<?> valueClass) {
		return xsdTypeForClass.containsKey(valueClass);
	}
	
	public String getXsdType() {
		Class<?> valueClass = mValue.getClass();
		if ( xsdTypeForClass.containsKey(valueClass) ) {
			return xsdTypeForClass.get(mValue.getClass());
		}
		else {
			return XSD_PRE+":anySimpleType";
		}
	}
    
	public URI getPredicateNamespace() {
		String property = getProperty();
		if ( null == property || "".equals(property) ) {
			property = getRel();
		}
		String[] parts = property.split(":");
		String prefix = parts[0];
		if ( null != prefix && ! "".equals(prefix) ) {
			Element elt = getElement();
			String xmlns = elt.getAttribute(XMLNS_PRE+":"+prefix); 
			while ( null == xmlns || "".equals(xmlns) ) {
				if ( elt.getParentNode() instanceof Element ) {
					elt = (Element)elt.getParentNode();
					xmlns = elt.getAttribute(XMLNS_PRE+":"+prefix);
				}
				else {
					break;
				}
			}
			if ( null != xmlns && ! "".equals(xmlns) ) {
				return URI.create(xmlns);
			}
		}
		return null;
	}
	
}
