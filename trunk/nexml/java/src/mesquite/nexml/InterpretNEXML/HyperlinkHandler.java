package mesquite.nexml.InterpretNEXML;

import java.net.URI;

public class HyperlinkHandler extends PredicateHandlerImpl {
	private Object mValue;

	public HyperlinkHandler(Object subject,String predicate, Object value) {
		super(subject,predicate, value);
	}
	
	String getPredicate() {
		return "has_External_Reference";
	}	
	
	public String getProperty() {
		return getPrefix() + ":" + getPredicate();
	}

	public Object getValue() {
		URI uri = null;
		try {
			uri = new URI(mValue.toString());
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return uri;
	}

	boolean getPropertyIsRel() {
		return true;
	}
	
	public String getURIString () {
		 return "http://evolutionaryontology.org#";
	}
	
	public void setValue(Object value) {
		mValue = value;		
	}
	
	String getPrefix () {
		return "cdao";
	}	

}
