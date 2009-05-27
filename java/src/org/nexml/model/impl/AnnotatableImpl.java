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
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Object)
     */    
    public void addAnnotationValue(String property, URI nameSpaceURI, Object value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, org.w3c.dom.NodeList)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, NodeList value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, org.w3c.dom.Element)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, Element value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.net.URI)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, URI value){
        AnnotationImpl annotation = new AnnotationImpl(getDocument());
        annotation.setRel(property);
        mAnnotations.add(annotation);
        getElement().appendChild(annotation.getElement());
        getElement().setAttribute("about","#" + getId()); 
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Byte[])
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, Byte[] value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.math.BigDecimal)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, BigDecimal value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.math.BigInteger)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, BigInteger value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Boolean)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, Boolean value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Byte)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, Byte value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    }
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.Calendar)
     */
    public void addAnnotationValue(String property, URI nameSpaceURI, Calendar value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
    } 
    
    /*
     * (non-Javadoc)
     * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.Date)
     */
	public void addAnnotationValue(String property, URI nameSpaceURI, Date value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Double)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Double value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	} 
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Float)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Float value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Integer)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Integer value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Long)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Long value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.Short)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Short value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.UUID)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, UUID value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.lang.String)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, String value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.awt.Image)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, java.awt.Image value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.datatype.Duration)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Duration value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	} 
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.namespace.QName)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, QName value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.transform.Source)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, Source value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	} 
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, javax.xml.datatype.XMLGregorianCalendar)
	 */
	public void addAnnotationValue(String property, URI nameSpaceURI, XMLGregorianCalendar value){
        Annotation annotation = addAnnotationValueHelper(property,nameSpaceURI,value);
        annotation.setValue(value);           
	}    
	
	/*
	 * (non-Javadoc)
	 * @see org.nexml.model.Annotatable#addAnnotationValue(java.lang.String, java.util.Set)
	 */
    public void addAnnotationValue(String property, URI nameSpaceURI, Set<Annotation> value) {
        AnnotationImpl annotation = new AnnotationImpl(getDocument());
        annotation.setRel(property);
        mAnnotations.add(annotation);
        getElement().appendChild(annotation.getElement());
        getElement().setAttribute("about","#" + getId()); 
        annotation.setValue(value);      	
    }
    
    /**
     * Helper method to fill out the boiler plate attributes for an annotation
     * @param property the predicate as a CURIE, e.g. cdao:hasSupportValue
     * @param value the object
     * @return
     */
    private Annotation addAnnotationValueHelper(String property, URI nameSpaceURI, Object value) {
        AnnotationImpl annotation = new AnnotationImpl(getDocument());
        String[] curie = property.split(":");
        annotation.setProperty(property);
        mAnnotations.add(annotation);
        getElement().setAttributeNS(
        	"http://www.w3.org/2000/xmlns/",
        	"xmlns:" + curie[0],
        	nameSpaceURI.toString()
        );
        getElement().appendChild(annotation.getElement());
        getElement().setAttribute("about","#" + getId()); 
        return annotation;
    }
       
}
