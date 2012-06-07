package com.adahas.tools.jmxeval.exception;

import com.adahas.tools.jmxeval.response.Status;

/**
 * Exception to throw when a known exception occurs
 */
public class EvalException extends Exception {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Line break character
   */
  private static final String LINE_BREAK = "\n";
  
  /**
   * Carriage return character
   */
  private static final String CARRIAGE_RETURN = "\r";
  
  /**
   * Tab character
   */
  private static final String TAB = "\t";
  
  /**
   * White space character
   */
  private static final String WHITE_SPACE = " ";
  
  /**
   * Status to report when this exception occurs
   */
  private final transient Status status;
  
  /**
   * Constructs an exception instance
   * 
   * @param status Status to report in the event of this exception
   * @param message Exception message
   * @param cause Cause of the exception
   */
  public EvalException(final Status status, final String message, final Throwable cause) {
    super(convertToSingleLine(message), cause);
    this.status = status;
  }
  
  /**
   * Constructs an exception instance
   * 
   * @param status Status to report in the event of this exception
   * @param message Exception message
   */
  public EvalException(final Status status, final String message) {
    super(convertToSingleLine(message));
    this.status = status;
  }

  /**
   * Get the eval status to report for the exception
   * 
   * @return the status
   */
  public Status getStatus() {
    return status;
  }
  
  /**
   * Converts a multi line string to a single line string
   */
  protected static String convertToSingleLine(final String source) {
    String returnString;
    
    if (source == null) {
      returnString = source;
    } else {
      returnString = source.replaceAll(LINE_BREAK, WHITE_SPACE).replaceAll(
          CARRIAGE_RETURN, WHITE_SPACE).replaceAll(TAB, WHITE_SPACE);
      
      while (returnString.contains(WHITE_SPACE + WHITE_SPACE)) {
        returnString = returnString.replaceAll(WHITE_SPACE + WHITE_SPACE, WHITE_SPACE).trim();
      }
    }
    
    return returnString;
  }
}
