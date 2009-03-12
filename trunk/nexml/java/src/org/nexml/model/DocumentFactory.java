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
	static public Document createDocument() throws ParserConfigurationException {
		return new DocumentImpl(getDocumentBuilder().newDocument());
	}

	static public Document parse(InputStream inputStream)
			throws ParserConfigurationException, SAXException, IOException {
		Document document = new DocumentImpl(getDocumentBuilder().parse(
				inputStream));
		return document;
	}

	static public Document parse(String uri) throws SAXException, IOException,
			ParserConfigurationException {
		org.w3c.dom.Document domDocument = getDocumentBuilder().parse(uri);
		Document document = new DocumentImpl(domDocument, domDocument
				.getDocumentElement());
		return document;
	}

	static public Document parse(File file) throws SAXException, IOException,
			ParserConfigurationException {
		Document document = new DocumentImpl(getDocumentBuilder().parse(file));
		return document;
	}

	static public Document parse(InputSource inputSource) throws SAXException,
			IOException, ParserConfigurationException {
		Document document = new DocumentImpl(getDocumentBuilder().parse(
				inputSource));
		return document;
	}

	static private DocumentBuilder getDocumentBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		return documentBuilder;
	}
}
