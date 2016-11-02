package com.adahas.tools.jmxeval.response;

/**
 * Result of a single eval check
 */
public class EvalResult {

  /**
   * Name of the eval
   */
  private final String name;

  /**
   * Eval status
   */
  private final Status status;

  /**
   * Eval result message
   */
  private final String message;

  /**
   * Constructs an eval result
   *
   * @param name Name of the eval
   * @param status Eval status
   * @param message Eval result message
   */
  public EvalResult(final String name, final Status status, final String message) {
    this.name = name;
    this.status = status;

    if (message == null) {
      this.message = null;
    } else {
      this.message = message.replaceAll("[\\n\\r\\t]+", " ").replaceAll("[\\s]{2,}", " ").trim();
    }
  }

  /**
   * Get the name of the eval
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the status of the eval
   *
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Get the result message of the eval
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Convert the result to its string representation
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(name);
    builder.append(" ");
    builder.append(status);
    if (message != null) {
      builder.append(" - ");
      builder.append(message);
    }
    return builder.toString();
  }
}
