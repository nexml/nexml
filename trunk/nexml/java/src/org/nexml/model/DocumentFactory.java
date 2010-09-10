package org.nexml.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nexml.model.impl.DocumentImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentFactory {
	
	/**
	 * Creates a new NeXML document, to be populated programmatically.
	 * @return an org.nexml.model.Document object
	 * @throws ParserConfigurationException
	 */
	static public Document createDocument() throws ParserConfigurationException {
		return new DocumentImpl(getDocumentBuilder().newDocument());
	}
	
	/**
	 * Creates a new NeXML document, to be populated programmatically.
	 * Catches ParserConfigurationException internally, prints stack trace
	 * @return an org.nexml.model.Document object
	 */
	static public Document safeCreateDocument() {
		Document doc = null;
		try {
			doc = new DocumentImpl(getDocumentBuilder().newDocument());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * Parses a NeXML document, returns a populated object hierarchy
	 * that reflects the document's contents
	 * @param inputStream
	 * @return an org.nexml.model.Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	static public Document parse(InputStream inputStream)
			throws ParserConfigurationException, SAXException, IOException {
		return createDocument(getDocumentBuilder().parse(inputStream));
	}
	
	/**
	 * Like parse, but wraps it in a try/catch block
	 * @param inputStream
	 * @return
	 */
	static public Document safeParse(InputStream inputStream) {
		try {
			return parse(inputStream);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parses a NeXML document, returns a populated object hierarchy
	 * that reflects the document's contents
	 * @param uri
	 * @return an org.nexml.model.Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	static public Document parse(String uri) throws SAXException, IOException,
			ParserConfigurationException {
		return createDocument(getDocumentBuilder().parse(uri));
	}
	
	/**
	 * Like parse, but wraps it in a try/catch block
	 * @param inputStream
	 * @return
	 */
	static public Document safeParse(String uri) {
		try {
			return parse(uri);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	

	/**
	 * Parses a NeXML document, returns a populated object hierarchy
	 * that reflects the document's contents
	 * @param file
	 * @return an org.nexml.model.Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	static public Document parse(File file) throws SAXException, IOException,
			ParserConfigurationException {
		return createDocument(getDocumentBuilder().parse(file));
	}
	
	/**
	 * Like parse, but wraps it in a try/catch block
	 * @param inputStream
	 * @return
	 */
	static public Document safeParse(File file) {
		try {
			return parse(file);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	

	/**
	 * Parses a NeXML document, returns a populated object hierarchy
	 * that reflects the document's contents
	 * @param inputSource
	 * @return an org.nexml.model.Document object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	static public Document parse(InputSource inputSource) throws SAXException,
			IOException, ParserConfigurationException {
		return createDocument(getDocumentBuilder().parse(inputSource));
	}

	/**
	 * Like parse, but wraps it in a try/catch block
	 * @param inputStream
	 * @return
	 */
	static public Document safeParse(InputSource inputSource) {
		try {
			return parse(inputSource);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	static private Document createDocument(org.w3c.dom.Document domDocument) {
		return new DocumentImpl(domDocument, domDocument.getDocumentElement());
	}

	/**
	 * Helper method to instantiate a new DocumentBuilder object
	 * @return a DocumentBuilder object
	 * @throws ParserConfigurationException
	 */
	static private DocumentBuilder getDocumentBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		return documentBuilder;
	}
}
