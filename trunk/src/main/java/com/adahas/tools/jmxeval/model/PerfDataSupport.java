package com.adahas.tools.jmxeval.model;

/**
 * Interface to make a element instance support
 * performance data reporting
 */
public interface PerfDataSupport {

  /**
   * Get the variable name that is taken as performance data
   * 
   * @return Variable name
   */
  String getVar();
  
  /**
   * Critical value/level enforced by the implementation element
   * 
   * @return critical value/level
   */
  String getCritical();
  
  /**
   * Warning value/level enforced by the implementation element
   * 
   * @return warning value/level
   */
  String getWarning();
}
