package org.nexml.model.impl;

import org.nexml.model.Annotation;

public class AnnotationImpl extends NexmlWritableImpl implements Annotation {
    
    //private String mProperty;
    private Object mValue;
    
    public AnnotationImpl(String property, Object value) {
        
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
    }

}
