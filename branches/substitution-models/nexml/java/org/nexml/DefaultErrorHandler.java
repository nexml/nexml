package org.nexml;

//$Id: DefaultErrorHandler.java $

/**
 * The error handler allows parsing errors to be reported by NexmlParser.
 * The interface for this behavior is defined in org.xml.sax.ErrorHandler
 * and used by setErrorHandler() and getErrorHandler() in NexmlParser.
 * This class is a concrete implementation thereof.
 * @author pmidford
 * @see    NexmlParser
 * @see    ObjectFactory
 */


import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler {
    
    private boolean rethrowErrors = false;
    private boolean rethrowWarnings = false;

    /**
     * @arg exception
     * @throws 
     */
    public void error(SAXParseException exception) throws SAXException {
        errPrint("An error was reported: " + exception);
        if (rethrowErrors)
            throw exception;
    }

    /**
     * @arg exception
     * @throws 
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        errPrint("A fatal error was reported: " + exception);
        if (rethrowErrors)
            throw exception;
    }

    /**
     * @arg exception
     * @throws 
     */
    public void warning(SAXParseException exception) throws SAXException {
        errPrint("A warning was reported: " + exception);
        if (rethrowWarnings)
            throw exception;
    }
    
    private void errPrint(String s){
        System.err.println(s);
    }

}
