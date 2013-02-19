package com.adahas.tools.jmxeval.exception;

import java.lang.IllegalArgumentException;
import java.lang.String;
import java.lang.Throwable;

public class BadNagiosRangeException extends IllegalArgumentException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an exception instance
   * 
   * @param message Exception message
   * @param cause Cause of the exception
   */
  public BadNagiosRangeException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  /**
   * Constructs an exception instance
   * 
   * @param message Exception message
   */
  public BadNagiosRangeException(final String message) {
    super(message);
  }
}
