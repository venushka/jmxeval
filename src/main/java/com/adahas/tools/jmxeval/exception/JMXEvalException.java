package com.adahas.tools.jmxeval.exception;

public class JMXEvalException extends Exception {

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public JMXEvalException(final String message, final Throwable cause) {
    super(toSingleLine(message), cause);
  }

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public JMXEvalException(final String message) {
    super(toSingleLine(message));
  }

  /**
   * Converts a multi-line string to a single line string
   */
  protected static String toSingleLine(final String source) {
    if (source == null) {
      throw new IllegalArgumentException("Exception message cannot be null");
    } else {
      // Replace line break, carriage return and tab with white space.
      // Replace any blocks of two or more white spaces with a single white space.
      // Trim to remove any leading or trailing white spaces.
      return source.replaceAll("[\\n\\r\\t]+", " ").replaceAll("[\\s]{2,}", " ").trim();
    }
  }
}