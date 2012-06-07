package com.adahas.tools.jmxeval.response;

/**
 * Status of the JMX check
 */
public enum Status {
  
  // statuses defined based on the priority order
  UNKNOWN(3),
  CRITICAL(2),
  WARNING(1),
  OK(0);
  
  private final int value;
  
  /**
   * Constructs a status instance
   * 
   * @param value 
   */
  Status(final int value) {
    this.value = value;
  }
  
  /**
   * Get the numerical representation of the status
   * 
   * @return status value
   */
  public int getValue() {
    return value;
  }
}
