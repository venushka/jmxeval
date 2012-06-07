package com.adahas.tools.jmxeval.model;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ErrorHandler implementation to parse the configuration XML
 */
public class ConfigurationErrorHandler implements ErrorHandler {
  
  public void warning(final SAXParseException exception) throws SAXException {
    // ignore warnings
  }
  
  public void fatalError(final SAXParseException exception) throws SAXException {
    throw new SAXException(exception.getMessage(), exception);
  }
  
  public void error(final SAXParseException exception) throws SAXException {
    throw new SAXException(exception.getMessage(), exception);
  }
}