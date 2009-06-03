package mesquite.nexml.InterpretNEXML;

import mesquite.lib.NameReference;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;

public class TaxonLinkHandler extends PredicateHandlerImpl {

	public TaxonLinkHandler(Object subject, String predicate, Object value) {
		super(subject, predicate, value);
		if ( subject instanceof Taxon ) {
			Taxa taxa = ((Taxon)subject).getTaxa();
			((Taxon)subject).setLink(value.toString());
			int taxonIndex = ((Taxon)subject).getNumber();
			NameReference nr = new NameReference("hyperlink");
			taxa.setAssociatedObject(nr,taxonIndex,value.toString());
		}
	}
	
	public String getProperty() {
		return "msq:taxonUID";
	}

}
