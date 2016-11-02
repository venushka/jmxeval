package com.adahas.tools.jmxeval.model;

import org.w3c.dom.Node;

import com.adahas.tools.jmxeval.Context;

public class ElementWithMultipleConstructors extends Element {

  private Node node;
  private Element parent;

  public ElementWithMultipleConstructors(final Context context, final Node node) {
    super(context);
    this.node = node;
  }

  public ElementWithMultipleConstructors(final Context context, final Element parent) {
    super(context);
    this.parent = parent;
  }

  public Element getParent() {
    return parent;
  }

  public Node getNode() {
    return node;
  }
}

