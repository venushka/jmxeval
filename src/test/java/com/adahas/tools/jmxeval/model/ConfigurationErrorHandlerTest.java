package com.adahas.tools.jmxeval.model;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ConfigurationErrorHandlerTest {

  private final ConfigurationErrorHandler handler = new ConfigurationErrorHandler();

  /**
   * Ensure warnings are not re-thrown.
   */
  @Test
  public void testWarning() throws Exception {
    handler.warning(new SAXParseException("parsing failed", null));
  }

  /**
   * Ensure fatal errors are thrown.
   */
  @Test
  public void testFatalError() {
    try {
      handler.fatalError(new SAXParseException("parsing failed", null));
      fail("Should re-throw the exception for fatal errors");
    } catch (SAXException e) {
      // expected
    }
  }

  /**
   * Ensure errors are thrown.
   */
  @Test
  public void testError() {
    try {
      handler.error(new SAXParseException("parsing failed", null));
      fail("Should re-throw the exception for errors");
    } catch (SAXException e) {
      // expected
    }
  }
}

