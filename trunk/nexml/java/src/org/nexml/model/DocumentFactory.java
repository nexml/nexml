package org.nexml.model;

import java.io.InputStream;

import org.nexml.model.impl.DocumentImpl;

public class DocumentFactory {
	static public Document createDocument() {
		return new DocumentImpl();
	}
	
	static public Document parse(InputStream is) {
		return null;
	}
	
	static public Document parse(String uri) { 
		return null;
	}
	
	//etc.
}
