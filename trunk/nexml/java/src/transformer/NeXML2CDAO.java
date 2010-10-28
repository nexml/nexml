package transformer;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;


/**
 * @author rvosa
 *
 */
public class NeXML2CDAO {
	private static String RDFa2RDFXML = "/xslt/RDFa2RDFXML.xsl";
	private static String NEXML2CDAO  = "/xslt/nexml2cdao.xsl";
		
	/**
	 * Applies the XSL transformation file located at xsltLocation (a URL) to the
	 * provided input DOM document, returns a DOMResult
	 * @param inputDoc
	 * @param xsltFile
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SaxonApiException 
	 */
	static private Document transform(File inputFile, File xsltFile) throws SaxonApiException, ParserConfigurationException {		
		Processor processor = new Processor(false);
		XsltCompiler xsltCompiler = processor.newXsltCompiler();
        XsltExecutable xsltExecutable = xsltCompiler.compile(new StreamSource(xsltFile));
        XdmNode xdmNodeSource = processor.newDocumentBuilder().build(new StreamSource(inputFile));
        XsltTransformer xsltTransformer = xsltExecutable.load();
        xsltTransformer.setInitialContextNode(xdmNodeSource);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document newDocument = documentBuilder.newDocument();
        DOMDestination domDestination = new DOMDestination(newDocument);
        xsltTransformer.setDestination(domDestination);
        xsltTransformer.transform();
        return newDocument;
	}

	/**
	 * Merges the two provided Documents such that all children in the root node
	 * of rdfaResult become children of the root node of cdaoResult, returns the
	 * merged DOM document
	 * @param rdfaResult
	 * @param cdaoResult
	 * @return
	 */
	private static Document merge(Document rdfaResult, Document cdaoResult) {
		Element rdfaRootNode = rdfaResult.getDocumentElement();
		Element cdaoRootNode = cdaoResult.getDocumentElement();
		NodeList rdfaRootNodeChildren = rdfaRootNode.getChildNodes();
		for ( int i = 0; i < rdfaRootNodeChildren.getLength(); i++ ) {
			Node rdfaRootNodeChild = rdfaRootNodeChildren.item(i);
			Node importedRdfaRootNodeChild = cdaoResult.importNode(rdfaRootNodeChild, true);
			cdaoRootNode.appendChild(importedRdfaRootNodeChild);			
		}		
		return cdaoResult;
	}	
	
	/**
	 * 
	 * @param document
	 * @param indent
	 * @param indentation
	 * @return
	 * @throws TransformerException
	 */
	private static String stringify(Document document,boolean indent,int indentation) throws TransformerException {
		StringWriter stringWriter = new StringWriter();
		Source source = new DOMSource(document);
		Result result = new StreamResult(stringWriter);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", ""+indentation);
		transformer.transform(source, result);
		return stringWriter.getBuffer().toString();		
	}
	
	/**
	 * @param args
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SaxonApiException 
	 */
	public static void main(final String args[]) throws SaxonApiException, ParserConfigurationException, TransformerException {
		if ( args.length < 1 ) {
			System.out.println("Usage: transform.NeXML2CDAO <input file>");
			System.exit(1);
		}
		else {
			String NEXML_ROOT;
			if ( args.length > 1 ) {
				NEXML_ROOT = args[1];
			}
			else {
				NEXML_ROOT = System.getenv("NEXML_ROOT");
			}
			File inputFile = new File(args[0]);
			File nexml2cdaoFile = new File(NEXML_ROOT + NEXML2CDAO);
			Document cdaoResult = transform(inputFile,nexml2cdaoFile);
			
			File rdfa2rdfxmlFile = new File(NEXML_ROOT + RDFa2RDFXML);
			Document rdfaResult = transform(inputFile,rdfa2rdfxmlFile);
			
			Document mergedDoc = merge(rdfaResult,cdaoResult);
			System.out.println(stringify(mergedDoc,true,2));
			System.exit(0);
		}
	}
	
}
