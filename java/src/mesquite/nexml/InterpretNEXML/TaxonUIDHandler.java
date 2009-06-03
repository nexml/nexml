package mesquite.nexml.InterpretNEXML;

import mesquite.lib.Taxon;

public class TaxonUIDHandler extends PredicateHandlerImpl {

	public TaxonUIDHandler(Object subject, String predicate, Object value) {
		super(subject, predicate, value);
		if ( subject instanceof Taxon ) {
			//((Taxon)subject).setUniqueID(value.toString());
		}
	}
	
	public String getProperty () {
		return "msq:taxonUID";
	}

}
