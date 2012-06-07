package com.adahas.tools.jmxeval.exception;

/**
 * Exception thrown when configuration is invalid. These exceptions
 * are not expected to be thrown while execution unless any configurations
 * or execution arguments change.
 */
public class ConfigurationException extends Exception {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an exception instance
   * 
   * @param message Exception message
   * @param cause Cause of the exception
   */
  public ConfigurationException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  /**
   * Constructs an exception instance
   * 
   * @param message Exception message
   */
  public ConfigurationException(final String message) {
    super(message);
  }
}
