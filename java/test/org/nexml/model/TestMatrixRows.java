package org.nexml.model;

import java.net.URI;

import org.junit.Test;


public class TestMatrixRows {
	URI tbTermsUri = URI.create("http://purl.org/phylo/treebase/2.0/terms#");

	@Test
	public void testCreateDNAMatrix () {
		
		// create a document
		Document doc = DocumentFactory.safeCreateDocument();
        
        // create a taxa block with 5 taxa
        OTUs otus = doc.createOTUs();
        for ( int i = 1; i <= 5; i++ ) {
        	OTU otu = otus.createOTU();
        	otu.setLabel("Taxon_"+i);
        }		
        
        // create a DNA matrix for the taxa
        MolecularMatrix matrix = doc.createMolecularMatrix(otus, MolecularMatrix.DNA);
        CharacterStateSet stateSet = matrix.getCharacterStateSet();
      
        
        // populate
        String[] symbols = { "A", "C", "G", "T" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	StringBuffer sb = new StringBuffer();
        	
        	for ( int i = 0; i <= 9; i++ ) {
        		Long l = new Long(Math.round(Math.random()*(symbols.length-1)));
        		sb.append(symbols[l.intValue()]);
        		matrix.createCharacter(stateSet);
        	}
        	MatrixRow<CharacterState> row = matrix.getRowObject(otu);
        	Annotation annotation = row.addAnnotationValue("tb:rowSegment", tbTermsUri,new String());
        	annotation.addAnnotationValue("tb:segmentStart", tbTermsUri, 1);
        	annotation.addAnnotationValue("tb:segmentEnd", tbTermsUri, 10);
        	annotation.addAnnotationValue("tb:accession", tbTermsUri, "DC567254");
        	row.setSeq(sb.toString());
        }
        
        // print document
        System.out.println(doc.getXmlString());

	}
	
}
