package com.adahas.tools.jmxeval.model.impl;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.model.Element;

/**
 * Root configuration element
 */
public class JMXEval extends Element {

  /**
   * Constructs the element
   *
   * @param node XML node
   * @param parentElement Parent element
   */
  public JMXEval(@SuppressWarnings("unused") final Node node, final Element parentElement) {
    super(parentElement);
  }
}
