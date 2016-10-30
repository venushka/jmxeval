package com.adahas.tools.jmxeval.exception;

public class BadNagiosRangeException extends IllegalArgumentException {

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
