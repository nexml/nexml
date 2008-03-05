package org.nexml;

// $Id$

import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The class you're looking at now is the entry point
 * into the parser library. Instances of this class will hold a 
 * reference to something that implements the interface ElementHandler,
 * which beyond having to implement the org.xml.sax.ContentHandler has 
 * some getter and setter methods for object factories and for an object
 * listener. 
 * <br><br>
 * The factories (which implement the ObjectFactory interface)
 * will be called whenever a new xml element node is encountered, and the
 * objects they return will be sent to the listener (an implementation
 * of ObjectListener).
 * <br><br>
 * Therefore, the flow of data and objects is as follows:
 * <ol>
 * <li>NexmlParser    -- passes node stream to --> </li> 
 * <li>ElementHandler -- calls createObject for each node --></li>
 * <li>ObjectFactory  -- returns object --></li>
 * <li>ElementHandler -- calls newObjectNotification --></li>
 * <li>ObjectListener</li>
 * </ol>
 * <br><br>
 * And so the simplest usage is to implement an ObjectListener, which
 * will be passed a DefaultObject for every encountered element node
 * in the document.
 * <br><br>
 * However, the DefaultObject (which only has some simple fields for
 * xml id's, id references, label attributes and containing elements)
 * is not very interesting and probably not what you want in most 
 * cases. The next step is therefore to implement your own ObjectFactory
 * implementations. This is the part where you have to map the elements
 * in the nexml spec onto objects you'd like to use. For example, for
 * an <otu> element, you may have a taxon object, and so you need to
 * write a factory that will instantiate that object (which will then
 * be passed to the listener).
 * 
 */

public class NexmlParser {
	private ElementHandler handler;
	private XMLReader xr;
	
	/**
	 * Instantiates a new NexmlParser object when an ElementHandler is available
	 * @param  myHandler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @see    ElementHandler
	 */
	public NexmlParser(ElementHandler myHandler) throws ParserConfigurationException, SAXException {
		if ( myHandler == null ) {
			this.handler = new DefaultElementHandler();
		}
		else {
			this.handler = myHandler;
		}
		this.initXMLReader();
	}
	
	/**
	 * Instantiates a new NexmlParser object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */	
	public NexmlParser() throws ParserConfigurationException, SAXException {
		this.handler = new DefaultElementHandler();
		this.initXMLReader();
		print("created new NexmlParser");
	}
	
	/**
	 * Initializes the xml reader by having the XMLReaderFactory instantiate one,
	 * then sets the element handler on the reader
	 */
	private void initXMLReader () throws SAXException {
		this.xr = XMLReaderFactory.createXMLReader();
		this.xr.setContentHandler(this.handler);
	}
	
	/**
	 * Gets the ElementHandler
	 * @return an ElementHandler
	 */
	public ElementHandler getHandler() {
		return this.handler;
	}
	
	/**
	 * Sets an ElementHandler to process the stream
	 * @param myHandler an ElementHandler
	 */
	public void setHandler(ElementHandler myHandler) {
		this.handler = myHandler;
	}
	
	/**
	 * Sets an error handler for the XMLReader
	 * @param myErrorHandler an ErrorHandler
	 */
	public void setErrorHandler(ErrorHandler myErrorHandler){
	    xr.setErrorHandler(myErrorHandler);
	}
	
	/**
	 * Gets the error handler 
	 * @return an ErrorHandler
	 */
	public ErrorHandler getErrorHandler(){
	    return xr.getErrorHandler();
	}
	
	/**
	 * Sets a feature in the XMLReader
     * @param name The feature name, which is a fully-qualified URI.
     * @param value The requested value of the feature (true or false).
	 * @throws SAXNotSupportedException If the reader doesn't support the feature
	 * @throws SAXNotRecognizedException If the reader can't set the feature to value
	 */
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException{
	    xr.setFeature(name, value);
	}

	/**
	 * Gets the state of a feature in the XMLReader
	 * @param name The feature name, which is a fully-qualified URI.
	 * @return a boolean value reflecting the state of the feature in the XMLReader
	 * @throws SAXNotRecognizedException If the reader doesn't support the feature
	 * @throws SAXNotSupportedException If the reader can't retrieve a value for the feature
	 */
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException{
	    return xr.getFeature(name);
	}
	
	/**
	 * Parses xml from a URL
	 * @param url a url from which to read xml
	 * @throws SAXException
	 * @throws IOException
	 */	
	public void parse(String url) throws SAXException, IOException {
		this.xr.parse(url);
		print("created dom and root element from url " + url);
	}
	
	/**
	 * Parses xml from a string
	 * @param xml a string to read
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parseString (String xml) throws SAXException, IOException {
		Reader reader = new StringReader(xml);
		this.parse(new InputSource(reader));
	}
	
	/**
	 * Parses xml from a file
	 * @param path a file path to read from
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parseFile (String path) throws SAXException, IOException {
		this.xr.parse(new InputSource(new FileInputStream(new File(path))));
	}
	
	/**
	 * Parses xml from an InputSource
	 * @param input an InputSource
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parse(InputSource input) throws SAXException, IOException {
		this.xr.parse(input);
		print("created dom and root element from input source " + input);
	}
	
	/**
	 * Prints messages to standard out 
	 * @param line a string to print out
	 */
	public static void print (String line) {
		System.out.println(line);
	}
}
