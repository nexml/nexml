package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import java.net.URI;

import mesquite.lib.Listable;

public class AnnotationWrapper implements Listable {
	private String mName = null;
	private Object mObject = null;
	private URI mURI = null;

	@Override
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public Object getValue() {
		return mObject;
	}
	
	public void setValue(Object object) {
		mObject = object;
	}
	
	public URI getPredicateNamespace() {
		return mURI;
	}
	
	public void setPredicateNamespace(URI uri) {
		mURI = uri;
	}

}
