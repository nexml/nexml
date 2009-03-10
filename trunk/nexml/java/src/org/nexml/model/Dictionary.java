package org.nexml.model;

import java.util.Set;

//TODO: make this generic?
public interface Dictionary extends NexmlWritable {
	public void setAnnotations(Set<Annotation> annotations);

	public Set<Annotation> getAnnotations();
}
