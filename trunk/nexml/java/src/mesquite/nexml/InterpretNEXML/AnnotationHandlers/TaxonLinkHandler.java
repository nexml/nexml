package mesquite.nexml.InterpretNEXML.AnnotationHandlers;

import org.nexml.model.Annotatable;
import org.nexml.model.Annotation;

import mesquite.lib.Associable;
import mesquite.lib.Listable;
import mesquite.lib.NameReference;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.nexml.InterpretNEXML.Constants;

public class TaxonLinkHandler extends PredicateHandlerImpl {

	public TaxonLinkHandler(Annotatable annotatable,Annotation annotation) {
		super(annotatable,annotation);
	}
	
	public String getProperty() {
		return Constants.TaxonUID;
	}
	
	public void read(Associable associable,Listable listable,int index) {
		Taxa taxa = (Taxa)associable;
		Taxon taxon = (Taxon)listable;
		taxon.setLink(getValue().toString());
		NameReference nr = new NameReference("hyperlink");
		taxa.setAssociatedObject(nr,index,getValue().toString());
		
	}

}
