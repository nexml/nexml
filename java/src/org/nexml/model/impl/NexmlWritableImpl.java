package org.nexml.model.impl;
import java.util.UUID;

import org.nexml.model.NexmlWritable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public abstract class NexmlWritableImpl implements NexmlWritable {
    private Document mDocument = null;
    private String mId;
    private Element mElement;
    protected static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    protected static String XSI_PREFIX = "xsi";
    protected static String NEX_PREFIX = "nex";

    protected NexmlWritableImpl() {
    }
    
    public NexmlWritableImpl(Document document) {
        mId = "a" + UUID.randomUUID();
        mDocument = document;
        mElement = document.createElementNS(DEFAULT_NAMESPACE, getTagName());
        getElement().setAttribute("id", getId());
    }   
    
    public NexmlWritableImpl(Document document,Element element) {
        mId = "a" + UUID.randomUUID();
        mDocument = document;
        mElement = element;     
        getElement().setAttribute("id", getId());
    }   
    
    protected final Document getDocument() {
        return mDocument;
    }
    
    public final Element getElement() {
        return mElement;
    }
    
    public String getLabel() {
        return getElement().getAttribute("label");
    }

    public void setLabel(String label) {
        getElement().setAttribute("label", label);
    }
    
    public String getId() { 
        return mId;
    }
    
    abstract String getTagName();
        
}
