package org.nexml.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestCreateMolecular {
	
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
        
        // create 10 characters
        Set<Character> characters = new HashSet<Character>(10);
        for ( int i = 0; i < 9; i++ ) {
        	characters.add(matrix.createCharacter(stateSet));
        }
        
        // populate
        String[] symbols = { "A", "C", "G", "T" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	for ( Character character : characters ) {
        		Long l = new Long(Math.round(Math.random()*(symbols.length-1)));
        		matrix.getCell(otu, character).setValue(matrix.parseSymbol(symbols[l.intValue()]));
        	}
        }
        
        // print document
        System.out.println(doc.getXmlString());

	}
	
	@Test
	public void testCreateRNAMatrix () {
		
		// create a document
		Document doc = DocumentFactory.safeCreateDocument();
        
        // create a taxa block with 5 taxa
        OTUs otus = doc.createOTUs();
        for ( int i = 1; i <= 5; i++ ) {
        	OTU otu = otus.createOTU();
        	otu.setLabel("Taxon_"+i);
        }		
        
        // create a RNA matrix for the taxa
        MolecularMatrix matrix = doc.createMolecularMatrix(otus, MolecularMatrix.RNA);
        CharacterStateSet stateSet = matrix.getCharacterStateSet();
        
        // create 10 characters
        Set<Character> characters = new HashSet<Character>(10);
        for ( int i = 0; i < 9; i++ ) {
        	characters.add(matrix.createCharacter(stateSet));
        }
        
        // populate
        String[] symbols = { "A", "C", "G", "U" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	for ( Character character : characters ) {
        		Long l = new Long(Math.round(Math.random()*(symbols.length-1)));
        		matrix.getCell(otu, character).setValue(matrix.parseSymbol(symbols[l.intValue()]));
        	}
        }
        
        // print document
        System.out.println(doc.getXmlString());

	}	

	@Test
	public void testCreateProteinMatrix () {
		
		// create a document
		Document doc = DocumentFactory.safeCreateDocument();
        
        // create a taxa block with 5 taxa
        OTUs otus = doc.createOTUs();
        for ( int i = 1; i <= 5; i++ ) {
        	OTU otu = otus.createOTU();
        	otu.setLabel("Taxon_"+i);
        }		
        
        // create a protein matrix for the taxa
        MolecularMatrix matrix = doc.createMolecularMatrix(otus, MolecularMatrix.Protein);
        CharacterStateSet stateSet = matrix.getCharacterStateSet();
        
        // create 10 characters
        Set<Character> characters = new HashSet<Character>(10);
        for ( int i = 0; i < 9; i++ ) {
        	characters.add(matrix.createCharacter(stateSet));
        }
        
        // populate
        String[] symbols = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	for ( Character character : characters ) {
        		Long l = new Long(Math.round(Math.random()*(symbols.length-1)));
        		matrix.getCell(otu, character).setValue(matrix.parseSymbol(symbols[l.intValue()]));
        	}
        }
        
        // print document
        System.out.println(doc.getXmlString());

	}	
	
	@Test
	public void testCreateThreeTypes () {
		// create a document
		Document doc = DocumentFactory.safeCreateDocument();
        
        // create a taxa block with 5 taxa
        OTUs otus = doc.createOTUs();
        for ( int i = 1; i <= 5; i++ ) {
        	OTU otu = otus.createOTU();
        	otu.setLabel("Taxon_"+i);
        }		
        
        // create a DNA matrix for the taxa
        MolecularMatrix dnaMatrix = doc.createMolecularMatrix(otus, MolecularMatrix.DNA);
        CharacterStateSet dnaStateSet = dnaMatrix.getCharacterStateSet();
        
        // create 10 characters
        Set<Character> dnaCharacters = new HashSet<Character>(10);
        for ( int i = 0; i < 9; i++ ) {
        	dnaCharacters.add(dnaMatrix.createCharacter(dnaStateSet));
        }
        
        // populate
        String[] dnaSymbols = { "A", "C", "G", "T" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	for ( Character character : dnaCharacters ) {
        		Long l = new Long(Math.round(Math.random()*(dnaSymbols.length-1)));
        		dnaMatrix.getCell(otu, character).setValue(dnaMatrix.parseSymbol(dnaSymbols[l.intValue()]));
        	}
        }
		
        // create a RNA matrix for the taxa
        MolecularMatrix rnaMatrix = doc.createMolecularMatrix(otus, MolecularMatrix.RNA);
        CharacterStateSet rnaStateSet = rnaMatrix.getCharacterStateSet();
        
        // create 10 characters
        Set<Character> rnaCharacters = new HashSet<Character>(10);
        for ( int i = 0; i < 9; i++ ) {
        	rnaCharacters.add(rnaMatrix.createCharacter(rnaStateSet));
        }
        
        // populate
        String[] rnaSymbols = { "A", "C", "G", "U" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	for ( Character character : rnaCharacters ) {
        		Long l = new Long(Math.round(Math.random()*(rnaSymbols.length-1)));
        		rnaMatrix.getCell(otu, character).setValue(rnaMatrix.parseSymbol(rnaSymbols[l.intValue()]));
        	}
        }
        
        // create a protein matrix for the taxa
        MolecularMatrix proteinMatrix = doc.createMolecularMatrix(otus, MolecularMatrix.Protein);
        CharacterStateSet proteinStateSet = proteinMatrix.getCharacterStateSet();
        
        // create 10 characters
        Set<Character> proteinCharacters = new HashSet<Character>(10);
        for ( int i = 0; i < 9; i++ ) {
        	proteinCharacters.add(proteinMatrix.createCharacter(proteinStateSet));
        }
        
        // populate
        String[] proteinSymbols = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        for ( OTU otu : otus.getAllOTUs() ) {
        	for ( Character character : proteinCharacters ) {
        		Long l = new Long(Math.round(Math.random()*(proteinSymbols.length-1)));
        		proteinMatrix.getCell(otu, character).setValue(proteinMatrix.parseSymbol(proteinSymbols[l.intValue()]));
        	}
        }
        
        // print document
        System.out.println(doc.getXmlString());
        
		
	}
	
}
