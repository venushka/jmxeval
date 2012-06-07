package com.adahas.tools.jmxeval.model.impl;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.EvalException;
import com.adahas.tools.jmxeval.model.Element;
import com.adahas.tools.jmxeval.model.PerfDataSupport;
import com.adahas.tools.jmxeval.response.PerfDataResult;

/**
 * Element to enable reporting performance data
 */
public class PerfData extends Element {

  /**
   * Data label
   */
  private transient final String label;
  
  /**
   * Variable to refer
   */
  private transient final String var;
  
  /**
   * Critical value/level
   */
  private transient final String critical;
  
  /**
   * Warning value/level
   */
  private transient final String warning;
  
  /**
   * Minimum value
   */
  private transient final String min;
  
  /**
   * Maximum value
   */
  private transient final String max;
  
  /**
   * Unit of measurement
   */
  private transient final String unit;
  
  /**
   * Constructs the element
   * 
   * @param node XML node
   * @param parentElement Parent element
   */
  public PerfData(final Node node, final Element parentElement) {
    super(parentElement);
    
    final PerfDataSupport parent = (PerfDataSupport) parentElement;
    
    this.label = getNodeAttribute(node, "label", parent.getVar());
    this.var = parent.getVar();
    this.critical = getNodeAttribute(node, "critical", parent.getCritical());
    this.warning = getNodeAttribute(node, "warning", parent.getWarning());
    this.min = getNodeAttribute(node, "min");
    this.max = getNodeAttribute(node, "max");
    this.unit = getNodeAttribute(node, "unit");
  }
  
  /**
   * @see Element#process(Context)
   */
  @Override
  public void process(final Context context) throws EvalException {
    final Object value = context.getVar(var);
    
    // set performance data
    context.getResponse().addPerfData(new PerfDataResult(
        label, value, unit, warning, critical, min, max));
    
    // process child elements
    super.process(context);
  }
}
