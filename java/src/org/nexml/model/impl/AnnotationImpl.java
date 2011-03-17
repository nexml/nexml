package org.nexml.model.impl;

import java.lang.reflect.Constructor;
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
	
	private static String datatype = "datatype";
	private static String href = "href";
	private static String content = "content";
	private static String rel = "rel";
	private static String property = "property";
	private static String anySimpleTypeLocal = "anySimpleType";
	private static String LiteralMeta = NEX_PRE + ":LiteralMeta";
	private static String ResourceMeta = NEX_PRE + ":ResourceMeta";
	private static String XMLLiteral = RDF_PRE + ":XMLLiteral";
	private static String anySimpleType = XSD_PRE + ":" + anySimpleTypeLocal;
	private static QName anySimpleTypeQName = new QName(XS_URI,anySimpleTypeLocal,XSD_PRE);
	
	@SuppressWarnings("serial")
	private static Map<QName,Class<?>> classForXsdType = new HashMap<QName, Class<?>>() {{
		put(new QName(XS_URI,"decimal",XSD_PRE), BigDecimal.class);
		put(new QName(XS_URI,"integer",XSD_PRE), BigInteger.class);
		put(new QName(XS_URI,"boolean",XSD_PRE), Boolean.class);
		put(new QName(XS_URI,"byte",XSD_PRE), Byte.class);
		put(new QName(XS_URI,"QName",XSD_PRE), QName.class);		
		put(new QName(XS_URI,"double",XSD_PRE), Double.class);
		put(new QName(XS_URI,"float",XSD_PRE), Float.class);
		put(new QName(XS_URI,"long",XSD_PRE), Long.class);
		put(new QName(XS_URI,"short",XSD_PRE), Short.class);		
		put(new QName(XS_URI,"string",XSD_PRE), String.class);
		put(new QName(XS_URI,"char",XSD_PRE), CharWrapper.class);		
		put(new QName(XS_URI,"dateTime",XSD_PRE), DateTimeWrapper.class);
		put(new QName(XS_URI,"base64Binary",XSD_PRE), Base64BinaryWrapper.class);		
		put(new QName(XS_URI,"duration",XSD_PRE), DurationWrapper.class);		
	}};
	
	@SuppressWarnings("serial")
	private static Map<Class<?>,QName> xsdTypeForClass = new HashMap<Class<?>,QName>() {{
		for ( QName xsdType : classForXsdType.keySet() ) {
			put(classForXsdType.get(xsdType), xsdType);
		}	
		put(Integer.class,new QName(XS_URI,"integer",XSD_PRE));
		put(Date.class, new QName(XS_URI,"dateTime",XSD_PRE));
		put(Calendar.class, new QName(XS_URI,"dateTime",XSD_PRE));
		put(UUID.class, new QName(XS_URI,"string",XSD_PRE));
		put(java.awt.Image.class, new QName(XS_URI,"base64Binary",XSD_PRE));
		put(Duration.class, new QName(XS_URI,"duration",XSD_PRE));
		put(java.lang.Character.class, new QName(XS_URI,"char",XSD_PRE));
		put(Source.class, new QName(XS_URI,"base64Binary",XSD_PRE));
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
        if ( element.hasAttribute(content) ) {
            String datatypeString = element.getAttribute(datatype);
            String[] curie = datatypeString.split(":");
            QName datatype = new QName(XS_URI,curie[1],curie[0]);
            String contentValue = element.getAttribute(content); 
            Class<?> theClass = getClassForXsdType(datatype);
        	try {
        		Constructor<?>[] constructors = theClass.getConstructors();
        		for ( int i = 0; i < constructors.length; i++ ) {
        			Class<?>[] params = constructors[i].getParameterTypes();
        			if ( params.length == 1 && params[0].equals(String.class) ) {
        				mValue = constructors[i].newInstance(contentValue);
        			}
        			else if ( params.length == 2 && params[0].equals(this.getClass()) && params[1].equals(String.class) ) {
        				mValue = constructors[i].newInstance(this,contentValue);
        			}
        		}
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        else if ( element.hasAttribute(href) ) {
        	mValue = URI.create(element.getAttribute(href));
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
        return getElement().getAttribute(property);
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
    public void setProperty(String propertyValue) {
        getElement().setAttribute(property, propertyValue);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#getRel()
     */
    public String getRel() {
    	return getElement().getAttribute(rel);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setRel(java.lang.String)
     */
    public void setRel(String relValue) {
    	getElement().setAttribute(rel,relValue);
    }

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Object)
     */  
    public void setValue(Set<Annotation> value) {
    	mValue = value;
		getElement().setAttribute(XSI_TYPE,ResourceMeta); 
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
    	getElement().setAttribute(XSI_TYPE,ResourceMeta);
    	getElement().appendChild(((AnnotationImpl)value).getElement());
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        mValue = value;
    	getElement().setAttribute(datatype, anySimpleType);
    	getElement().setAttribute(XSI_TYPE,LiteralMeta);
    	if ( null != value ) {
    		getElement().setAttribute(content,value.toString());
    	}
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(org.w3c.dom.NodeList)
     */
    public void setValue(NodeList value) {
    	mValue = value;
		getElement().setAttribute(datatype, XMLLiteral);
		getElement().setAttribute(XSI_TYPE,LiteralMeta);
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
        getElement().setAttribute(datatype, XMLLiteral);
        getElement().setAttribute(XSI_TYPE,LiteralMeta);
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
		getElement().setAttribute(datatype, XMLLiteral);
		getElement().setAttribute(XSI_TYPE,LiteralMeta);
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
		    		getElement().setAttribute(href, valueString.substring(baseURILength));
		    	}
		    	else {
		    		getElement().setAttribute(href, valueString);
		    	}
	    	}
	    	else {
	    		getElement().setAttribute(href, valueString);			
	    	}	    	
    	}
    	else {
    		getElement().setAttribute(href, valueString);			
    	}
    	getElement().setAttribute(XSI_TYPE,ResourceMeta);
    }
    
    /**
     * Helper method to fill out the boiler plate for atomic literal meta objects
     * @param datatype schema datatype, i.e. a CURIE such as xsd:string
     * @param value a marshalled version of the object
     */
    private void setValueAttributes(QName datatypeQName,Object value) {
    	mValue = value;
    	StringBuffer sb = new StringBuffer();
    	sb.append(datatypeQName.getPrefix()).append(':').append(datatypeQName.getLocalPart());
    	getElement().setAttribute(datatype,sb.toString());
		getElement().setAttribute(XSI_TYPE,LiteralMeta);
		//getElement().appendChild(getDocument().createTextNode(value.toString()));
		// XXX changing this to conform to:
		// * http://www.xml.com/pub/a/2007/02/14/introducing-rdfa.html
		// * http://dublincore.org/documents/dcq-html/
		// * http://en.wikipedia.org/wiki/RDFa#XHTML.2BRDFa_1.0_example
		getElement().setAttribute(content,value.toString());    	
    }
    
    /** 
     * these setters are the default JAXB mappings, see
     * http://java.sun.com/javaee/5/docs/tutorial/doc/bnazq.html#bnazt 
     * */
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Byte[])
     */
    public void setValue(Byte[] value) {    
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);	
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.math.BigDecimal)
     */
    public void setValue(BigDecimal value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.math.BigInteger)
     */
    public void setValue(BigInteger value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value); 	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Boolean)
     */
    public void setValue(Boolean value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Byte)
     */
    public void setValue(Byte value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.util.Calendar)
     */
    public void setValue(Calendar value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);    	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.util.Date)
     */
    public void setValue(Date value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Double)
     */
    public void setValue(Double value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Float)
     */
    public void setValue(Float value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Integer)
     */
    public void setValue(Integer value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);     	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Long)
     */
    public void setValue(Long value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Short)
     */
    public void setValue(Short value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.util.UUID)
     */
    public void setValue(UUID value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.String)
     */
    public void setValue(String value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.awt.Image)
     */
    public void setValue(java.awt.Image value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);  	
    }   
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.datatype.Duration)
     */
    public void setValue(Duration value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.namespace.QName)
     */
    public void setValue(QName value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);      	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(java.lang.Character)
     */
    public void setValue(java.lang.Character value) {
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.transform.Source)
     */
    public void setValue(Source value) {    	
    	setValueAttributes(getXsdTypeForClass(value.getClass()),value);    	
    }    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotation#setValue(javax.xml.datatype.XMLGregorianCalendar)
     */
    public void setValue(XMLGregorianCalendar value) {
       	mValue = value;
    	getElement().setAttribute(datatype,anySimpleType);
		getElement().setAttribute(XSI_TYPE,LiteralMeta);
		//getElement().appendChild(getDocument().createTextNode(value.toString()));
		// XXX changing this to conform to:
		// * http://www.xml.com/pub/a/2007/02/14/introducing-rdfa.html
		// * http://dublincore.org/documents/dcq-html/
		// * http://en.wikipedia.org/wiki/RDFa#XHTML.2BRDFa_1.0_example
		getElement().setAttribute(content,value.toXMLFormat());    	
    }  
    
    /**
     * Test to see if provided class has a mapping to an xsd type
     */
	@Override
	public boolean isValueMapped(Class<?> valueClass) {
		return xsdTypeForClass.containsKey(valueClass);
	}
	
	/**
	 * Returns the value of the "datatype" attribute for the focal
	 * annotation as a QName
	 */
	public QName getXsdType() {
		return getXsdTypeForClass(mValue.getClass());
	}
   
	/**
	 * Gets the namespace URI for the predicate (either the "property" 
	 * or "rel" attribute in the case of annotations with URLs or nested
	 * annotations). Traverses up the element tree if the URI is declared
	 * on an ancestral element.
	 */
	public URI getPredicateNamespace() {
		String property = getProperty();
		if ( isEmpty(property) ) {
			property = getRel();
		}
		String[] parts = property.split(":");
		String prefix = parts[0];
		if ( ! isEmpty(prefix) ) {
			Element elt = getElement();
			String xmlnsAttr = XMLNS_PRE + ":" + prefix;
			String xmlns = elt.getAttribute(xmlnsAttr); 
			while ( isEmpty(xmlns) ) {
				if ( elt.getParentNode() instanceof Element ) {
					elt = (Element)elt.getParentNode();
					xmlns = elt.getAttribute(xmlnsAttr);
				}
				else {
					break;
				}
			}
			if ( ! isEmpty(xmlns) ) {
				return URI.create(xmlns);
			}
		}
		return null;
	}
	
	/**
	 * Test to see if provided string is either null or ""
	 * @param str
	 * @return
	 */
	private static boolean isEmpty(String str) {
		return null == str || "".equals(str);
	}
	
	/**
	 * Given a class name, returns the QName that is to be used
	 * as the value of the "datatype" attribute when serialized.
	 * If no mapping is found, this method first checks if there
	 * is a mapping for a superclass; for example, if we pass in
	 * DurationImpl, we get the mapping back for Duration (i.e.
	 * xsd:duration). If no mapping exists at all, e.g. for a 
	 * SimpleObject, this method returns xsd:anySimpleType.
	 * @param theClass
	 * @return
	 */
	private static QName getXsdTypeForClass(Class<?> theClass) {
		if ( xsdTypeForClass.containsKey(theClass) ) {
			return xsdTypeForClass.get(theClass);
		}
		else {
			QName result = anySimpleTypeQName;
			for ( Class<?> candidateClass : xsdTypeForClass.keySet() ) {
				if ( candidateClass.isAssignableFrom(theClass) ) {
					result = xsdTypeForClass.get(candidateClass);
				}
			}
			return result;
		}
	}
	
	/**
	 * Given a QName, presumably parsed as the value of the "datatype"
	 * attribute of an annotation, returns the corresponding class. If
	 * No such class exists, the SimpleObject class is returned, which
	 * is a simple wrapper around a string, but which ensures that this
	 * string is serialized as an xsd:anySimpleType, not an xsd:String
	 * @param theType
	 * @return
	 */
	private static Class<?> getClassForXsdType(QName theType) {
		if ( classForXsdType.containsKey(theType) ) {
			return classForXsdType.get(theType);
		}
		else {
			return SimpleObject.class;
		}
	}
	
	/* INNER CLASSES
	 * These classes are used to deal with situations where we read an
	 * annotation for which no equivalent java class exists that can be
	 * instantiated with a simple String argument. This is the case for
	 * the following data types:
	 * - xsd:anySimpleType
	 * - xsd:char
	 * - xsd:dateTime
	 * - xsd:base64Binary
	 * - xsd:duration
	 */
	
    /**
     * A wrapper for xsd:anySimpleType holding an opaque string
     * @author rvosa
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
    
    /**
     * Wrapper for xsd:char values
     * @author rvosa
     */
    class CharWrapper {
    	private java.lang.Character mValue;
    	public CharWrapper(String value) {
    		mValue = new java.lang.Character(value.charAt(0));
    	}
    	public String toString() {
    		return mValue.toString();
    	}
    }
    
    /**
     * Wrapper for xsd:dateTime values
     * @author rvosa
     */
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
    
    /**
     * Wrapper for xsd:base64Binary values
     * @author rvosa
     */
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
    
    /**
     * Wrapper for xsd:duration values
     * @author rvosa
     */
    class DurationWrapper {
    	private Duration mValue;
    	public DurationWrapper(String value) {
        	DatatypeFactory dtf = null;
			try {
				dtf = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
        	mValue = dtf.newDuration(value);
    	}
    	public String toString() {
    		return mValue.toString();
    	}
    }	
	
}
