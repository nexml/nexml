package org.nexml.model.impl;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nexml.model.CategoricalMatrix;
import org.nexml.model.Document;
import org.nexml.model.OTUs;
import org.nexml.model.TreeBlock;
import org.w3c.dom.Element;

public class DocumentImpl extends NexmlWritableImpl implements Document {
	
	public DocumentImpl(org.w3c.dom.Document document) {
		super(document);
		setRootAttributes();
	}
	
	protected DocumentImpl(org.w3c.dom.Document document,Element element) {
		super(document,element);
		setRootAttributes();
	}	
	
	private void setRootAttributes() {
		getElement().setAttribute("version", "0.8");
		getElement().setAttribute("generator", getClass().getName());
		getElement().setPrefix("nex");
		getElement().removeAttribute("id");
	}
	
	protected DocumentImpl(){}

	public OTUs createOTUs() {
		OTUsImpl otus = new OTUsImpl(getDocument());
		getElement().appendChild(otus.getElement());
		return otus;
	}

	public TreeBlock createTreeBlock(OTUs otus) {
		TreeBlockImpl treeBlock = new TreeBlockImpl(getDocument());
		getElement().appendChild(treeBlock.getElement());
		treeBlock.setOTUs(otus);
		return treeBlock;		
	}


	@Override
	String getTagName() {
		return "nexml";
	}

	public CategoricalMatrix createCategoricalMatrix(OTUs otus) {
		CategoricalMatrixImpl categoricalMatrix = new CategoricalMatrixImpl(getDocument());
		getElement().appendChild(categoricalMatrix.getElement());
		categoricalMatrix.setOTUs(otus);
		return categoricalMatrix;
	}

	public String getXmlString() {
        StringWriter stringWriter = new StringWriter();		
        try {
            Source source = new DOMSource(getElement());
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);            
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return stringWriter.getBuffer().toString();
	}

}
