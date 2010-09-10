/**
 * 
 */
package mesquite.nexml.InterpretNEXML.NexmlReader;

import mesquite.lib.EmployerEmployee;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.duties.TaxaManager;

import org.nexml.model.Annotatable;
import org.nexml.model.OTU;
import org.nexml.model.OTUs;

/**
 * @author rvosa
 *
 */
public class NexmlOTUsBlockReader extends NexmlBlockReader {

	/**
	 * @param employerEmployee
	 */
	public NexmlOTUsBlockReader(EmployerEmployee employerEmployee) {
		super(employerEmployee);
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockReader#readBlock(mesquite.lib.MesquiteProject, mesquite.lib.MesquiteFile, org.nexml.model.Annotatable, org.nexml.model.OTUs)
	 */
	@Override
	protected FileElement readBlock(MesquiteProject mesProject,
			MesquiteFile mesFile, Annotatable xmlAnnotatable, OTUs xmlNULL) {
		TaxaManager mesTM = (TaxaManager)getEmployerEmployee().findElementManager(Taxa.class);
		OTUs xmlOTUs = (OTUs)xmlAnnotatable;
		int xmlOTUListSize = xmlOTUs.getSegmentCount();
        Taxa mesTaxa = mesTM.makeNewTaxa(mesProject.getTaxas().getUniqueName(xmlOTUs.getId()), xmlOTUListSize, false);
        mesTaxa.setName(xmlOTUs.getLabel());
        mesTaxa.setUniqueID(xmlOTUs.getId()); // for findEquivalentTaxa       
        int mesTaxonIndex = 0;
        for ( OTU xmlOTU : xmlOTUs.getAllOTUs() ) {
        	Taxon mesTaxon = mesTaxa.getTaxon(mesTaxonIndex);
        	mesTaxon.setUniqueID(xmlOTU.getId()); // for findEquivalentTaxon
        	mesTaxon.setName(xmlOTU.getLabel());        	
        	mesTaxonIndex++;
        }	
        return mesTaxa;
	}

	/*
	 * (non-Javadoc)
	 * @see mesquite.nexml.InterpretNEXML.NexmlBlockReader#getThingInMesquiteBlock(mesquite.lib.FileElement, int)
	 */
	@Override
	protected Listable getThingInMesquiteBlock(FileElement mesBlock,
			int index) {
		Taxa mesTaxa = (Taxa)mesBlock;
		Taxon mesTaxon = mesTaxa.getTaxon(index);
		return (Listable)mesTaxon;
	}

}
