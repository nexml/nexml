package org.nexml;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

/*
 * Welcome to the source of the nexml parser libraries!
 * 
 * This comment block will outline the basic design, giving examples 
 * how to use it. 
 * 
 * The class you're looking at now is the entry point
 * into the parser library. Instances of this class will hold a 
 * reference to something that implements the interface ElementHandler,
 * which beyond having to implement the org.xml.sax.ContentHandler has 
 * some getter and setter methods for object factories and for an object
 * listener. 
 * 
 * The factories (which implement the ObjectFactory interface)
 * will be called whenever a new xml element node is encountered, and the
 * objects they return will be sent to the listener (an implementation
 * of ObjectListener).
 * 
 * Therefore, the flow of data and objects is as follows:
 * NexmlParser    -- passes node stream to --> 
 * ElementHandler -- calls createObject for each node -->
 * ObjectFactory  -- returns object -->
 * ElementHandler -- calls newObjectNotification -->
 * ObjectListener
 * 
 * And so the simplest usage is to implement an ObjectListener, which
 * will be passed a DefaultObject for every encountered element node
 * in the document.
 * 
 * However, the DefaultObject (which only has some simple fields for
 * xml id's, id references, label attributes and containing elements)
 * is not very interesting and probably not what you want in most 
 * cases. The next step is therefore to implement your own ObjectFactory
 * implementations. This is the part where you have to map the elements
 * in the nexml spec onto objects you'd like to use. For example, for
 * a <taxon> element, you may have a taxon object, and so you need to
 * write a factory that will instantiate that object (which will then
 * be passed to the listener).
 * 
 */

public class NexmlParser {
	private ElementHandler handler;
	private XMLReader xr;
	
	public NexmlParser(ElementHandler myHandler) throws ParserConfigurationException, SAXException {
		if ( myHandler == null ) {
			this.handler = new DefaultElementHandler();
		}
		else {
			this.handler = myHandler;
		}
		this.initXMLReader();
	}
	
	public NexmlParser() throws ParserConfigurationException, SAXException {
		this.handler = new DefaultElementHandler();
		this.initXMLReader();
		print("created new NexmlParser");
	}
	
	private void initXMLReader () throws SAXException {
		this.xr = XMLReaderFactory.createXMLReader();
		this.xr.setContentHandler(this.handler);
	}
	
	public ElementHandler getHandler() {
		return this.handler;
	}
	
	public void setHandler(ElementHandler myHandler) {
		this.handler = myHandler;
	}
	
	public void parse(String url) throws SAXException, IOException {
		this.xr.parse(url);
		print("created dom and root element from url " + url);
	}
	
	public void parseString (String xml) throws SAXException, IOException {
		Reader reader = new StringReader(xml);
		this.parse(new InputSource(reader));
	}
	
	public void parseFile (String path) throws SAXException, IOException {
		this.xr.parse(new InputSource(new FileInputStream(new File(path))));
	}
	
	public void parse(InputSource input) throws SAXException, IOException {
		this.xr.parse(input);
		print("created dom and root element from input source " + input);
	}
	
	public static void print (String line) {
		System.out.println(line);
	}
}
