package org.nexml.model;

public interface Document extends NexmlWritable {
	Tree createTree();

	Network createNetwork();

	OTUs createOTUs();

	OTU createOTU();
	
	Matrix createMatrix();
	
	Character createCharacter();
}
