package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class AnnotatableImpl extends NexmlWritableImpl implements Annotatable {
    
    private Set<Annotation> mAnnotations;
    
    public AnnotatableImpl(Document document, Element element) {
        super(document, element);
    }

    public AnnotatableImpl(Document document) {
        super(document);
    }
    
    protected AnnotatableImpl() {}

    public Set<Object> getAnnotationValues(String property) {
        //TODO
           return null;
       }
           
       public void addAnnotationValue(String property, Object value) {
           AnnotationImpl annotation = new AnnotationImpl(property, value);
           mAnnotations.add(annotation);
           getElement().appendChild(annotation.getElement());
       }
       
}
