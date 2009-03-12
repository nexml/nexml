package org.nexml.model;

import java.util.Set;

public interface Annotatable extends NexmlWritable {
    
    Set<Object> getAnnotationValues(String property);
    
    void setAnnotationValue(String property, Object value);

}
