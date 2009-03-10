package org.nexml.model;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class testInputNexml {
	@Test
	public void readFile() {
		// Nexml nexml = new Nexml(file);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();

			Document document = documentBuilder
					.parse("file:///C:/Users/samd/Documents/Projects/nexml/examples/trees.xml");
			NodeList otus = document.getElementsByTagName("otu");
			for (int i = 0; i < otus.getLength(); i++) {
				Node otu = otus.item(i);
				System.out.println("otu: " + otu.getNodeName() + "/" + otu.getAttributes().item(0));
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
