package org.nexml.model.impl;

import java.util.Set;

import org.nexml.model.Annotatable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class AnnotatableImpl extends NexmlWritableImpl implements Annotatable {
    
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
           
       public void setAnnotationValue(String property, Object value) {
           //TODO
       }
}
