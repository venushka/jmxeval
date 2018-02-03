package com.adahas.tools.jmxeval.model.impl;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;
import com.adahas.tools.jmxeval.exception.JMXEvalException;
import com.adahas.tools.jmxeval.model.Element;

public class Arg extends Element {

  /**
   * Type of the argument.
   */
  private final Field type;

  /**
   * Value to set.
   */
  private final Field value;

  /**
   * Constructs the element
   *
   * @param context Execution context
   * @param node XML node
   */
  public Arg(final Context context, final Node node) {
    super(context);

    type = getNodeAttr(node, "type");
    value = getNodeAttr(node, "value");
  }

  /**
   * @see com.adahas.tools.jmxeval.model.Element#process()
   */
  @Override
  public void process() throws JMXEvalException {
    // nothing to do
  }

  /**
   * Get the argument type.
   *
   * @return the argument type
   */
  public Field getType() {
    return type;
  }

  /**
   * Get the argument value.
   *
   * @return the argument value
   */
  public Field getValue() {
    return value;
  }
}

