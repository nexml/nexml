package org.nexml.model.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

abstract class AnnotatableImpl extends NexmlWritableImpl implements Annotatable {
    
    private Set<Annotation> mAnnotations;
    
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
    protected AnnotatableImpl(Document document, Element element) {
        super(document, element);
        mAnnotations = new HashSet<Annotation>();
		for ( Element metaElement : getChildrenByTagName(element,AnnotationImpl.getTagNameClass())) {
			AnnotationImpl anno = new AnnotationImpl(document,metaElement);
	        mAnnotations.add(anno);
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
    protected AnnotatableImpl(Document document) {
        super(document);
        mAnnotations = new HashSet<Annotation>();
    }
    
    protected AnnotatableImpl() {}

    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#getAnnotationValues(java.lang.String)
     */
    public Set<Object> getAnnotationValues(String property) {
    	Set<Object> annotationValues = new HashSet<Object>();
    	if ( property == null ) {
    		return annotationValues;
    	}    	
    	for ( Annotation annotation : mAnnotations ) {
    		if ( property.equals(annotation.getProperty()) ) {
    			annotationValues.add(annotation.getValue());
    		}
    	}
    	return annotationValues;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#getRelValues(java.lang.String)
     */
    public Set<Object> getRelValues(String rel) {
    	Set<Object> annotationValues = new HashSet<Object>();
    	if ( rel == null ) {
    		return annotationValues;
    	}    	
    	for ( Annotation annotation : mAnnotations ) {
    		if ( rel.equals(annotation.getRel()) ) {
    			annotationValues.add(annotation.getValue());
    		}
    	}
    	return annotationValues;
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#getAllAnnotations()
     */
    public Set<Annotation> getAllAnnotations() {
		return mAnnotations;
    	
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#getAllAnnotationsForURI(java.net.URI)
     */
    public Set<Annotation> getAllAnnotationsForURI(URI uri) {
    	Set<Annotation> results = new HashSet<Annotation>();
    	String requestedUri = uri.toString();
    	for ( Annotation annotation : getAllAnnotations() ) {
    		String property = annotation.getProperty();
    		if ( null == property ) {
    			property = annotation.getRel();
    		}
    		String[] curie = property.split(":");
    		String prefix = curie[0];
    		String uriString = getNamespaceForPrefix(prefix,((AnnotationImpl)annotation).getElement());
    		if ( uriString.equals(requestedUri) ) {
    			results.add(annotation);
    		}
    	}
		return results;
    	
    } 
    
    private String getNamespaceForPrefix(String prefix,
        org.w3c.dom.Node namespaceContext) {
    	Node parent = namespaceContext;
    	String namespace = null;

		if ( prefix.equals("xml") ) {
			namespace = "http://www.w3.org/XML/1998/namespace";
		}
		else {
			int type;
			while ((null != parent) && (null == namespace) && (((type = parent.getNodeType()) == Node.ELEMENT_NODE) || (type == Node.ENTITY_REFERENCE_NODE))) {
				if (type == Node.ELEMENT_NODE) {
					if ( parent.getNodeName().indexOf(prefix+":") == 0 ) 
						return parent.getNamespaceURI();              
						NamedNodeMap nnm = parent.getAttributes();
						for (int i = 0; i < nnm.getLength(); i++) {
							Node attr = nnm.item(i);
							String aname = attr.getNodeName();
							boolean isPrefix = aname.startsWith("xmlns:");
							if (isPrefix || aname.equals("xmlns")) {
								int index = aname.indexOf(':');
								String p = isPrefix ? aname.substring(index + 1) : "";
								if (p.equals(prefix)) {
									namespace = attr.getNodeValue();
									break;
								}
							}
						}
				}
				parent = parent.getParentNode();
			}
		}
		return namespace;
}    
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#getAnnotations(java.lang.String)
     */
    public Set<Annotation> getAnnotations(String rel) {
    	Set<Annotation> annotations = new HashSet<Annotation>();
    	if ( rel == null ) {
    		return annotations;
    	}
    	for ( Annotation annotation : mAnnotations ) {
    		if ( rel.equals(annotation.getProperty()) || rel.equals(annotation.getRel()) ) {
    			annotations.add(annotation);
    		}
    	}
    	return annotations;
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Object)
     */    
    @SuppressWarnings("unchecked")
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Object value){
    	if ( value instanceof BigDecimal ) {
	    	return addAnnotationValue(property,nameSpaceURI,(BigDecimal)value);
	    }
	    else if ( value instanceof BigInteger ) {
	    	return addAnnotationValue(property,nameSpaceURI,(BigInteger)value);
	    }	   
	    else if ( value instanceof Boolean ) {
	        return addAnnotationValue(property,nameSpaceURI,(Boolean)value);
	    }	
	    else if ( value instanceof Byte ) {
	        return addAnnotationValue(property,nameSpaceURI,(Byte)value);
	    }	
	    else if ( value instanceof Byte[] ) {
	        return addAnnotationValue(property,nameSpaceURI,(Byte[])value);
	    }	
	    else if ( value instanceof Calendar ) {
	        return addAnnotationValue(property,nameSpaceURI,(Calendar)value);
	    }	
	    else if ( value instanceof Date ) {
	        return addAnnotationValue(property,nameSpaceURI,(Date)value);
	    }	
	    else if ( value instanceof Double ) {
	        return addAnnotationValue(property,nameSpaceURI,(Double)value);
	    }	
	    else if ( value instanceof Duration ) {
	        return addAnnotationValue(property,nameSpaceURI,(Duration)value);
	    }
	    else if ( value instanceof Element ) {
	        return addAnnotationValue(property,nameSpaceURI,(Element)value);
	    }	 
	    else if ( value instanceof Float ) {
	        return addAnnotationValue(property,nameSpaceURI,(Float)value);
	    }	   
	    else if ( value instanceof Integer ) {
	        return addAnnotationValue(property,nameSpaceURI,(Integer)value);
	    }
	    else if ( value instanceof java.awt.Image ) {
	        return addAnnotationValue(property,nameSpaceURI,(java.awt.Image)value);
	    }	
	    else if ( value instanceof Long ) {
	        return addAnnotationValue(property,nameSpaceURI,(Long)value);
	    }	
	    else if ( value instanceof NodeList ) {
	        return addAnnotationValue(property,nameSpaceURI,(NodeList)value);
	    }
	    else if ( value instanceof Node ) {
	        return addAnnotationValue(property,nameSpaceURI,(Node)value);
        }
	    else if ( value instanceof QName ) {
	        return addAnnotationValue(property,nameSpaceURI,(QName)value);
	    }	
	    else if ( value instanceof Set ) {
	        return addAnnotationValue(property,nameSpaceURI,(Set<Annotation>)value);
	    }	    
	    else if ( value instanceof Short ) {
	        return addAnnotationValue(property,nameSpaceURI,(Short)value);
	    }	  
	    else if ( value instanceof Source ) {
	        return addAnnotationValue(property,nameSpaceURI,(Source)value);
	    }	 
	    else if ( value instanceof String ) {
	        return addAnnotationValue(property,nameSpaceURI,(String)value);
	    }	    
	    else if ( value instanceof URI ) {
	        return addAnnotationValue(property,nameSpaceURI,(URI)value);
	    }
	    else if ( value instanceof UUID ) {
	        return addAnnotationValue(property,nameSpaceURI,(UUID)value);
	    }	
	    else if ( value instanceof XMLGregorianCalendar ) {
	        return addAnnotationValue(property,nameSpaceURI,(XMLGregorianCalendar)value);
	    }	    
	    // XXX etc.
    	else {
	        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
	        annotation.setValue(value);
	        return annotation;
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, org.w3c.dom.NodeList)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, NodeList value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);
        return annotation;
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, org.w3c.dom.Node)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Node value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);
        return annotation;
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, org.w3c.dom.Element)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Element value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value); 
        return annotation;          
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.net.URI)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, URI value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,true);
        annotation.setValue(value);    
        return annotation;       
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Byte[])
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Byte[] value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.math.BigDecimal)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, BigDecimal value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.math.BigInteger)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, BigInteger value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value); 
        return annotation;          
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Boolean)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Boolean value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);    
        return annotation;       
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Byte)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Byte value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);      
        return annotation;     
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.Calendar)
     */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Calendar value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.Date)
     */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Date value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);   
        return annotation;        
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Double)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Double value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	} 
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Float)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Float value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Integer)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Integer value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Long)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Long value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Short)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Short value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);    
        return annotation;       
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.UUID)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, UUID value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);      
        return annotation;     
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.String)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, String value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.awt.Image)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, java.awt.Image value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);   
        return annotation;        
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.datatype.Duration)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Duration value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	} 
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.namespace.QName)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, QName value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);    
        return annotation;       
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.transform.Source)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, Source value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);  
        return annotation;         
	} 
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.datatype.XMLGregorianCalendar)
	 */
	public Annotation addAnnotationValue(String property, URI nameSpaceURI, XMLGregorianCalendar value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,false);
        annotation.setValue(value);   
        return annotation;        
	}    
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.Set)
	 */
    public Annotation addAnnotationValue(String property, URI nameSpaceURI, Set<Annotation> value) {
    	Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,true);
        annotation.setValue(value); 
        return annotation;     	
    }
    
    /**
     * Helper method to fill out the boiler plate attributes for an annotation
     * @param property the predicate as a CURIE, e.g. cdao:hasSupportValue
     * @param nameSpaceURI the namespace to which the CURIE's prefix is bound
     * @param propertyIsRel the property string is serialized as a rel attribute 
     * @return
     */
    private Annotation addAnnotationValueHelper(String property, URI nameSpaceURI, boolean propertyIsRel) {
        AnnotationImpl annotation = new AnnotationImpl(getDocument());
        if ( null == property ) {
        	return null;
        }
        String[] curie = property.split(":");
        if ( propertyIsRel ) {
        	annotation.setRel(property);
        }
        else {
        	annotation.setProperty(property);
        }
        mAnnotations.add(annotation);
        setNameSpace(getElement(), curie[0], nameSpaceURI.toString());
//        getElement().setAttributeNS(
//        	"http://www.w3.org/2000/xmlns/",
//        	"xmlns:" + curie[0],
//        	nameSpaceURI.toString()
//        );
        if ( getElement().getFirstChild() != null ) {
        	getElement().insertBefore(annotation.getElement(),getElement().getFirstChild());
        }
        else {
        	getElement().appendChild(annotation.getElement());
        }
        if ( this instanceof Annotation ) {
        	getElement().removeAttribute("content");
        	getElement().removeAttribute("datatype");        	
        	getElement().setAttribute("xsi:type","nex:ResourceMeta");
        	if ( getElement().hasAttribute("property") ) {
        		String rel = getElement().getAttribute("property");
        		getElement().setAttribute("rel",rel);
        		getElement().removeAttribute("property");
        	}
        }
        else {
        	String about = getElement().getAttribute("about");
        	if ( null == about || about.equals("") ) {
        		String id = getId();
        		if ( null == id || id.equals("") ) {
        			id = identify(getElement(),false);
        		}
        		getElement().setAttribute("about","#" + id.replace(':', '_'));
        	}
        }
        return annotation;
    }
       
}
