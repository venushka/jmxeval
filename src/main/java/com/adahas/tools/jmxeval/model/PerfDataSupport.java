package com.adahas.tools.jmxeval.model;

import com.adahas.tools.jmxeval.model.Element.Field;

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
  Field getVar();

  /**
   * Critical value/level enforced by the implementation element
   *
   * @return critical value/level
   */
  Field getCritical();

  /**
   * Warning value/level enforced by the implementation element
   *
   * @return warning value/level
   */
  Field getWarning();
}
