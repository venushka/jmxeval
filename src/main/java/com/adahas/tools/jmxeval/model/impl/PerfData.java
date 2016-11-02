package com.adahas.tools.jmxeval.model.impl;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
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
  private final Field label;

  /**
   * Variable to refer
   */
  private final Field var;

  /**
   * Critical value/level
   */
  private final Field critical;

  /**
   * Warning value/level
   */
  private final Field warning;

  /**
   * Minimum value
   */
  private final Field min;

  /**
   * Maximum value
   */
  private final Field max;

  /**
   * Unit of measurement
   */
  private final Field unit;

  /**
   * Constructs the element
   *
   * @param node XML node
   * @param parentElement Parent element
   */
  public PerfData(final Context context, final Node node, final Element parentElement) {
    super(context);

    final PerfDataSupport parent = (PerfDataSupport) parentElement;

    this.label = getNodeAttr(node, "label", parent.getVar());
    this.var = parent.getVar();
    this.critical = getNodeAttr(node, "critical", parent.getCritical());
    this.warning = getNodeAttr(node, "warning", parent.getWarning());
    this.min = getNodeAttr(node, "min");
    this.max = getNodeAttr(node, "max");
    this.unit = getNodeAttr(node, "unit");
  }

  /**
   * @see Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    final Object value = context.getVar(var.get());

    // set performance data
    context.getResponse().addPerfData(new PerfDataResult(label.get(), value, unit.get(), warning.get(), critical.get(), min.get(), max.get()));

    // process child elements
    super.process();
  }
}
