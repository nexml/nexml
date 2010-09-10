package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import java.net.URI;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;

public class HyperlinkHandler extends PredicateHandlerImpl {
	private Object mValue;

	public HyperlinkHandler(Annotatable annotatable, Annotation annotation) {
		super(annotatable, annotation);
	}
	
	public String getPredicate() {
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

	public boolean getPropertyIsRel() {
		return true;
	}
	
	public String getURIString () {
		 return "http://evolutionaryontology.org#";
	}
	
	public void setValue(Object value) {
		mValue = value;		
	}
	
	public String getPrefix () {
		return "cdao";
	}	

}
