package mesquite.nexml.InterpretNEXML.NexmlWriters;

import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Taxa;
import mesquite.nexml.InterpretNEXML.Constants;

import org.nexml.model.Annotatable;
import org.nexml.model.Document;
import org.nexml.model.NexmlWritable;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;

public class NexmlOTUsBlockWriter extends NexmlBlockWriter {

	/**
	 * 
	 * @param employerEmployee
	 */
	public NexmlOTUsBlockWriter(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockWriter#writeBlock(org.nexml.model.Document, mesquite.lib.FileElement)
	 */
	@Override
	protected Annotatable writeBlock(Document xmlProject, FileElement mesBlock) {
		OTUs xmlOTUs = xmlProject.createOTUs();
		Taxa mesTaxa = (Taxa)mesBlock;
		xmlOTUs.setLabel(mesTaxa.getName());
		String mesTaxaUID = mesTaxa.getUniqueID();
		if ( mesTaxaUID == null ) {
			mesTaxaUID = MesquiteTrunk.getUniqueIDBase() + Taxa.totalCreated;
			mesTaxa.setUniqueID(mesTaxaUID);
		}
		xmlOTUs.addAnnotationValue(Constants.TaxaUID,Constants.BaseURI, mesTaxaUID); // for findEquivalentTaxa
		for ( int j = 0; j < mesTaxa.getNumTaxa(); j++ ) {			
			OTU xmlOTU = xmlOTUs.createOTU();
			xmlOTU.setLabel(mesTaxa.getTaxonName(j));				
			xmlOTU.addAnnotationValue(Constants.TaxonUID,Constants.BaseURI,new Integer(j)); // for findEquivalentTaxon
			writeAttributes(mesTaxa.getTaxon(j), xmlOTU);
		}
		return xmlOTUs;
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockWriter#getThingInXmlBlock(org.nexml.model.NexmlWritable, int)
	 */
	@Override
	protected Annotatable getThingInXmlBlock(NexmlWritable xmlBlock, int index) {
		OTUs xmlOTUs = (OTUs)xmlBlock;
		return xmlOTUs.getAllOTUs().get(index);
	}

}
