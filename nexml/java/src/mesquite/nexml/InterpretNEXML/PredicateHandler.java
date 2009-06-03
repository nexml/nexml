package mesquite.nexml.InterpretNEXML;

import java.net.URI;

public abstract class PredicateHandler {
	abstract Object getSubject();
	abstract void setSubject(Object subject);
	
	abstract Object getValue();
	abstract void setValue(Object value);
	
	abstract String getPrefix ();
	abstract void setPrefix(String prefix);
	
	abstract String getPredicate();
	abstract void setPredicate(String predicate);	
	
	abstract boolean getPropertyIsRel();
	abstract void setPropertyIsRel(boolean propertyIsRel);
	
	abstract String getURIString();
	
	public PredicateHandler(Object subject,String predicate,Object object) {
		setSubject(subject);
		setValue(object);
		setPredicate(predicate);
	}
	String getProperty() {
		return getPrefix () + ":" + getPredicate();
	}
		
	public URI getURI () {
		URI uri = null;
		try {
			uri = new URI(getURIString());
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return uri;
	}
}
