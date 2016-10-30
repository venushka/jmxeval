package com.adahas.tools.jmxeval.model;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ErrorHandler implementation to parse the configuration XML
 */
public class ConfigurationErrorHandler implements ErrorHandler {

  /**
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  @Override
  public void warning(final SAXParseException exception) throws SAXException {
    // ignore warnings
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  @Override
  public void fatalError(final SAXParseException exception) throws SAXException {
    throw new SAXException(exception.getMessage(), exception);
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  @Override
  public void error(final SAXParseException exception) throws SAXException {
    throw new SAXException(exception.getMessage(), exception);
  }
}