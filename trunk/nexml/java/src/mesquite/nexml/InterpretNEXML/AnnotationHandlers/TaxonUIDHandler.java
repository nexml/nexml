package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import mesquite.nexml.InterpretNEXML.Constants;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;

public class TaxonUIDHandler extends PredicateHandlerImpl {

	public TaxonUIDHandler(Annotatable annotatable, Annotation annotation) {
		super(annotatable, annotation);
	}
	
	public String getProperty () {
		return Constants.TaxonUID;
	}

}
